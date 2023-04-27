package com.example.ventarapida.procesos

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.ventarapida.datos.ModeloFactura
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseFactura {
    // las tablas de referencia pueden ser Factura o Compra
    private const val TABLA_REFERENCIA = "Factura"

    fun guardarFactura(tablaReferencia:String ,updates: HashMap<String, Any>): Task<Void> {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(tablaReferencia).child(updates["id_pedido"] as String)
        return registroRef.updateChildren(updates)
    }

    fun eliminarFactura(tablaReferencia: String, id_pedido: String): Task<Void> {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(tablaReferencia).child(id_pedido)
        return registroRef.removeValue()
    }



    fun buscarFacturas(tablaReferencia: String): Task<MutableList<ModeloFactura>> {
        val database = FirebaseDatabase.getInstance()
        val tablaRef = database.getReference(tablaReferencia)

        val facturas = mutableListOf<ModeloFactura>()
        val taskCompletionSource = TaskCompletionSource<MutableList<ModeloFactura>>()

        tablaRef.keepSynced(true)
        tablaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (facturaSnapshot in snapshot.children) {
                    val factura = facturaSnapshot.getValue(ModeloFactura::class.java)
                    factura?.let {
                        facturas.add(it)
                    }
                }

                taskCompletionSource.setResult(facturas)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MiApp", "Error al buscar facturas: ${error.message}")
                taskCompletionSource.setException(error.toException())
            }
        })

        return taskCompletionSource.task
    }


}