package com.example.ventarapida.procesos

import com.example.ventarapida.datos.ModeloProductoFacturado
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseProductoFacturadosOComprados {

    //las tablas referencias son ProductosComprados y ProductosFacturados

    fun guardarProductoFacturado(tablaReferencia: String,listaProductosFacturados: ArrayList<ModeloProductoFacturado>): Task<Void> {
        val database = FirebaseDatabase.getInstance()
        val referencia = database.getReference(tablaReferencia)

        val updates = HashMap<String, Any>()
        for (producto in listaProductosFacturados) {
            val idProductoPedido = producto.id_producto_pedido
            val update = producto
            updates[idProductoPedido] = update
        }

        return referencia.updateChildren(updates)
    }

    fun eliminarProductoFacturado(tablaReferencia: String, listaProductosFacturados: ArrayList<ModeloProductoFacturado>) {
        val database = FirebaseDatabase.getInstance()
        val referencia = database.getReference(tablaReferencia)

        for (producto in listaProductosFacturados) {
            val id_producto = producto.id_producto_pedido
            referencia.child(id_producto).removeValue()
        }
        return
    }

    fun actualizarPrecioDescuento(idPedido:String, descuento:Double){
        val database = FirebaseDatabase.getInstance()
        val refProductosFacturados = database.getReference("ProductosFacturados")

        val porcentajeDescuento = descuento / 100


        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (registro in dataSnapshot.children) {
                    val productoFacturado = registro.getValue(ModeloProductoFacturado::class.java)
                    if (productoFacturado != null && productoFacturado.id_pedido == idPedido) {
                        // Actualiza el valor del campo "precio_descuento" en el registro
                        val nuevoValor = productoFacturado.venta.toInt()
                        val productoConDescuento=nuevoValor * (1 - porcentajeDescuento)
                        refProductosFacturados.child(registro.key!!).child("precioDescuentos").setValue(productoConDescuento.toString())
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Maneja el error aquí si es necesario
            }
        }

        refProductosFacturados.orderByChild("id_pedido").equalTo(idPedido).addListenerForSingleValueEvent(listener)

    }
}