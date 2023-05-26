package com.example.ventarapida.procesos

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.ventarapida.MainActivity
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloTransaccionSumaRestaProducto
import com.example.ventarapida.procesos.UtilidadesBaseDatos.eliminarColaSubidaCantidadProducto
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.*
import java.util.concurrent.CompletableFuture


object FirebaseProductos {

    private const val TABLA_REFERENCIA = "Productos"


    fun guardarProducto(updates: HashMap<String, Any>) {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(MainActivity.datosEmpresa.id).child(TABLA_REFERENCIA).child(updates["id"] as String)
        registroRef.keepSynced(true)
        registroRef.updateChildren(updates)
    }

    fun transaccionesCambiarCantidad(context: Context?, solicitudes: List<ModeloTransaccionSumaRestaProducto>){
        val database = FirebaseDatabase.getInstance()
        val productosRef = database.getReference(MainActivity.datosEmpresa.id).child(TABLA_REFERENCIA)
        productosRef.keepSynced(true)
        solicitudes.forEach { solicitud ->
            val idTransaccion = solicitud.idTransaccion
            val idProducto = solicitud.idProducto
            val cantidad = solicitud.cantidad

            val cantidadActualRef = productosRef.child(idProducto).child("cantidad")
            cantidadActualRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val cantidadActual = mutableData.getValue(String::class.java)?.toInt() ?: return Transaction.success(mutableData)

                    mutableData.value = (cantidadActual  - cantidad.toInt()).toString() // Actualizar la cantidad en la base de datos
                    return Transaction.success(mutableData)
                }

                override fun onComplete(
                    databaseError: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (databaseError != null) {
                        // Error al actualizar la cantidad
                        Toast.makeText(context, "Error al actualizar la cantidad del producto", Toast.LENGTH_SHORT).show()
                    } else {

                        eliminarColaSubidaCantidadProducto(context!!, idTransaccion) // eliminar registro con id

                    }
                }
            })
        }
    }

    fun buscarProductos(mayorCero: Boolean): Task<MutableList<ModeloProducto>> {
        val database = FirebaseDatabase.getInstance()
        val tablaRef = database.getReference(MainActivity.datosEmpresa.id).child(TABLA_REFERENCIA)

        val productos = mutableListOf<ModeloProducto>()
        val taskCompletionSource = TaskCompletionSource<MutableList<ModeloProducto>>()
        tablaRef.keepSynced(true)
        tablaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (facturaSnapshot in snapshot.children) {
                    val factura = facturaSnapshot.getValue(ModeloProducto::class.java)
                    if (mayorCero){

                        factura?.let {
                            if (it.cantidad.toInt() > 0) { // Filtrar productos con cantidad mayor a 0
                                productos.add(factura)
                            }
                        }
                    }else{
                        productos.add(factura!!)
                    }
                }

                taskCompletionSource.setResult(productos)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MiApp", "Error al buscar facturas: ${error.message}")
                taskCompletionSource.setException(error.toException())
            }
        })

        return taskCompletionSource.task
    }


}