package com.example.ventarapida.procesos

import android.content.Context
import com.example.ventarapida.MainActivity
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.ui.procesos.ServiciosSubirFoto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Preferencias {

    fun guardarPreferenciaListaSeleccionada(context: Context, map: MutableMap<ModeloProducto, Int>, referencia:String) {

        limpiarPreferenciaListaSeleccionada(context,referencia)

        val sharedPreferences = context.getSharedPreferences(referencia, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(map)
        editor.putString("seleccion", json)
        editor.apply()
    }

    fun limpiarPreferenciaListaSeleccionada(context: Context, referencia:String) {

        val sharedPreferences = context.getSharedPreferences(referencia, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("seleccion")
        editor.apply()

    }

    fun obtenerSeleccionPendiente(context: Context, referencia:String) {
        val sharedPreferences = context.getSharedPreferences(referencia, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("seleccion", "")

        if(referencia.equals("compra_seleccionada")) {
            if (json!!.isNotEmpty()) {
                val mapType = object : TypeToken<MutableMap<String, Int>>() {}.type
                val mapString: MutableMap<String, Int> = gson.fromJson(json, mapType)

                mapString.forEach { (productoJson, cantidad) ->

                    val modeloProducto = parsearModeloProducto(productoJson)

                    MainActivity.compraProductosSeleccionados[modeloProducto] = cantidad
                }
            }
        }
        if(referencia.equals("venta_seleccionada")){
            if (json!!.isNotEmpty()) {
                val mapType = object : TypeToken<MutableMap<String, Int>>() {}.type
                val mapString: MutableMap<String, Int> = gson.fromJson(json, mapType)

                mapString.forEach { (productoJson, cantidad) ->

                    val modeloProducto = parsearModeloProducto(productoJson)

                    MainActivity.ventaProductosSeleccionados[modeloProducto] = cantidad
                }
        }

        }
        return
    }

    fun parsearModeloProducto(productoJson: String): ModeloProducto {

        val fields = productoJson.split(", ")
        val cantidad = fields[0].substringAfter("=")
        val codigo = fields[1].substringAfter("=")
        val descripcion = fields[2].substringAfter("=")
        val fecha_ultima_modificacion = fields[3].substringAfter("=")
        val id = fields[4].substringAfter("=")
        val nombre = fields[5].substringAfter("=")
        val p_compra = fields[6].substringAfter("=")
        val p_diamante = fields[7].substringAfter("=")
        val url = fields[8].substringAfter("=")
        val descuento = fields[9].substringAfter("=")
        val precio_descuento = ""
        return ModeloProducto(cantidad, codigo, descripcion, fecha_ultima_modificacion, id, nombre, p_compra, p_diamante, url, descuento, precio_descuento)
    }

    fun obtenerServicioPendiente(context: Context) {

        val fotosParaSubir = ServiciosSubirFoto()
        val serviciosPendientes = fotosParaSubir.getServiciosPendientes(context)

        serviciosPendientes.forEach { servicio ->
            val fileUri = servicio.first
            val storageRefString = servicio.second
            val idProducto = servicio.third

            fotosParaSubir.guardarServicioPendiente(context, fileUri, storageRefString, idProducto)
        }
    }

}