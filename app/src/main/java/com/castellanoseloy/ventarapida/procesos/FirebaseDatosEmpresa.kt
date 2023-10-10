package com.castellanoseloy.ventarapida.procesos

import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseDatosEmpresa {
    private const val TABLA_REFERENCIA = "DatosEmpresa"

    fun guardarDatosEmpresa(updates: HashMap<String, String>) : Task<Void> {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(TABLA_REFERENCIA).child(updates["id"] as String)
        registroRef.keepSynced(true) // Activar la persistencia

        return registroRef.updateChildren(updates as Map<String, Any>)
    }


    fun obtenerDatosEmpresa(id: String, listener: ValueEventListener) {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(TABLA_REFERENCIA).child(id)
        registroRef.keepSynced(true)
        registroRef.addListenerForSingleValueEvent(listener)
    }
}