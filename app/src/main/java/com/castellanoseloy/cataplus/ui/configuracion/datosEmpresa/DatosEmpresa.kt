@file:Suppress("DEPRECATION")

package com.castellanoseloy.cataplus.ui.configuracion.datosEmpresa

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.R

import com.castellanoseloy.cataplus.databinding.FragmentDatosEmpresaBinding
import com.castellanoseloy.cataplus.datos.ModeloDatosEmpresa
import com.castellanoseloy.cataplus.procesos.FirebaseDatosEmpresa.guardarDatosEmpresa
import com.castellanoseloy.cataplus.procesos.GuardarImagenEnDispositivo
import com.castellanoseloy.cataplus.procesos.TomarFotoYGaleria
import com.castellanoseloy.cataplus.procesos.Utilidades.ocultarTeclado
import com.castellanoseloy.cataplus.servicios.ServiciosSubirFoto
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.launch
import java.io.File

class DatosEmpresa : Fragment() {

    private var binding: FragmentDatosEmpresaBinding? = null
    private lateinit var vista: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDatosEmpresaBinding.inflate(inflater, container, false)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view

        setHasOptionsMenu(true)
        if(DatosPersitidos.datosEmpresa.id!="")cargarDatos()

    }

    private fun cargarDatos() {
        binding?.editTextEmpresa?.setText(DatosPersitidos.datosEmpresa.nombre)
        binding?.editTextId?.setText(DatosPersitidos.datosEmpresa.documento)
        binding?.editTextPagina?.setText(DatosPersitidos.datosEmpresa.pagina)
        binding?.editTextCorreo?.setText(DatosPersitidos.datosEmpresa.correo)
        binding?.editTextTelefono1?.setText(DatosPersitidos.datosEmpresa.telefono1)
        binding?.editTextTelefono2?.setText(DatosPersitidos.datosEmpresa.telefono2)
        binding?.editTextDireccion?.setText(DatosPersitidos.datosEmpresa.direccion)
        binding?.editTextGarantia?.setText(DatosPersitidos.datosEmpresa.garantia)
        if (DatosPersitidos.datosEmpresa.url.isNotEmpty()){
            Picasso.get().load(DatosPersitidos.datosEmpresa.url).into(binding?.imageViewFotoEmpresa)
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

                guardarDatos()

                return true
            }
            R.id.action_camara->{
                val imageHandler = TomarFotoYGaleria(this)

                imageHandler.cargarImagen()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun guardarDatos() {
        ocultarTeclado(requireContext(),vista)
        val empresaID =DatosPersitidos.datosEmpresa.id

        val updates = hashMapOf(
            "id" to empresaID,
            "nombre" to binding?.editTextEmpresa?.text.toString(),
            "documento" to binding?.editTextId?.text.toString(),
            "pagina" to binding?.editTextPagina?.text.toString(),
            "correo" to binding?.editTextCorreo?.text.toString(),
            "telefono1" to binding?.editTextTelefono1?.text.toString(),
            "telefono2" to binding?.editTextTelefono2?.text.toString(),
            "direccion" to binding?.editTextDireccion?.text.toString(),
            "garantia" to binding?.editTextGarantia?.text.toString(),
        )

        lifecycleScope.launch {
            guardarImagen(empresaID)
        }

        guardarDatosEmpresa(updates)
            //actualizamos los datos actuales
            .addOnSuccessListener {
                val empresa = ModeloDatosEmpresa(
                    id = empresaID,
                    nombre = binding?.editTextEmpresa?.text.toString(),
                    documento = binding?.editTextId?.text.toString(),
                    pagina = binding?.editTextPagina?.text.toString(),
                    correo = binding?.editTextCorreo?.text.toString(),
                    telefono1 = binding?.editTextTelefono1?.text.toString(),
                    telefono2 = binding?.editTextTelefono2?.text.toString(),
                    direccion = binding?.editTextDireccion?.text.toString(),
                    garantia = binding?.editTextGarantia?.text.toString()
                )
                DatosPersitidos.datosEmpresa=empresa

                DatosPersitidos.editText_nombreEmpresa.text = DatosPersitidos.datosEmpresa.nombre

                if (DatosPersitidos.datosEmpresa.url.isNotEmpty()){
                    Picasso.get().load(DatosPersitidos.datosEmpresa.url).into(DatosPersitidos.logotipo)
                    DatosPersitidos.logotipo.setImageDrawable(DatosPersitidos.logotipo.drawable)
                }

                Toast.makeText(requireContext(),"Datos Actualizados",Toast.LENGTH_LONG).show()


                findNavController().popBackStack()
            }




    }

    private fun guardarImagen(empresaID:String) {
        // Obtener la imagen del ImageView como Bitmap

        if (binding?.imageViewFotoEmpresa!!.drawable is BitmapDrawable) {

            val bitmap = (binding?.imageViewFotoEmpresa?.drawable as BitmapDrawable).bitmap

            // Crear una referencia a la ubicación donde se subirá la imagen en Firebase Storage
            val storageRef = Firebase.storage.reference.child("$empresaID.jpg")


            val guardarImagenEnDispositivo= GuardarImagenEnDispositivo()
            val fileUri = guardarImagenEnDispositivo.guardarImagenEnDispositivo(requireContext() ,bitmap)

            // Crear el Intent para iniciar el servicio
            val intent = Intent(context, ServiciosSubirFoto::class.java)
            intent.putExtra("fileUri", fileUri)
            intent.putExtra("storageRef", storageRef.toString())
            intent.putExtra("idProducto", empresaID)
            intent.putExtra("tablaReferencia", "DatosEmpresa")


            // Iniciar el servicio en segundo plano utilizando JobIntentService
            JobIntentService.enqueueWork(
                requireContext(),
                ServiciosSubirFoto::class.java,
                DatosPersitidos.JOB_ID,
                intent)
        }


    }

    //Tomamos la foto resultante de la camara y la colocamos en el imageview
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Si la acción fue tomar una foto con la cámara
        if (requestCode == TomarFotoYGaleria.CAMARA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            // Recortar la imagen usando la biblioteca CropImage
            CropImage.activity(TomarFotoYGaleria.imagenUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                //.setAspectRatio(1, 1)
                .start(requireContext(), this)

        }

        // Si la acción fue elegir una imagen de la galería
        if (requestCode == TomarFotoYGaleria.GALERIA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
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
                    binding?.imageViewFotoEmpresa?.setImageBitmap(bitmap)
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                // Mostrar un mensaje de error si la recortada no fue exitosa
                val error = result.error
                Toast.makeText(requireContext(), "Error al recortar la imagen: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
        binding = null
    }

}