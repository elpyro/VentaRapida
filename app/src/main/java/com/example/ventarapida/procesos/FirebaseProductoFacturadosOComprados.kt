package com.example.ventarapida.procesos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.ventarapida.baseDatos.MyDatabaseHelper
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.UtilidadesBaseDatos.crearTransaccionBD
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

object FirebaseProductoFacturadosOComprados {

    //las tablas referencias son ProductosComprados y ProductosFacturados

    fun guardarProductoFacturado(
        tablaReferencia: String,
        listaProductosFacturados: MutableList<ModeloProductoFacturado>,
        tipo: String,
        context: Context
    ) {
        val database = FirebaseDatabase.getInstance()
        val referencia = database.getReference(tablaReferencia)

        val dbHelper = MyDatabaseHelper(context!!)
        val db = dbHelper.readableDatabase

        val updates = HashMap<String, Any>()

        for (producto in listaProductosFacturados) {
            val idProductoPedido = producto.id_producto_pedido
            val update = producto
            updates[idProductoPedido] = update

            //creamos una transccion de para restarlos luego a la cantidad de productos
            if(tipo!="edicion")  crearTransaccionBD(producto, tipo, db)

        }

         referencia.updateChildren(updates)
         db.close()
    }



    fun eliminarProductoFacturado(
        tablaReferencia: String,
        listaProductosFacturados: MutableList<ModeloProductoFacturado>,
        context: Context,
        tipo:String
    ) {
        val database = FirebaseDatabase.getInstance()
        val referencia = database.getReference(tablaReferencia)

        val dbHelper = MyDatabaseHelper(context!!)
        val db = dbHelper.readableDatabase

        for (producto in listaProductosFacturados) {
            val id_producto = producto.id_producto_pedido
            referencia.child(id_producto).removeValue()

            crearTransaccionBD(producto, tipo, db)

        }
        db.close()
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

    fun buscarProductosPorPedido(tablaReferencia: String, idPedido: String): Task<List<ModeloProductoFacturado>> {
        val taskCompletionSource = TaskCompletionSource<List<ModeloProductoFacturado>>()
        val database = FirebaseDatabase.getInstance()
        val productosRef = database.getReference(tablaReferencia).orderByChild("id_pedido").equalTo(idPedido)

        productosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val datosFactura = mutableListOf<ModeloProductoFacturado>()
                for (facturaSnapshot in dataSnapshot.children) {
                    val factura = facturaSnapshot.getValue(ModeloProductoFacturado::class.java)
                    factura?.let { datosFactura.add(it) }
                }
                taskCompletionSource.setResult(datosFactura)
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setException(error.toException())
            }
        })

        return taskCompletionSource.task
    }


}