package com.example.ve

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.ventarapida.ui.detalleProducto.DetalleProductoViewModel



import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentDetalleProductoBinding

import com.example.ventarapida.ui.data.ModeloProducto
import com.example.ventarapida.ui.process.HideKeyboard
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File

class DetalleProducto : Fragment() {
    companion object {

        // Creación de una instancia de DetalleProducto
        fun newInstance() = DetalleProducto()
        private val GALERIA_REQUEST_CODE = 1001
        private val CAMARA_REQUEST_CODE = 1002
        private val REQUEST_IMAGE_CAPTURE=1003
    }
//    private lateinit var toolbar: Toolbar
    private var binding: FragmentDetalleProductoBinding? = null
    private val viewModel: DetalleProductoViewModel by viewModels() // Inicialización de viewModel
    private lateinit var productosViewModel: DetalleProductoViewModel
    private var vista: View? = null
    private lateinit var id_producto:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflar el layout del fragmento usando el binding
        binding = FragmentDetalleProductoBinding.inflate(inflater, container, false)
//        toolbar = view.findViewById(R.id.toolbar)
        // Inicialización de Firebase
        FirebaseApp.initializeApp(requireContext())

        // Recibe los productos de la lista del fragmento anterior
        val bundle = arguments
        val modeloProducto = bundle?.getSerializable("modelo") as? ModeloProducto
        val listaDeProductos = bundle?.getSerializable("listaProductos") as? ArrayList<ModeloProducto>
        val posicionProducto = bundle?.getInt("position")

        // Llama al método actualizarListaProductos para indicar la lista de productos
        viewModel.actualizarListaProductos(listaDeProductos!!)

        // Inicialización de productosViewModel
        productosViewModel = ViewModelProvider(this).get(DetalleProductoViewModel::class.java)
        viewModel.init(requireActivity()) // Pasar la referencia de la actividad

        // Observa los cambios en detalleProducto y actualiza la UI en consecuencia
        productosViewModel.detalleProducto.observe(viewLifecycleOwner, Observer { detalleProducto ->
            if (detalleProducto.isNotEmpty()) {
                val producto = detalleProducto[0]
                actualizarCampos(producto)
            }else{
                cargarSiguienteProducto()
            }
        })
        productosViewModel.mensajeToast.observe(viewLifecycleOwner) { it
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }



        // Define el botón "Siguiente" y configura su OnClickListener
        binding?.imageViewBotonDerecha?.setOnClickListener {
            cargarSiguienteProducto()
        }

        // Define el botón "Anterior" y configura su OnClickListener
        binding?.imageViewBotonIzquierda?.setOnClickListener {
            cargarAnteriorProducto()
        }

        //Define el botón "Guardar"

        // Indica la posición del producto para abrir el producto seleccionado
        viewModel.actualizarPosiscion(posicionProducto!!)
        verificarPosiciones()

        // Carga el producto en la UI
        cargarProducto(modeloProducto)

        return binding!!.root // Retorna la vista inflada
    }


    private fun cargarImagen() {

        // Crear un AlertDialog con las opciones de cámara y galería
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecciona una opción")
        builder.setItems(arrayOf("Tomar foto", "Elegir de galería")) { dialog, which ->
            when (which) {
                0 -> tomarFoto()
                1 -> elegirDeGaleria()
            }
        }
        builder.create().show()

    }


    private lateinit var imagenUri: Uri

    private fun tomarFoto() {

        // Verificar si se tienen los permisos necesarios para utilizar la cámara
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Si no se tienen los permisos, solicitarlos al usuario
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
        } else {

            val photoFile = File(requireContext().getExternalFilesDir(null), "CompraRapidita.jpg")
            imagenUri = FileProvider.getUriForFile(requireContext(), "com.example.ventarapida.fileprovider", photoFile)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
            startActivityForResult(intent, CAMARA_REQUEST_CODE)

        }
    }



    // Función para elegir una imagen de la galería
    private fun elegirDeGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, GALERIA_REQUEST_CODE)
    }

    // Variable para almacenar la URI de la imagen resultante
    private var uri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Si la acción fue tomar una foto con la cámara
        if (requestCode == CAMARA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            // Recortar la imagen usando la biblioteca CropImage
            CropImage.activity(imagenUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(requireContext(), this)
        }

        // Si la acción fue elegir una imagen de la galería
        if (requestCode == GALERIA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Obtener la URI de la imagen seleccionada de la galería
            uri = data?.data
            // Recortar la imagen usando la biblioteca CropImage
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(requireContext(), this)

        }

        // Si la acción fue recortar la imagen usando CropImage
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                // Obtener el archivo de la imagen recortada
                val file = File(result.uri.path!!)
                if (file.exists()) {
                    // Cargar la imagen recortada en el ImageView
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    binding?.imageViewFoto?.setImageBitmap(bitmap)
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                // Mostrar un mensaje de error si la recortada no fue exitosa
                val error = result.error
                Toast.makeText(requireContext(), "Error al recortar la imagen: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarCampos(producto: ModeloProducto) {
        id_producto=producto.id
        binding?.editTextProducto?.setText(producto.nombre)
        binding?.editTextPCompra?.setText(producto.p_compra)
        binding?.editTextPVenta?.setText(producto.p_diamante)
        binding?.editTextCantidad?.setText(producto.cantidad)
       if (!producto.url.isEmpty()) Picasso.get().load(producto.url)
            .into(binding?.imageViewFoto)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista = view // Guarda la vista en la variable vista
    }

    private fun verificarPosiciones() {
        //Verifica la posicion del array para mostrar o ocultar los botones de siguiente o anterior
        if ( viewModel.posicionActual == 0) {
            binding?.imageViewBotonIzquierda?.visibility = View.INVISIBLE
        }else{
            binding?.imageViewBotonIzquierda?.visibility = View.VISIBLE
        }
        if ( viewModel.posicionActual == viewModel.listaProductos.size-1) {
            binding?.imageViewBotonDerecha?.visibility = View.INVISIBLE
        }else{
            binding?.imageViewBotonDerecha?.visibility = View.VISIBLE
        }
    }
    private fun cargarAnteriorProducto() {
        // Descremente la posición actual en 1
        viewModel.posicionActual--

        // Si hemos llegado al final de la lista, volvemos al principio
        if (viewModel.posicionActual >= viewModel.listaProductos.size) {
            viewModel.posicionActual = 0
        }

        // Obtenemos el siguiente modelo de producto de la lista
        val siguienteModeloProducto = viewModel.listaProductos[viewModel.posicionActual]

        // Actualizamos el fragmento con los detalles del siguiente producto
        cargarProducto(siguienteModeloProducto)
        verificarPosiciones()
        HideKeyboard(requireContext()).hideKeyboard(vista!!)
    }

    private fun cargarSiguienteProducto() {
        // Incrementa la posición actual en 1
        viewModel.posicionActual++

        // Si hemos llegado al final de la lista, volvemos al principio
        if (viewModel.posicionActual >= viewModel.listaProductos.size) {
            viewModel.posicionActual = 0
        }

        // Obtenemos el siguiente modelo de producto de la lista
        val siguienteModeloProducto = viewModel.listaProductos[viewModel.posicionActual]

        // Actualizamos el fragmento con los detalles del siguiente producto
        cargarProducto(siguienteModeloProducto)
        verificarPosiciones()
        HideKeyboard(requireContext()).hideKeyboard(vista!!)
    }


    private fun cargarProducto(modeloProducto: ModeloProducto?) {
        viewModel.setIdProducto(modeloProducto!!.id)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menuproducto, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_guardar ->{
                    guardar()
                return true
            }

            R.id.action_camara->{
                cargarImagen()
                return true
            }

            R.id.action_eliminar -> {
                    eliminar()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun eliminar() {
        HideKeyboard(requireContext()).hideKeyboard(vista!!)

        // Crear el diálogo de confirmación
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar producto")
        builder.setMessage("¿Estás seguro de que deseas eliminar este producto?")
        builder.setPositiveButton("Eliminar") { dialog, which ->
            viewModel.eliminarProducto(id_producto)
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun guardar() {
        HideKeyboard(requireContext()).hideKeyboard(vista!!)

        //verificando campos vacios
        if (id_producto.isEmpty() || binding!!.editTextProducto.text.toString().isEmpty()
            || binding!!.editTextCantidad.text.toString().trim().isEmpty()|| binding!!.editTextPCompra.text.toString().trim().isEmpty()
            ||binding!!.editTextPVenta.text.toString().trim().isEmpty()){

            val snackbar= Snackbar.make(vista!!, "Todos los datos son obligatorios", Snackbar.LENGTH_LONG)
            snackbar.view.setBackgroundColor(resources.getColor(R.color.rojo))
            snackbar.setTextColor(resources.getColor(R.color.white))
            snackbar.show()
            return
        }

        viewModel.subirImagenFirebase(binding?.imageViewFoto!!)

        val updates = hashMapOf<String, Any>(
            "id" to id_producto.toString().trim(),
            "nombre" to binding!!.editTextProducto.text.toString().trim(),
            "cantidad" to binding!!.editTextCantidad.text.toString().trim(),
            "p_compra" to binding!!.editTextPCompra.text.toString().trim(),
            "p_diamante" to binding!!.editTextPVenta.text.toString().trim()

        )
        viewModel.guardarProducto(updates)
    }

}