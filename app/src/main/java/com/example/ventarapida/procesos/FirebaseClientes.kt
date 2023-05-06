package com.example.ventarapida.procesos

import android.util.Log
import com.example.ventarapida.datos.ModeloClientes
import com.example.ventarapida.datos.ModeloFactura
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseClientes {

    private const val TABLA_REFERENCIA = "Clientes"

    fun buscarTodosClientes(): Task<MutableList<ModeloClientes>> {
        val database = FirebaseDatabase.getInstance()
        val tablaRef = database.getReference(TABLA_REFERENCIA)

        val clientes = mutableListOf<ModeloClientes>()
        val taskCompletionSource = TaskCompletionSource<MutableList<ModeloClientes>>()

        tablaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (facturaSnapshot in snapshot.children) {
                    val factura = facturaSnapshot.getValue(ModeloClientes::class.java)
                    factura?.let {
                        clientes.add(it)
                    }
                }

                taskCompletionSource.setResult(clientes)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MiApp", "Error al buscar facturas: ${error.message}")
                taskCompletionSource.setException(error.toException())
            }
        })

        return taskCompletionSource.task
    }

    fun guardarCliente(updates: HashMap<String, Any>): Task<Void> {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(TABLA_REFERENCIA).child(updates["id"] as String)
        return registroRef.updateChildren(updates)
    }

    fun eliminarCliente(id: String): Task<Void> {
        val database2 = FirebaseDatabase.getInstance()
        val registroRef = database2.getReference(TABLA_REFERENCIA).child(id)

        return registroRef.removeValue()
    }


}