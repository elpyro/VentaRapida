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
import android.os.Handler
import com.example.ventarapida.procesos.Utilidades.verificarPermisosAdministrador

object FirebaseFacturaOCompra {

    // las tablas de referencia pueden ser Factura o Compra

    fun guardarDetalleFacturaOCompra(tablaReferencia:String, updates: HashMap<String, Any>) {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(MainActivity.datosEmpresa.id).child(tablaReferencia).child(updates["id_pedido"] as String)
        registroRef.keepSynced(true)
        registroRef.updateChildren(updates)
    }

    fun eliminarFacturaOCompra(tablaReferencia: String, id_pedido: String) {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(MainActivity.datosEmpresa.id).child(tablaReferencia).child(id_pedido)
        registroRef.keepSynced(true)
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
                // Hacer una espera de 1 segundo antes de realizar la segunda consulta
                Handler().postDelayed({
                    tablaRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (facturaSnapshot in snapshot.children) {
                                val factura = facturaSnapshot.getValue(ModeloFactura::class.java)
                                factura?.let {
                                    if(verificarPermisosAdministrador()){
                                        facturas.add(it)
                                    }else {
                                        if (factura.id_vendedor.equals(MainActivity.datosUsuario.id)){
                                            facturas.add(it)
                                        } else {
                                        }
                                    }

                                }
                            }
                            taskCompletionSource.setResult(facturas)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w("MiApp", "Error al buscar facturas: ${error.message}")
                            taskCompletionSource.setException(error.toException())
                        }
                    })
                }, 1500) // Esperar 1 segundo (1000 milisegundos) antes de realizar la segunda consulta
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
        tablaRef.keepSynced(true)
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