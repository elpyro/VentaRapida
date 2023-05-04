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


    fun buscarTodosClientes(): Task<MutableList<ModeloClientes>> {
        val database = FirebaseDatabase.getInstance()
        val tablaRef = database.getReference("Clientes")

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

}