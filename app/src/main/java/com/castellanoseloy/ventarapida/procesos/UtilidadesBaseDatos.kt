package com.castellanoseloy.ventarapida.procesos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.castellanoseloy.ventarapida.baseDatos.MyDatabaseHelper
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.datos.ModeloTransaccionSumaRestaProducto
import com.castellanoseloy.ventarapida.datos.Variable
import java.util.*

object UtilidadesBaseDatos {

    fun eliminarColaSubida(context: Context, id: String) {
        val dbHelper = MyDatabaseHelper(context)
        val db = dbHelper.writableDatabase

        val contentValues = ContentValues().apply {
            put("subido", "true")
        }

        val whereClause = "idTransaccion = ?"
        val whereArgs = arrayOf(id)

        val query = "SELECT * FROM transaccionesSumaRestaProductos WHERE $whereClause LIMIT 1"
        val cursor = db.rawQuery(query, whereArgs)

        if (cursor.moveToFirst()) {
            val idColumnIndex = cursor.getColumnIndexOrThrow("idTransaccion")
            val idValue = cursor.getString(idColumnIndex)
            db.delete("transaccionesSumaRestaProductos", "idTransaccion = ?", arrayOf(idValue))
        }

        cursor.close()
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
                put("listaVariables", modeloProductoFacturado.convertirListaVariablesToString(
                    modeloProductoFacturado.listaVariables ?: emptyList()
                ))
            }
            // Guardamos la referencia en la base de datos para cambiar la cantidad del producto
            db.insert("transaccionesSumaRestaProductos", null, values)
        }

    }

    fun editarProductoTransaccion(context:Context, tipo: String, diferenciaCantidad:Int, idProducto:String, modeloProductoFacturado: ModeloProductoFacturado, cambiosVariable: Boolean = false) {
        val dbHelper = MyDatabaseHelper(context)
        val db = dbHelper.readableDatabase
        var multiplicador = 1
        if (tipo == "compra") multiplicador = -1

        val listaEditarInventario = arrayListOf<ModeloTransaccionSumaRestaProducto>()

        if (diferenciaCantidad != 0 || cambiosVariable) {
            val sumarORestar = diferenciaCantidad * multiplicador

            val idTransaccion = UUID.randomUUID().toString()
            val values = ContentValues().apply {
                put("idTransaccion", idTransaccion)
                put("idProducto", idProducto)
                put("cantidad", sumarORestar.toString())
                put("subido", "false")
                put("listaVariables", modeloProductoFacturado.convertirListaVariablesToString(
                    modeloProductoFacturado.listaVariables ?: emptyList()))
            }

            // Guardamos la referencia en la base de datos para cambiar la cantidad del producto
            db.insert("transaccionesSumaRestaProductos", null, values)


            val sumarProducto = ModeloTransaccionSumaRestaProducto(
                idTransaccion = idTransaccion,
                idProducto =idProducto,
                cantidad = sumarORestar.toString(),
                subido ="false",
                listaVariables = modeloProductoFacturado.listaVariables
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
                val listaVariablesJson = it.getString(it.getColumnIndexOrThrow("listaVariables"))
                // Convert JSON string to List<Variable>
                val listaVariables = ModeloProductoFacturado().convertirStringToListaVariables(listaVariablesJson)

                Log.d("ModeloProductoFacturado", "Lista de variables: convertida a modelo $listaVariables")

                val transaccion = ModeloTransaccionSumaRestaProducto(idTransaccion, idProducto, cantidad, subido,listaVariables)
                transacciones.add(transaccion)
            }
        }

        db.close()
        return transacciones
    }
}