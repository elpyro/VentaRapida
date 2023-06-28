package com.example.ventarapida.procesos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.ventarapida.baseDatos.MyDatabaseHelper
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.datos.ModeloTransaccionSumaRestaProducto
import java.util.*

object UtilidadesBaseDatos {

    fun eliminarColaSubida(context: Context, id: String) {
        val dbHelper = MyDatabaseHelper(context)
        val db = dbHelper.readableDatabase
        val contentValues = ContentValues().apply {
            put("subido", "true")
        }
        val whereClause = "idTransaccion = ?"
        val whereArgs = arrayOf(id)
        db.update("transaccionesSumaRestaProductos", contentValues, whereClause, whereArgs)
        db.close()

//        val dbHelper = MyDatabaseHelper(context)
//        val db = dbHelper.writableDatabase
//
//        val selection = "idTransaccion = ?"
//        val selectionArgs = arrayOf(id)
//        //TODO DEBERIA BORRAR 1 SOLO REGISTRO Y NO TODOS A LA VEZ
//        db.delete("transaccionesSumaRestaProductos", selection, selectionArgs)
//
//        db.close()
    }



    fun guardarTransaccionesBd(
        tipo: String,
        context: Context?,
        listaProductosFacturados: MutableList<ModeloProductoFacturado>
    ) {
        val dbHelper = MyDatabaseHelper(context!!)
        val db = dbHelper.readableDatabase


        listaProductosFacturados.forEach { modeloProductoFacturado ->
            var multiplicador = 1
            if (tipo == "compra") multiplicador = -1

            if (modeloProductoFacturado.cantidad.toInt() != 0) {
                val sumarORestar = modeloProductoFacturado.cantidad.toInt() * multiplicador

                val idTransaccion = UUID.randomUUID().toString()
                val values = ContentValues().apply {
                    put("idTransaccion", idTransaccion)
                    put("idProducto", modeloProductoFacturado.id_producto)
                    put("cantidad", sumarORestar.toString())
                }
                // Guardamos la referencia en la base de datos para cambiar la cantidad del producto
                db.insert("transaccionesSumaRestaProductos", null, values)

            }
        }

        db.close()
    }

    //las transacciones se ejecutaran en segundo plano para actualizar las cantidades de los productos
    //en firebase
    fun crearTransaccionBD(
        modeloProductoFacturado: ModeloProductoFacturado,
        tipo: String,
        db: SQLiteDatabase
    ) {


        var multiplicador = 1
        if (tipo == "compra") multiplicador = -1

        if (modeloProductoFacturado.cantidad.toInt() != 0) {
            val sumarORestar = modeloProductoFacturado.cantidad.toInt() * multiplicador

            val idTransaccion = modeloProductoFacturado.id_producto_pedido
            val values = ContentValues().apply {
                put("idTransaccion", idTransaccion)
                put("idProducto", modeloProductoFacturado.id_producto)
                put("cantidad", sumarORestar.toString())
                put("subido", "false")
            }
            // Guardamos la referencia en la base de datos para cambiar la cantidad del producto
            db.insert("transaccionesSumaRestaProductos", null, values)
        }

    }
    fun marcarTransaccionComoSubida(idTransaccion: String, context: Context) {
        val dbHelper = MyDatabaseHelper(context)
        val db = dbHelper.readableDatabase
        val contentValues = ContentValues().apply {
            put("subido", "true")
        }
        val whereClause = "idTransaccion = ?"
        val whereArgs = arrayOf(idTransaccion)
        db.update("transaccionesSumaRestaProductos", contentValues, whereClause, whereArgs)
        db.close()
    }
    fun editarProductoTransaccion(context:Context, tipo: String, diferenciaCantidad:Int, productoFacturado:ModeloProductoFacturado) {
        val dbHelper = MyDatabaseHelper(context)
        val db = dbHelper.readableDatabase
        var multiplicador = 1
        if (tipo == "compra") multiplicador = -1

        val listaEditarInventario = arrayListOf<ModeloTransaccionSumaRestaProducto>()

        if (diferenciaCantidad != 0) {
            val sumarORestar = diferenciaCantidad * multiplicador

            val idTransaccion = UUID.randomUUID().toString()
            val values = ContentValues().apply {
                put("idTransaccion", idTransaccion)
                put("idProducto", productoFacturado.id_producto)
                put("cantidad", sumarORestar.toString())
                put("subido", "false")
            }

            // Guardamos la referencia en la base de datos para cambiar la cantidad del producto
            db.insert("transaccionesSumaRestaProductos", null, values)


            val sumarProducto = ModeloTransaccionSumaRestaProducto(
                idTransaccion = idTransaccion,
                idProducto =productoFacturado.id_producto,
                cantidad = sumarORestar.toString(),
                subido ="false"
            )

            listaEditarInventario.add(sumarProducto)

            //ejecutamos la transaccion
            FirebaseProductos.transaccionesCambiarCantidad(context, listaEditarInventario)
        }
    }


    fun obtenerTransaccionesSumaRestaProductos(context: Context?): List<ModeloTransaccionSumaRestaProducto> {
        val dbHelper = MyDatabaseHelper(context!!)
        val db = dbHelper.readableDatabase
        val transacciones = mutableListOf<ModeloTransaccionSumaRestaProducto>()

        val selection = "subido = ?"
        val selectionArgs = arrayOf("false")
        val cursor = db.query("transaccionesSumaRestaProductos", null, selection, selectionArgs, null, null, null)

        cursor.use {
            while (it.moveToNext()) {
                val idTransaccion = it.getString(it.getColumnIndexOrThrow("idTransaccion"))
                val idProducto = it.getString(it.getColumnIndexOrThrow("idProducto"))
                val cantidad = it.getString(it.getColumnIndexOrThrow("cantidad"))
                val subido=it.getString(it.getColumnIndexOrThrow("subido"))
                val transaccion = ModeloTransaccionSumaRestaProducto(idTransaccion, idProducto, cantidad, subido)
                transacciones.add(transaccion)
            }
        }

        db.close()
        return transacciones
    }
}