package com.example.ventarapida.procesos

import android.content.ContentValues
import android.content.Context
import com.example.ventarapida.baseDatos.MyDatabaseHelper
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloTransaccionSumaRestaProducto
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
        context: Context?,
        productosSeleccionados: MutableMap<ModeloProducto, Int>
    ){

        //guarda en la base de datos los productos que van a subir a firebase cuando hay intentet
        val dbHelper = MyDatabaseHelper(context!!)
        val db = dbHelper.readableDatabase

       productosSeleccionados.forEach { (producto, cantidadSeleccionada) ->
           if(cantidadSeleccionada!=0){
               val idTransaccion = UUID.randomUUID().toString()
               val values = ContentValues().apply {
                   put("idTransaccion", idTransaccion)
                   put("idProducto", producto.id)
                   put("cantidad", cantidadSeleccionada.toString())
               }
               db.insert("transaccionesSumaRestaProductos", null, values)
           }
        }

        db.close()
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