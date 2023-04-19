package com.example.ventarapida.ui.procesos

import com.example.ventarapida.ui.datos.ModeloProductoFacturado
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase

object FirebaseProductoFacturados {

    private const val TABLA_REFERENCIA = "ProductosFacturados"

    fun guardarProductoFacturado(listaProductosFacturados: ArrayList<ModeloProductoFacturado>): Task<Void> {
        val database = FirebaseDatabase.getInstance()
        val referencia = database.getReference(TABLA_REFERENCIA)

        val updates = HashMap<String, Any>()
        for (producto in listaProductosFacturados) {
            val idProductoPedido = producto.id_producto_pedido
            val update = producto
            updates[idProductoPedido] = update
        }

        return referencia.updateChildren(updates)
    }
}