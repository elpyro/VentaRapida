package com.castellanoseloy.ventarapida.procesos

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.preference.PreferenceManager
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.mostrarAgotadosCatalogo
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.tono
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.servicios.ServiciosSubirFoto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Preferencias {

    fun preferenciasConfiguracion(context: Context){
        // Obtener el valor de la preferencia
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        if (!DatosPersitidos.datosUsuario.configuracion.agregarInformacionAdicional) {
            DatosPersitidos.preferencia_informacion_superior =""
            DatosPersitidos.preferencia_informacion_inferior =""
        }else{
            DatosPersitidos.preferencia_informacion_superior =
                sharedPreferences.getString("inf_superior", "")!!
            DatosPersitidos.preferencia_informacion_inferior =
                sharedPreferences.getString("inf_inferior", "")!!
        }


        DatosPersitidos.edit_text_preference_codigo_area =
            sharedPreferences.getString("edit_text_preference_codigo_area","+")!!

        tono = sharedPreferences.getBoolean("sonido", true)
        mostrarAgotadosCatalogo = sharedPreferences.getBoolean("mostrarAgotados", true)


        obtenerSeleccionPendiente(context,"compra_seleccionada")
        obtenerSeleccionPendiente(context,"venta_seleccionada")

    }

    fun guardarPreferenciaListaSeleccionada(context: Context, map: MutableMap<ModeloProducto, Int>, referencia:String) {

        limpiarPreferenciaListaSeleccionada(context,referencia)

        val sharedPreferences = context.getSharedPreferences(referencia, MODE_PRIVATE)
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

    fun obtenerSeleccionPendiente(context: Context, referencia: String) {
        val sharedPreferences = context.getSharedPreferences(referencia, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("seleccion", "")

        if (referencia.equals("compra_seleccionada")) {
            if (json!!.isNotEmpty()) {
                val mapType = object : TypeToken<MutableMap<String, Int>>() {}.type
                val mapString: MutableMap<String, Int> = gson.fromJson(json, mapType)

                mapString.forEach { (productoJson, cantidad) ->
                    val modeloProducto = parsearModeloProducto(productoJson)
                    DatosPersitidos.compraProductosSeleccionados[modeloProducto] = cantidad
                }
            }
        }

        if (referencia.equals("venta_seleccionada")) {
            if (json!!.isNotEmpty()) {
                val mapType = object : TypeToken<MutableMap<String, Int>>() {}.type
                val mapString: MutableMap<String, Int> = gson.fromJson(json, mapType)

                mapString.forEach { (productoJson, cantidad) ->
                    val modeloProducto = parsearModeloProducto(productoJson)
                    DatosPersitidos.ventaProductosSeleccionados[modeloProducto] = cantidad
                }
            }
        }
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

    fun obtenerServicioPendienteSubirFoto(context: Context) {

        val fotosParaSubir = ServiciosSubirFoto()
        val serviciosPendientes = fotosParaSubir.getServiciosPendientes(context)

        serviciosPendientes.forEach { servicio ->
            val fileUri = servicio.first
            val storageRefString = servicio.second
            val idProducto = servicio.third
            val tablaReferencia = servicio.fourth

            fotosParaSubir.guardarServicioPendiente(context, fileUri, storageRefString, idProducto,tablaReferencia)
        }
    }

}