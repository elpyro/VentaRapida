package com.example.ventarapida.procesos

import android.util.Log
import com.example.ventarapida.MainActivity
import com.example.ventarapida.datos.ModeloFactura
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseFacturaOCompra {

    // las tablas de referencia pueden ser Factura o Compra

    fun guardarDetalleFacturaOCompra(tablaReferencia:String, updates: HashMap<String, Any>) {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(MainActivity.datosEmpresa.id).child(tablaReferencia).child(updates["id_pedido"] as String)
        registroRef.updateChildren(updates)
    }

    fun eliminarFacturaOCompra(tablaReferencia: String, id_pedido: String) {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(MainActivity.datosEmpresa.id).child(tablaReferencia).child(id_pedido)
        registroRef.removeValue()
    }

    fun buscarFacturasOCompra(tablaReferencia: String): Task<MutableList<ModeloFactura>> {
        val database = FirebaseDatabase.getInstance()
        val tablaRef = database.getReference(MainActivity.datosEmpresa.id).child(tablaReferencia)

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

    fun buscarFacturaOCompraPorId(tablaReferencia: String, facturaId: String): Task<ModeloFactura?> {
        val database = FirebaseDatabase.getInstance()
        val tablaRef = database.getReference(MainActivity.datosEmpresa.id).child(tablaReferencia)

        val taskCompletionSource = TaskCompletionSource<ModeloFactura?>()

        tablaRef.child(facturaId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val factura = snapshot.getValue(ModeloFactura::class.java)
                taskCompletionSource.setResult(factura)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MiApp", "Error al buscar factura: ${error.message}")
                taskCompletionSource.setException(error.toException())
            }
        })

        return taskCompletionSource.task
    }

}