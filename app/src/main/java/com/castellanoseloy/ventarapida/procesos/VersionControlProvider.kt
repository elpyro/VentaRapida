package com.castellanoseloy.ventarapida.procesos

import android.util.Log
import com.castellanoseloy.ventarapida.datos.VersionModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase

class VersionControlProvider {

    // Cambiar a la referencia de la base de datos en tiempo real
    private val database = FirebaseDatabase.getInstance().reference.child("Configuracion")

    // Método para obtener la versión actual desde la base de datos en tiempo real
    fun getVersionActual(): Task<DataSnapshot> {
        // Utilizar addOnCompleteListener para manejar el resultado de manera asincrónica
        val taskCompletionSource = TaskCompletionSource<DataSnapshot>()
        database.keepSynced(true)
        // Agregar un listener para obtener la instantánea de datos
        database.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Éxito: completar la tarea con la instantánea de datos
                taskCompletionSource.setResult(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Error: completar la tarea con una excepción
                taskCompletionSource.setException(databaseError.toException())
            }
        })

        return taskCompletionSource.task
    }

}
