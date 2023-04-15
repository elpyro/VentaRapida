package com.example.ve

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import com.example.ventarapida.ui.detalleProducto.DetalleProductoViewModel
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentDetalleProductoBinding
import com.example.ventarapida.ui.datos.ModeloProducto
import com.example.ventarapida.ui.procesos.OcultarTeclado
import com.example.ventarapida.ui.procesos.TomarFotoYGaleria
import com.example.ventarapida.ui.procesos.Utilidades.eliminarPuntosComas
import com.example.ventarapida.ui.procesos.Utilidades.escribirFormatoMoneda
import com.example.ventarapida.ui.procesos.VerificarInternet
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File

@Suppress("DEPRECATION")
class DetalleProducto : Fragment() {

//    private lateinit var toolbar: Toolbar
    private var binding: FragmentDetalleProductoBinding? = null
    private val viewModel: DetalleProductoViewModel by viewModels() // Inicialización de viewModel
    private lateinit var productosViewModel: DetalleProductoViewModel
    private var vista: View? = null
    private lateinit var id_producto:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        // Inflar el layout del fragmento usando el binding
        binding = FragmentDetalleProductoBinding.inflate(inflater, container, false)

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

        // Observa los cambios en detalleProducto y actualiza la UI en consecuencia
        productosViewModel.detalleProducto.observe(viewLifecycleOwner) { detalleProducto ->
            if (detalleProducto.isNotEmpty()) {
                val producto = detalleProducto[0]
                actualizarCampos(producto)
            }else{
                cargarSiguienteProducto()
            }
        }
        productosViewModel.mensajeToast.observe(viewLifecycleOwner) {
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

        // Indica la posición del producto para abrir el producto seleccionado
        viewModel.actualizarPosiscion(posicionProducto!!)
        verificarPosiciones()

        // Carga el producto en la UI
        cargarProducto(modeloProducto)

        binding!!.editTextPCompra.escribirFormatoMoneda()
        binding!!.editTextPVenta.escribirFormatoMoneda()

        return binding!!.root // Retorna la vista inflada
    }

    //Tomamos la foto resultante de la camara o la galeria y la colocamos en el imageview
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Si la acción fue tomar una foto con la cámara
        if (requestCode == TomarFotoYGaleria.CAMARA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            // Recortar la imagen usando la biblioteca CropImage
            CropImage.activity(TomarFotoYGaleria.imagenUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(requireContext(), this)
        }

        // Si la acción fue elegir una imagen de la galería
        if (requestCode == TomarFotoYGaleria.GALERIA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Obtener la URI de la imagen seleccionada de la galería
            val uri=  data?.data
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

//    var  verificarImagenCambiada:ImageView?=null
    private fun actualizarCampos(producto: ModeloProducto) {
        id_producto=producto.id
        binding?.editTextProducto?.setText(producto.nombre)
        binding?.editTextPCompra?.setText(producto.p_compra)
        binding?.editTextPVenta?.setText(producto.p_diamante)
        binding?.editTextCantidad?.setText(producto.cantidad)
       if (!producto.url.isEmpty()){
           Picasso.get().load(producto.url).into(binding?.imageViewFoto)
//           Picasso.get().load(producto.url).into(verificarImagenCambiada)
       }

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
        OcultarTeclado(requireContext()).hideKeyboard(vista!!)
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
        OcultarTeclado(requireContext()).hideKeyboard(vista!!)
    }


    private fun cargarProducto(modeloProducto: ModeloProducto?) {
        binding?.imageViewFoto?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_menu_camera))

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
                val imageHandler = TomarFotoYGaleria(this)
                imageHandler.cargarImagen()
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
        OcultarTeclado(requireContext()).hideKeyboard(vista!!)

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

     @SuppressLint("SuspiciousIndentation")
     fun guardar() {

        OcultarTeclado(requireContext()).hideKeyboard(vista!!)

        //verificando campos vacios
        if (id_producto.isEmpty() || this.binding!!.editTextProducto.text.toString().isEmpty()
            || this.binding!!.editTextCantidad.text.toString().trim().isEmpty()|| this.binding!!.editTextPCompra.text.toString().trim().isEmpty()
            || this.binding!!.editTextPVenta.text.toString().trim().isEmpty()){

            val snackbar= Snackbar.make(vista!!, "Todos los datos son obligatorios", Snackbar.LENGTH_LONG)
            snackbar.view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rojo))
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            snackbar.show()
            return
        }
         //veficicar si hay imagen cargada
         if (this.binding?.imageViewFoto!!.drawable is BitmapDrawable) {
             viewModel.subirImagenFirebase(requireContext(),this.binding?.imageViewFoto!!)
         }
        val updates = hashMapOf<String, Any>(
            "id" to id_producto.trim(),
            "nombre" to this.binding!!.editTextProducto.text.toString().trim(),
            "cantidad" to this.binding!!.editTextCantidad.text.toString().trim(),
            "p_compra" to this.binding!!.editTextPCompra.text.toString().eliminarPuntosComas().trim(),
            "p_diamante" to this.binding!!.editTextPVenta.text.toString().eliminarPuntosComas().trim()

        )

          viewModel.guardarProducto(updates)

         val verificarConexion= VerificarInternet()

         if (!verificarConexion.verificarConexion(requireContext())){
             Toast.makeText(requireContext(),getString(R.string.disponbleEnlaNuebe),Toast.LENGTH_LONG).show()
         }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
    }

}