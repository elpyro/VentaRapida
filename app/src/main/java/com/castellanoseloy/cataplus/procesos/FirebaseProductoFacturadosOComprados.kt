package com.castellanoseloy.cataplus.procesos

import android.content.Context

import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.baseDatos.MyDatabaseHelper
import com.castellanoseloy.cataplus.datos.ModeloProductoFacturado
import com.castellanoseloy.cataplus.procesos.UtilidadesBaseDatos.crearTransaccionBD
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


object FirebaseProductoFacturadosOComprados {

    //las tablas referencias son ProductosComprados y ProductosFacturados


    fun guardarProductoFacturado(
        tablaReferencia: String,
        listaProductosFacturados: MutableList<ModeloProductoFacturado>,
        tipo: String,
        context: Context
    ) {
        val database = FirebaseDatabase.getInstance()
        val referencia = database.getReference(DatosPersitidos.datosEmpresa.id).child(tablaReferencia)
        referencia.keepSynced(true)

        val dbHelper = MyDatabaseHelper(context)
        val db = dbHelper.readableDatabase

        val updates = HashMap<String, Any>()

        for (producto in listaProductosFacturados) {
            val idProductoPedido = producto.id_producto_pedido
            val update = producto
            updates[idProductoPedido] = update

            //creamos una transccion de para restarlos luego a la cantidad de productos
            if(tipo!="edicion") {
                crearTransaccionBD(producto, tipo, db)
            }

        }

         referencia.updateChildren(updates)
         db.close()
    }



    fun eliminarProductoFacturado(
        tablaReferencia: String,
        listaProductosFacturados: MutableList<ModeloProductoFacturado>,
        context: Context,
        tipo: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val referencia = database.getReference(DatosPersitidos.datosEmpresa.id).child(tablaReferencia)
        referencia.keepSynced(true)

        var contador = 0

        for (producto in listaProductosFacturados) {
            val id_producto = producto.id_producto_pedido
            referencia.child(id_producto).removeValue()

            contador++
        }

        if (contador == listaProductosFacturados.size) {
            DatosPersitidos.progressDialog?.dismiss()
        }



    }

    fun actualizarPrecioDescuento(idPedido:String, descuento:Double){
        val database = FirebaseDatabase.getInstance()
        val refProductosFacturados = database.getReference(DatosPersitidos.datosEmpresa.id).child("ProductosFacturados")
        refProductosFacturados.keepSynced(true)
        val porcentajeDescuento = descuento / 100


        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (registro in dataSnapshot.children) {
                    val productoFacturado = registro.getValue(ModeloProductoFacturado::class.java)
                    if (productoFacturado != null && productoFacturado.id_pedido == idPedido) {
                        // Actualiza el valor del campo "precio_descuento" en el registro
                        val nuevoValor = productoFacturado.venta.toDouble()
                        val productoConDescuento=nuevoValor * (1 - porcentajeDescuento)
                        refProductosFacturados.child(registro.key!!).child("precioDescuentos").setValue(productoConDescuento.toString())
                        refProductosFacturados.child(registro.key!!).child("porcentajeDescuento").setValue(descuento.toString())
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Maneja el error aquí si es necesario
            }
        }
        refProductosFacturados.keepSynced(true)
        refProductosFacturados.orderByChild("id_pedido").equalTo(idPedido).addListenerForSingleValueEvent(listener)

    }

    fun buscarProductosPorPedido(tablaReferencia: String, idPedido: String): Task<List<ModeloProductoFacturado>> {
        val taskCompletionSource = TaskCompletionSource<List<ModeloProductoFacturado>>()
        val database = FirebaseDatabase.getInstance()
        val productosRef = database.getReference(DatosPersitidos.datosEmpresa.id).child(tablaReferencia).orderByChild("id_pedido").equalTo(idPedido)
        productosRef.keepSynced(true)
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


    fun buscarProductosFacturadosPorId(tablaReferencia: String, idProducto: String): Task<List<ModeloProductoFacturado>> {
        val taskCompletionSource = TaskCompletionSource<List<ModeloProductoFacturado>>()
        val database = FirebaseDatabase.getInstance()

        val productosRef = database.getReference(DatosPersitidos.datosEmpresa.id).child(tablaReferencia).orderByChild("id_producto").equalTo(idProducto)
        productosRef.keepSynced(true)
        productosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val datosFactura = mutableListOf<ModeloProductoFacturado>()
                for (facturaSnapshot in dataSnapshot.children) {
                    val factura = facturaSnapshot.getValue(ModeloProductoFacturado::class.java)
                    factura!!.tipoOperacion =if(tablaReferencia.equals("ProductosComprados"))"Surtido" else "Vendido"
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


    fun buscarProductosPorFecha(fechaInicio: Long, fechaFin: Long, idVendedor:String, tabla:String): Task<List<ModeloProductoFacturado>> {

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = database.getReference(DatosPersitidos.datosEmpresa.id).child(tabla)

        val tcs = TaskCompletionSource<List<ModeloProductoFacturado>>()
        reference.keepSynced(true)
        reference.orderByChild("fechaBusquedas")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // esperamos 2 segundo y volvemos a hacer la misma busqueda para estar seguro que si se obtienen los valores actualizados
                    Utilidades.esperarUnSegundo()
                    Utilidades.esperarUnSegundo()
                    reference.orderByChild("fechaBusquedas")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val productos = mutableListOf<ModeloProductoFacturado>()

                                for (snapshot in dataSnapshot.children) {
                                    val elemento = snapshot.getValue(ModeloProductoFacturado::class.java)

                                    // Obtener la fecha del elemento actual
                                    val fechaElemento = elemento?.fechaBusquedas ?: 0

                                    // Verificar si la fecha está dentro del rango deseado
                                    if (fechaElemento in fechaInicio..fechaFin) {
                                        //evalua si el producto es del vendedor especificado
                                        if(idVendedor=="false") {
                                            if(!elemento!!.productoEditado.equals("Inventario Editado")) productos.add(elemento!!)
                                        }else if (idVendedor.equals(elemento?.id_vendedor)){
                                            if(!elemento!!.productoEditado.equals("Inventario Editado")) productos.add(elemento!!)
                                        }
                                    }
                                }

                                productos.sortWith(compareBy<ModeloProductoFacturado> { it.fechaBusquedas }.thenBy { it.id_pedido })
                                tcs.setResult(productos)
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                tcs.setException(databaseError.toException())
                            }
                        })


                }

                override fun onCancelled(databaseError: DatabaseError) {
                    tcs.setException(databaseError.toException())
                }
            })

        return tcs.task
    }


}