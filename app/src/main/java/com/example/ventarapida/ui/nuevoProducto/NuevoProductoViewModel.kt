package com.example.ventarapida.ui.nuevoProducto

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.ImageView
import androidx.core.app.JobIntentService.enqueueWork
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity
import com.example.ventarapida.ui.process.UploadService
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class NuevoProductoViewModel : ViewModel() {

    fun guardarProducto(updates: HashMap<String, Any>):  Task<Void>  {
        val id = updates["id"] as String?
        val database2 = FirebaseDatabase.getInstance()
        val registroRef = database2.getReference("Productos").child(id!!)
        return registroRef.updateChildren(updates)
    }

    // Crear una función para guardar los datos del servicio pendiente en una preferencia

    companion object {


        fun subirImagenFirebase(context: Context, imageViewFoto: ImageView?, idProducto: String): Task<Void> {

            // Obtener la imagen del ImageView como Bitmap
            val bitmap = (imageViewFoto?.drawable as BitmapDrawable).bitmap

            // Crear una referencia a la ubicación donde se subirá la imagen en Firebase Storage
            val storageRef = Firebase.storage.reference.child(idProducto + ".jpg")

            // Obtener la URI del archivo temporal
            val fileUri = guardarImagenEnDispositivo(context, bitmap)

            // Crear el Intent para iniciar el servicio
            val intent = Intent(context, UploadService::class.java)
            intent.putExtra("fileUri", fileUri)
            intent.putExtra("storageRef", storageRef.toString())
            intent.putExtra("idProducto", idProducto)


           // Iniciar el servicio en segundo plano utilizando JobIntentService
            enqueueWork(context, UploadService::class.java, MainActivity.JOB_ID, intent)

            return TaskCompletionSource<Void>().task // Devuelve una tarea vacía
        }
    }
}
        private fun guardarImagenEnDispositivo(context: Context, bitmap: Bitmap): Uri? {
            // Crear un archivo temporal en el almacenamiento interno
            val file = File.createTempFile(
                "tempImagen",
                ".jpg",
                context.cacheDir
            )

            // Convertir el bitmap a un archivo jpeg
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
            val byteArray = stream.toByteArray()

            // Escribir el archivo jpeg en el archivo temporal
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(byteArray)
            fileOutputStream.flush()
            fileOutputStream.close()

            // Obtener la URI del archivo temporal
            return Uri.fromFile(file)
        }


