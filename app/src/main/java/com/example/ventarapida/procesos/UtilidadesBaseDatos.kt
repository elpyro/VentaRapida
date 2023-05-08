package com.example.ventarapida.procesos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.ventarapida.baseDatos.MyDatabaseHelper
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.datos.ModeloTransaccionSumaRestaProducto
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import java.util.*

object UtilidadesBaseDatos {

    fun eliminarColaSubidaCantidadProducto(context: Context, id: String) {
        //si el producto subio con exito en la nube borrarlo de la base de datos
        val dbHelper = MyDatabaseHelper(context)
        val db = dbHelper.writableDatabase
        db.delete("transaccionesSumaRestaProductos", "idTransaccion = ?", arrayOf(id))
        db.close()
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

    fun editarProductoTransaccion(context:Context, tipo: String, diferenciaCantidad:Int, productoFacturado:ModeloProductoFacturado) {
        val dbHelper = MyDatabaseHelper(context!!)
        val db = dbHelper.readableDatabase
        var multiplicador = 1
        if (tipo == "compra") multiplicador = -1

        if (diferenciaCantidad != 0) {
            val sumarORestar = diferenciaCantidad * multiplicador

            val idTransaccion = UUID.randomUUID().toString()
            val values = ContentValues().apply {
                put("idTransaccion", idTransaccion)
                put("idProducto", productoFacturado.id_producto)
                put("cantidad", sumarORestar.toString())
            }
            // Guardamos la referencia en la base de datos para cambiar la cantidad del producto
            db.insert("transaccionesSumaRestaProductos", null, values)
        }
    }


    fun obtenerTransaccionesSumaRestaProductos(context: Context?): List<ModeloTransaccionSumaRestaProducto> {
        val dbHelper = MyDatabaseHelper(context!!)
        val db = dbHelper.writableDatabase
        val transacciones = mutableListOf<ModeloTransaccionSumaRestaProducto>()
        val cursor = db.rawQuery("SELECT * FROM transaccionesSumaRestaProductos", null)
        cursor.use {
            while (it.moveToNext()) {
                val idTransaccion = it.getString(it.getColumnIndexOrThrow("idTransaccion"))
                val idProducto = it.getString(it.getColumnIndexOrThrow("idProducto"))
                val cantidad = it.getString(it.getColumnIndexOrThrow("cantidad"))
                val transaccion = ModeloTransaccionSumaRestaProducto(idTransaccion, idProducto, cantidad)
                transacciones.add(transaccion)
            }
        }
        return transacciones
    }

}