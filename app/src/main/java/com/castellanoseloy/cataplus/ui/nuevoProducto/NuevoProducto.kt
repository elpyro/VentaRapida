package com.castellanoseloy.cataplus.ui.nuevoProducto

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.castellanoseloy.cataplus.R
import com.castellanoseloy.cataplus.databinding.FragmentNuevoProductoBinding
import com.castellanoseloy.cataplus.datos.ModeloProducto
import com.castellanoseloy.cataplus.procesos.FirebaseProductos.guardarProducto
import com.castellanoseloy.cataplus.procesos.TomarFotoYGaleria
import com.castellanoseloy.cataplus.procesos.TomarFotoYGaleria.Companion.CAMARA_REQUEST_CODE
import com.castellanoseloy.cataplus.procesos.TomarFotoYGaleria.Companion.GALERIA_REQUEST_CODE
import com.castellanoseloy.cataplus.procesos.TomarFotoYGaleria.Companion.imagenUri
import com.castellanoseloy.cataplus.procesos.Utilidades.ocultarTeclado
import com.castellanoseloy.cataplus.procesos.VerificarInternet
import com.castellanoseloy.cataplus.ui.promts.PromtAgregarVariante
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.util.*


@Suppress("DEPRECATION")
class NuevoProducto : Fragment() {


    private var informacion: Boolean? = false
    private lateinit var producto: ModeloProducto
    var binding: FragmentNuevoProductoBinding? = null
    private lateinit var viewModel: NuevoProductoViewModel
    private lateinit var vista:View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNuevoProductoBinding.inflate(inflater, container, false)

        val bundle = arguments
        informacion = bundle?.getBoolean("primerProducto")
        if(informacion==true){
            crearAlertDialogo()
        }

        binding?.imageViewFoto?.setOnClickListener{
            cargarImagen()
        }

        binding?.editTextCantidad?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString().isEmpty()){
                    binding?.textViewInformacionAgregarCantidades?.visibility=View.GONE
                }else{
                    binding?.textViewInformacionAgregarCantidades?.visibility=View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        marcarCamposObligatorios()
        return binding!!.root // Retorna la vista inflada
    }

    private fun crearAlertDialogo() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("¡Crea tus productos!")
        builder.setIcon(R.drawable.logo2_compra_rapidita)
        builder.setMessage(
            "¡Ya estás dentro!\n\n" +
                    "Primero crea tu un producto.\n\n" +
                    "Ingresa el [Nombre del producto], el [Precio Compra] y el [Precio Venta]. Lo ultilizaremos para calcular la rentabilidad de tu negocio.\n\n" +
                    "Solo si el producto tiene  variantes distintas como: modelo, tamaño o color, puedes agregarlas aquí."
        )
        builder.setPositiveButton("¡Entendido!") { dialog, which ->
            // Acciones después de hacer clic en "Entendido"
        }

        builder.show()

    }

    private fun marcarCamposObligatorios() {
        binding!!.editTextProducto.error = "Obligatorio"

        binding!!.editTextPCompra.error = "Obligatorio"

        binding!!.editTextPVenta.error = "Obligatorio"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista = view // Guarda la vista en la variable vista

        viewModel = ViewModelProvider(this).get(NuevoProductoViewModel::class.java)

        setHasOptionsMenu(true)

        producto = ModeloProducto()

        binding?.buttonAgregarVariantes?.setOnClickListener {
            promtVariente()
        }
    }

    private fun promtVariente() {
        val promptAgregarVariante = PromtAgregarVariante()
        promptAgregarVariante.agregar(
            requireContext(),
            producto.listaVariables,
            null
        ) { listaActualizada ->
            producto.listaVariables = listaActualizada

            val gridLayoutManager = GridLayoutManager(requireContext(), 1)
            binding!!.recyclerVariantes.layoutManager = gridLayoutManager
            var adaptador = VariantesAdaptador(listaActualizada)
            binding?.recyclerVariantes?.adapter = adaptador
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_guardar_y_foto, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
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
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun cargarImagen() {
        val imageHandler = TomarFotoYGaleria(this)
        imageHandler.cargarImagen()
    }

    //Tomamos la foto resultante de la camara y la colocamos en el imageview
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Si la acción fue tomar una foto con la cámara
        if (requestCode == CAMARA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            // Recortar la imagen usando la biblioteca CropImage
            CropImage.activity(imagenUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                //.setAspectRatio(1, 1)
                .start(requireContext(), this)
        }

        // Si la acción fue elegir una imagen de la galería
        if (requestCode == GALERIA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Obtener la URI de la imagen seleccionada de la galería
            val uri=  data?.data
            // Recortar la imagen usando la biblioteca CropImage
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                //.setAspectRatio(1, 1)
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

    private fun guardar() {

            ocultarTeclado(requireContext(),vista)

        if(binding!!.editTextProducto.text.toString().isEmpty()){
            binding!!.editTextProducto.error = "Obligatorio"
            return
        }
        if(binding!!.editTextPCompra.text.toString().trim().isEmpty()){
            binding!!.editTextPCompra.error = "Obligatorio"
            return
        }
        if(binding!!.editTextPVenta.text.toString().trim().isEmpty()){
            binding!!.editTextPVenta.error = "Obligatorio"
            return
        }



        val idProducto = UUID.randomUUID().toString()

        //subir imagen
        if (binding?.imageViewFoto!!.drawable is BitmapDrawable) {
            viewModel.subirImagenFirebase(requireContext(), binding?.imageViewFoto!!, idProducto)
                .addOnFailureListener {
                    Toast.makeText(requireContext(),"Error al obtener la URL de descarga de la imagen subida.",Toast.LENGTH_LONG).show()
                }
            }
        var cantidadDisponible="0"

        if(binding!!.editTextCantidad.text.toString().trim().isNotEmpty()) cantidadDisponible=binding!!.editTextCantidad.text.toString().trim()
        //subir datos

        // Actualizar el producto con los valores del formulario
        producto = producto.copy(
            cantidad = cantidadDisponible,
            nombre = binding!!.editTextProducto.text.toString().trim(),
            p_compra = binding!!.editTextPCompra.text.toString(),
            p_diamante = binding!!.editTextPVenta.text.toString(),
            id = idProducto,
            comentario = binding!!.editTextComentario.text.toString().trim(),
            proveedor = binding!!.editTextProveedor.text.toString()
        )

        // Obtener el HashMap de actualizaciones usando getUpdates
        val updates = producto.getUpdates()

        Log.d("ModeloProducto", "Updates: $updates")
        // Guardar el producto
        guardarProducto(updates)

        Toast.makeText(requireContext(),"Producto Guardado",Toast.LENGTH_LONG).show()

        //limpiando campos
        binding?.editTextProducto?.setText("")
        binding?.editTextCantidad?.setText("")
        binding?.editTextPCompra?.setText("")
        binding?.editTextPVenta?.setText("")
        binding?.editTextComentario?.setText("")
        binding?.editTextProveedor?.setText("")
        binding?.imageViewFoto?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_menu_camera))


        producto.listaVariables = emptyList()

        var adaptador = VariantesAdaptador(emptyList())
        binding?.recyclerVariantes?.adapter = adaptador


        val verificarConexion= VerificarInternet()

        if (!verificarConexion.verificarConexion(requireContext())){
            Toast.makeText(requireContext(),getString(R.string.disponbleEnlaNuebe),Toast.LENGTH_LONG).show()
        }
        if (informacion == true){
            crearAlertDialogoSurtir()
        }

        }

    private fun crearAlertDialogoSurtir() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("¡Ya creaste un producto!")
        builder.setIcon(R.drawable.logo2_compra_rapidita)
        builder.setMessage(
            "¡Necesitamos surtirlo al inventario!\n\n" +
                    "Para agregar cantidades de un producto debes surtirlo.\n" +
                    "Cada vez que surtes un producto, se sumará a las existencias y cuando lo vendas, se restará de las existencias.\n\n"+
                    "Los productos surtidos  sumarán su [Precio Compra] a tu inventario.\n"



        )
        builder.setPositiveButton("¡Ir a surtir inventario!") { dialog, which ->
            // Acciones después de hacer clic en "¡Ir al panel de surtir inventario!"
            Navigation.findNavController(vista!!).popBackStack()

            Navigation.findNavController(vista!!).navigate(R.id.compra)

        }
        builder.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
        binding = null
    }


}