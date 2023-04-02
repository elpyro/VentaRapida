package com.example.ventarapida.ui.process
import android.content.Context
import android.content.Intent
import android.net.Uri

import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.JobIntentService
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UploadService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        // Obtener los datos del Intent
        var fileUri = intent.getParcelableExtra<Uri>("fileUri")
        val fileUriString = fileUri.toString()
        val storageRefString = intent.getStringExtra("storageRef")
        val idProducto = intent.getStringExtra("idProducto")

//        borrarServicioPendiente(applicationContext, idProducto.toString())
        agregarServicioPendiente(applicationContext,fileUriString,storageRefString,idProducto)

        val serviciosPendientes = getServiciosPendientes(applicationContext)

        serviciosPendientes.forEach { servicio ->
            val fileUri = servicio.first
            val storageRefString = servicio.second
            val idProducto = servicio.third

            guardarServicioPendiente(applicationContext, fileUri, storageRefString, idProducto)
        }
    }

     fun guardarServicioPendiente(context: Context, fileUri: String?, storageRefString: String?, idProducto: String?) {
        if (fileUri != null && storageRefString != null && idProducto != null) {
            // Crear una referencia al almacenamiento de Firebase
            val storageRef = Firebase.storage.getReferenceFromUrl(storageRefString)
            val subirArchivo=Uri.parse(fileUri)
            // Subir el archivo al servidor
            val uploadTask = storageRef.putFile(subirArchivo)
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

                    registroRef.updateChildren(updates).
                    addOnSuccessListener {
                        borrarServicioPendiente(context, idProducto)
                    }

                }
            }
        }

    }

    // Obtener la lista de imágenes pendientes de las preferencias compartidas
    fun getServiciosPendientes(context: Context): List<Triple<String?, String?, String?>> {
        val prefs = context.getSharedPreferences("servicio_pendiente", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("imagenes_pendientes", null)
        return if (jsonString != null) {
            val typeToken = object : TypeToken<List<Triple<String?, String?, String?>>>() {}.type
            Gson().fromJson(jsonString, typeToken)
        } else {
            emptyList()
        }
    }


    // Agregar una imagen pendiente a la lista
    fun agregarServicioPendiente(context: Context, fileUri: String?, storageRefString: String?, idProducto: String?) {
        val serviciosPendientes = getServiciosPendientes(context).toMutableList()
        serviciosPendientes.add(Triple(fileUri, storageRefString, idProducto))
        val jsonString = Gson().toJson(serviciosPendientes)
        val prefs = context.getSharedPreferences("servicio_pendiente", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("imagenes_pendientes", jsonString)
        editor.commit()
    }



    // Borrar una imagen pendiente de la lista
    fun borrarServicioPendiente(context: Context, id: String) {
        val serviciosPendientes = getServiciosPendientes(context).toMutableList()
        val servicioAEliminar = serviciosPendientes.firstOrNull { it.third == id }
        if (servicioAEliminar != null) {
            serviciosPendientes.remove(servicioAEliminar)
            val jsonString = Gson().toJson(serviciosPendientes)
            val prefs = context.getSharedPreferences("servicio_pendiente", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("imagenes_pendientes", jsonString)
            editor.commit()
        }
    }


}