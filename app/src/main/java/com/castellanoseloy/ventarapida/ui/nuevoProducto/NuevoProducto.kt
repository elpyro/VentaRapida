package com.castellanoseloy.ventarapida.ui.nuevoProducto

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.FragmentNuevoProductoBinding
import com.castellanoseloy.ventarapida.procesos.FirebaseProductos.guardarProducto
import com.castellanoseloy.ventarapida.procesos.TomarFotoYGaleria
import com.castellanoseloy.ventarapida.procesos.TomarFotoYGaleria.Companion.CAMARA_REQUEST_CODE
import com.castellanoseloy.ventarapida.procesos.TomarFotoYGaleria.Companion.GALERIA_REQUEST_CODE
import com.castellanoseloy.ventarapida.procesos.TomarFotoYGaleria.Companion.imagenUri
import com.castellanoseloy.ventarapida.procesos.Utilidades.ocultarTeclado
import com.castellanoseloy.ventarapida.procesos.VerificarInternet
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.util.*


@Suppress("DEPRECATION")
class NuevoProducto : Fragment() {


    var binding: FragmentNuevoProductoBinding? = null
    private lateinit var viewModel: NuevoProductoViewModel
    private lateinit var vista:View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNuevoProductoBinding.inflate(inflater, container, false)

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
            val updates = hashMapOf<String, Any>(
                "id" to idProducto,
                "nombre" to binding!!.editTextProducto.text.toString().trim(),
                "cantidad" to cantidadDisponible,
                "p_compra" to binding!!.editTextPCompra.text.toString(),
                "p_diamante" to binding!!.editTextPVenta.text.toString(),
                "comentario" to binding!!.editTextComentario.text.toString().trim(),
                "proveedor" to binding!!.editTextProveedor.text.toString()
            )

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



        val verificarConexion= VerificarInternet()

        if (!verificarConexion.verificarConexion(requireContext())){
            Toast.makeText(requireContext(),getString(R.string.disponbleEnlaNuebe),Toast.LENGTH_LONG).show()
        }
        }
    override fun onDestroyView() {
        super.onDestroyView()
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
        binding = null
    }

}