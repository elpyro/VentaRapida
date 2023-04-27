package com.example.ventarapida.procesos

import android.content.Context
import android.widget.Toast
import com.example.ventarapida.datos.ModeloTransaccionSumaRestaProducto
import com.example.ventarapida.procesos.UtilidadesBaseDatos.eliminarColaSubidaCantidadProducto
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*


object FirebaseProductos {

    private const val TABLA_REFERENCIA = "Productos"


    fun guardarProducto(updates: HashMap<String, Any>): Task<Void> {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(TABLA_REFERENCIA).child(updates["id"] as String)
        return registroRef.updateChildren(updates)
    }

    fun transaccionesCambiarCantidad(context: Context?, solicitudes: List<ModeloTransaccionSumaRestaProducto>){
        val database = FirebaseDatabase.getInstance()
        val productosRef = database.getReference(TABLA_REFERENCIA)

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

}