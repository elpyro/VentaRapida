package com.example.ventarapida.ui.nuevoProducto

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
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentNuevoProductoBinding

import com.example.ventarapida.procesos.FirebaseProductos.guardarProducto
import com.example.ventarapida.procesos.TomarFotoYGaleria
import com.example.ventarapida.procesos.TomarFotoYGaleria.Companion.CAMARA_REQUEST_CODE
import com.example.ventarapida.procesos.TomarFotoYGaleria.Companion.GALERIA_REQUEST_CODE
import com.example.ventarapida.procesos.TomarFotoYGaleria.Companion.imagenUri
import com.example.ventarapida.procesos.Utilidades.eliminarPuntosComasLetras
import com.example.ventarapida.procesos.Utilidades.escribirFormatoMoneda
import com.example.ventarapida.procesos.Utilidades.ocultarTeclado
import com.example.ventarapida.procesos.VerificarInternet
import com.google.android.material.snackbar.Snackbar
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
                // Realiza alguna validación, por ejemplo, si el contenido no es válido
                  binding?.inputEditTextCantidad?.error = "Puedes usar SURTIR MI NEGOCIO para agregar"

            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return binding!!.root // Retorna la vista inflada
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista = view // Guarda la vista en la variable vista

        viewModel = ViewModelProvider(this).get(NuevoProductoViewModel::class.java)

        viewModel.mensajeToast.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        setHasOptionsMenu(true)

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_guardar_y_foto, menu)
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

            //verificando campos vacios
            if ( binding!!.editTextProducto.text.toString().isEmpty()
                || binding!!.editTextCantidad.text.toString().trim().isEmpty()|| binding!!.editTextPCompra.text.toString().trim().isEmpty()
                ||binding!!.editTextPVenta.text.toString().trim().isEmpty()){

                val snackbar= Snackbar.make(vista, "Todos los datos son obligatorios", Snackbar.LENGTH_LONG)
                snackbar.view.setBackgroundColor(resources.getColor(R.color.rojo))
                snackbar.setTextColor(resources.getColor(R.color.white))
                snackbar.show()
                return
            }

        val idProducto = UUID.randomUUID().toString()

        //subir imagen
        if (binding?.imageViewFoto!!.drawable is BitmapDrawable) {
            NuevoProductoViewModel.subirImagenFirebase(requireContext(), binding?.imageViewFoto!!, idProducto)
                .addOnFailureListener {
                    Toast.makeText(requireContext(),"Error al obtener la URL de descarga de la imagen subida.",Toast.LENGTH_LONG).show()
                }
            }
        //subir datos
            val updates = hashMapOf<String, Any>(
                "id" to idProducto,
                "nombre" to binding!!.editTextProducto.text.toString().trim(),
                "cantidad" to binding!!.editTextCantidad.text.toString().trim(),
                "p_compra" to binding!!.editTextPCompra.text.toString(),
                "p_diamante" to binding!!.editTextPVenta.text.toString(),
                "comentario" to binding!!.editTextComentario.text.toString().trim(),
                "proveedor" to binding!!.editTextProveedor.text.toString()
            )

        guardarProducto(updates)

                Toast.makeText(requireContext(),"Producto Guardado",Toast.LENGTH_LONG).show()

        //limpiando campos
        binding?.editTextProducto?.setText("")
        binding?.editTextCantidad?.setText("0")
        binding?.editTextPCompra?.setText("")
        binding?.editTextPVenta?.setText("")
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