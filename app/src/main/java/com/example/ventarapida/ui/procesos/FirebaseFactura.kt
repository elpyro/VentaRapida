package com.example.ventarapida.ui.procesos

import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase

object FirebaseFactura {

    private const val TABLA_REFERENCIA = "Factura"

    fun guardarFactura(updates: HashMap<String, Any>): Task<Void> {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(TABLA_REFERENCIA).child(updates["id_pedido"] as String)
        return registroRef.updateChildren(updates)
    }
}