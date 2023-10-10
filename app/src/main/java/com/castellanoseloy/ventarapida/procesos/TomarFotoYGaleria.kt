@file:Suppress("DEPRECATION")

package com.castellanoseloy.ventarapida.procesos

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment



import java.io.File

class TomarFotoYGaleria(private val fragment: Fragment) {

    companion object {
        lateinit var imagenUri: Uri
        val GALERIA_REQUEST_CODE = 1001
        val CAMARA_REQUEST_CODE = 1002
        val REQUEST_IMAGE_CAPTURE = 1003
    }
    fun cargarImagen() {
        val builder = AlertDialog.Builder(fragment.requireContext())
        builder.setTitle("Selecciona una opción")
        builder.setItems(arrayOf("Tomar foto", "Elegir de galería")) { dialog, which ->
            when (which) {
                0 -> tomarFoto()
                1 -> elegirDeGaleria()
            }
        }
        builder.create().show()
    }

    private fun tomarFoto() {
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(fragment.requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
        } else {
            val photoFile = File(fragment.requireContext().getExternalFilesDir(null), "CompraRapidita.jpg")

            imagenUri = FileProvider.getUriForFile(fragment.requireContext(), "com.castellanoseloy.ventarapida.fileprovider", photoFile)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
            fragment.startActivityForResult(intent, CAMARA_REQUEST_CODE)
        }
    }

    private fun elegirDeGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        fragment.startActivityForResult(intent, GALERIA_REQUEST_CODE)
    }


}
