package com.castellanoseloy.cataplus.servicios
import android.content.Context
import android.content.Intent
import android.net.Uri

import androidx.core.app.JobIntentService
import com.castellanoseloy.cataplus.datos.Quatruple
import com.castellanoseloy.cataplus.procesos.Preferencias
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ServiciosSubirFoto : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        // Obtener los datos del Intent
        var fileUri = intent.getParcelableExtra<Uri>("fileUri")
        val fileUriString = fileUri.toString()
        val storageRefString = intent.getStringExtra("storageRef")
        val idProducto = intent.getStringExtra("idProducto")
        val tablaReferencia = intent.getStringExtra("tablaReferencia")


        agregarServicioPendiente(applicationContext,fileUriString,storageRefString,idProducto, tablaReferencia)

        val serviciosPendientes = getServiciosPendientes(applicationContext)

        serviciosPendientes.forEach { servicio ->
            val fileUri = servicio.first
            val storageRefString = servicio.second
            val idProducto = servicio.third
            val tablaReferencia=servicio.fourth

            guardarServicioPendiente(applicationContext, fileUri, storageRefString, idProducto,tablaReferencia)
        }
    }

     fun guardarServicioPendiente(context: Context, fileUri: String?, storageRefString: String?, idImagen: String?, tablaReferencia: String?) {
            //Tambien puede almacenar el logo de la empresa

         if (fileUri != null && storageRefString != null && idImagen != null) {

            val storageRef = Firebase.storage.reference.child("imagenes").child("$idImagen.jpg")
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
                    if(tablaReferencia.equals("DatosEmpresa")){
                        val preferencias= Preferencias()
                        preferencias.preferenciasConfiguracion(context)
                        val database = FirebaseDatabase.getInstance()
                        val registroRef = database.getReference("DatosEmpresa").child(idImagen)


                        registroRef.updateChildren(updates).
                        addOnSuccessListener {
                            borrarServicioPendiente(context, idImagen)

                            //actualiza las preferencias si cambia la imagen de la empresa

                        }
                    }else{
                        val database = FirebaseDatabase.getInstance()
                        val registroRef = database.getReference(DatosPersitidos.datosEmpresa.id).child(tablaReferencia!!).child(idImagen)


                        registroRef.updateChildren(updates).
                        addOnSuccessListener {
                            borrarServicioPendiente(context, idImagen)

                            //actualiza las preferencias si cambia la imagen de la empresa

                        }

                    }



                }
            }
        }

    }

    // Obtener la lista de imágenes pendientes de las preferencias compartidas
    fun getServiciosPendientes(context: Context): List<Quatruple<String?, String?, String?, String?>> {
        val prefs = context.getSharedPreferences("PreferenciaSubirFotos", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("imagenes_pendientes", null)
        return if (jsonString != null) {
            val typeToken = object : TypeToken<List<Quatruple<String?, String?, String?, String?>>>() {}.type
            Gson().fromJson(jsonString, typeToken)
        } else {
            emptyList()
        }
    }


    // Agregar una imagen pendiente a la lista

    fun agregarServicioPendiente(context: Context, fileUri: String?, storageRefString: String?, idProducto: String?, tablaReferencia:String?) {
        val serviciosPendientes = getServiciosPendientes(context).toMutableList()
        serviciosPendientes.add(Quatruple(fileUri, storageRefString, idProducto, tablaReferencia))
        val jsonString = Gson().toJson(serviciosPendientes)
        val prefs = context.getSharedPreferences("PreferenciaSubirFotos", Context.MODE_PRIVATE)
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
            val prefs = context.getSharedPreferences("PreferenciaSubirFotos", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("imagenes_pendientes", jsonString)
            editor.commit()
        }
    }


}