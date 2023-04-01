package com.example.ventarapida.ui.process
import android.content.Context
import android.content.Intent
import android.net.Uri

import android.widget.ImageView
import androidx.core.app.JobIntentService
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class UploadService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        // Obtener los datos del Intent
        val fileUri = intent.getParcelableExtra<Uri>("fileUri")
        val storageRefString = intent.getStringExtra("storageRef")
        val idProducto = intent.getStringExtra("idProducto")

        guardarServicioPendiente(applicationContext,fileUri,storageRefString,idProducto)


        if (fileUri != null && storageRefString != null && idProducto != null) {
            // Crear una referencia al almacenamiento de Firebase
            val storageRef = Firebase.storage.getReferenceFromUrl(storageRefString)

            // Subir el archivo al servidor
            val uploadTask = storageRef.putFile(fileUri)
            uploadTask.addOnSuccessListener {
                // Obtener la URL de descarga de la imagen subida
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Aquí puedes usar la URL para mostrar la imagen en tu app, o para guardarla en tu base de datos
                    val url = uri.toString()

                    val updates = hashMapOf<String, Any>(
                        "url" to url.trim(),
                    )

                    val database = FirebaseDatabase.getInstance()
                    val registroRef = database.getReference("Productos").child(idProducto)

                    registroRef.updateChildren(updates)
                    borrarServicioPendiente(applicationContext)
                }
            }
        }
    }

    fun guardarServicioPendiente(context: Context, fileUri: Uri?, storageRefString: String?, idProducto: String?) {
        val prefs = context.getSharedPreferences("servicio_pendiente", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        if (fileUri != null) {
            editor.putString("fileUri", fileUri.toString())
        }
        if (storageRefString != null) {
            editor.putString("storageRef", storageRefString)
        }
        if (idProducto != null) {
            editor.putString("idProducto", idProducto)
        }
        editor.apply()
    }



    // Crear una función para borrar los datos del servicio pendiente de la preferencia
    fun borrarServicioPendiente(context: Context) {
        val prefs = context.getSharedPreferences("servicio_pendiente", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}