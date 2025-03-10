@file:Suppress("DEPRECATION")

package com.castellanoseloy.ventarapida.ui.configuracion.datosEmpresa


import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.FragmentDatosEmpresaBinding
import com.castellanoseloy.ventarapida.datos.ModeloDatosEmpresa
import com.castellanoseloy.ventarapida.procesos.FirebaseDatosEmpresa.guardarDatosEmpresa
import com.castellanoseloy.ventarapida.procesos.GuardarImagenEnDispositivo
import com.castellanoseloy.ventarapida.procesos.TomarFotoYGaleria
import com.castellanoseloy.ventarapida.procesos.Utilidades.ocultarTeclado
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.servicios.ServiciosSubirFoto
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class DatosEmpresa : Fragment() {

    private var binding: FragmentDatosEmpresaBinding? = null
    private lateinit var vista: View
    private var imageUri: Uri? = null

    // Registro del recorte de imagen
    private val cropImageLauncher =
        registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                val uriCropped = result.uriContent
                if (uriCropped != null) {
                    binding?.imageViewFotoEmpresa?.setImageURI(uriCropped)
                }
            } else {
                Toast.makeText(requireContext(), "Error al recortar la imagen", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDatosEmpresaBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista = view
        setHasOptionsMenu(true)

        if (DatosPersitidos.datosEmpresa.id.isNotEmpty()) cargarDatos()
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

        if (DatosPersitidos.datosEmpresa.url.isNotEmpty()) {
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
        return when (item.itemId) {
            R.id.action_guardar -> {
                guardarDatos()
                true
            }
            R.id.action_camara -> {
                val imageHandler = TomarFotoYGaleria(this)
                imageHandler.cargarImagen()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun guardarDatos() {
        ocultarTeclado(requireContext(), vista)
        val empresaID = DatosPersitidos.datosEmpresa.id

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
            .addOnSuccessListener {
                DatosPersitidos.datosEmpresa = ModeloDatosEmpresa(
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

                DatosPersitidos.editText_nombreEmpresa.text = DatosPersitidos.datosEmpresa.nombre

                if (DatosPersitidos.datosEmpresa.url.isNotEmpty()) {
                    Picasso.get().load(DatosPersitidos.datosEmpresa.url)
                        .into(DatosPersitidos.logotipo)
                    DatosPersitidos.logotipo.setImageDrawable(DatosPersitidos.logotipo.drawable)
                }

                Toast.makeText(requireContext(), "Datos Actualizados", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
    }

    private fun guardarImagen(empresaID: String) {
        if (binding?.imageViewFotoEmpresa?.drawable is BitmapDrawable) {
            val bitmap = (binding?.imageViewFotoEmpresa?.drawable as BitmapDrawable).bitmap
            val storageRef = Firebase.storage.reference.child("$empresaID.jpg")

            val guardarImagenEnDispositivo = GuardarImagenEnDispositivo()
            val fileUri = guardarImagenEnDispositivo.guardarImagenEnDispositivo(requireContext(), bitmap)

            val intent = Intent(context, ServiciosSubirFoto::class.java).apply {
                putExtra("fileUri", fileUri)
                putExtra("storageRef", storageRef.toString())
                putExtra("idProducto", empresaID)
                putExtra("tablaReferencia", "DatosEmpresa")
            }

            JobIntentService.enqueueWork(
                requireContext(),
                ServiciosSubirFoto::class.java,
                DatosPersitidos.JOB_ID,
                intent
            )
        }
    }

    // Nuevo manejo de imágenes
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TomarFotoYGaleria.CAMARA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = TomarFotoYGaleria.imagenUri
            iniciarRecorte(imageUri)
        }

        if (requestCode == TomarFotoYGaleria.GALERIA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            iniciarRecorte(imageUri)
        }
    }

    private fun iniciarRecorte(uri: Uri?) {
        uri?.let {
            val options = CropImageOptions().apply {
                guidelines = CropImageView.Guidelines.ON
                // setAspectRatio(1, 1) // Si deseas un recorte cuadrado
            }
            cropImageLauncher.launch(CropImageContractOptions(it, options))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().invalidateOptionsMenu()
        binding = null
    }
}
