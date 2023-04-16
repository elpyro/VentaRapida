package com.example.ventarapida.ui.procesos

import android.content.Context
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*


object UtilidadesFirebase {

    private const val PRODUCTOS_REF = "Productos"


    fun guardarProducto(updates: HashMap<String, Any>): Task<Void> {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(PRODUCTOS_REF).child(updates["id"] as String)
        return registroRef.updateChildren(updates)
    }

    fun CambiarCantidad(context: Context, id_transaccion: String , id_producto:String, cantidad: String){
        val database = FirebaseDatabase.getInstance()
        val productosRef = database.getReference(PRODUCTOS_REF)

        val idProducto = id_producto
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

                val preferenciaModificarCantidad=PreferenciaModificarCantidad()
                    preferenciaModificarCantidad.eliminarRegistroPreferencia(context,id_transaccion)

                }
            }
        })

    }

}