package com.castellanoseloy.ventarapida.ui.nuevoProducto

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.core.app.JobIntentService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.castellanoseloy.ventarapida.MainActivity

import com.castellanoseloy.ventarapida.procesos.GuardarImagenEnDispositivo
import com.castellanoseloy.ventarapida.servicios.ServiciosSubirFoto
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class NuevoProductoViewModel : ViewModel() {

    val mensajeToast = MutableLiveData<String>()


    // Crear una función para guardar los datos del servicio pendiente en una preferencia

    companion object {


        fun subirImagenFirebase(context: Context, imageViewFoto: ImageView?, idProducto: String): Task<Void> {

            // Obtener la imagen del ImageView como Bitmap
            val bitmap = (imageViewFoto?.drawable as BitmapDrawable).bitmap

            // Crear una referencia a la ubicación donde se subirá la imagen en Firebase Storage
            val storageRef = Firebase.storage.reference.child(idProducto + ".jpg")


            val guardarImagenEnDispositivo= GuardarImagenEnDispositivo()
            val fileUri = guardarImagenEnDispositivo.guardarImagenEnDispositivo(context ,bitmap)


            // Crear el Intent para iniciar el servicio
            val intent = Intent(context, ServiciosSubirFoto::class.java)
            intent.putExtra("fileUri", fileUri)
            intent.putExtra("storageRef", storageRef.toString())
            intent.putExtra("idProducto", idProducto)
            intent.putExtra("tablaReferencia", "Productos")

            // Iniciar el servicio en segundo plano utilizando JobIntentService
            JobIntentService.enqueueWork(
                context,
                ServiciosSubirFoto::class.java,
                MainActivity.JOB_ID,
                intent
            )


            return TaskCompletionSource<Void>().task // Devuelve una tarea vacía
        }
    }
}
