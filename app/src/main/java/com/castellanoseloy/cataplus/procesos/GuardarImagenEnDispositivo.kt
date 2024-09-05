package com.castellanoseloy.cataplus.procesos

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class GuardarImagenEnDispositivo {

     fun guardarImagenEnDispositivo(context: Context, bitmap: Bitmap): Uri? {
        // Crear un archivo temporal en el almacenamiento interno
        val file = File.createTempFile(
            "tempImagen",
            ".jpg",
            context.cacheDir
        )

        // Convertir el bitmap a un archivo jpeg
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, stream)
        val byteArray = stream.toByteArray()

        // Escribir el archivo jpeg en el archivo temporal
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(byteArray)
        fileOutputStream.flush()
        fileOutputStream.close()

        // Obtener la URI del archivo temporal
        return Uri.fromFile(file)
    }
}