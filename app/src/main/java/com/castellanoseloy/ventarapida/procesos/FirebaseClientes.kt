package com.castellanoseloy.ventarapida.procesos

import android.util.Log
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.datos.ModeloClientes
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
        val tablaRef = database.getReference(DatosPersitidos.datosEmpresa.id).child(TABLA_REFERENCIA)

        val clientes = mutableListOf<ModeloClientes>()
        val taskCompletionSource = TaskCompletionSource<MutableList<ModeloClientes>>()
        tablaRef.keepSynced(true)
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

    fun guardarCliente(updates: HashMap<String, Any>) {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(DatosPersitidos.datosEmpresa.id).child(TABLA_REFERENCIA).child(updates["id"] as String)
        registroRef.keepSynced(true)
        registroRef.updateChildren(updates)
    }

    fun eliminarCliente(id: String) {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(DatosPersitidos.datosEmpresa.id).child(TABLA_REFERENCIA).child(id)
        registroRef.keepSynced(true)
        registroRef.removeValue()
    }


}