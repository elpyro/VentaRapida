package com.castellanoseloy.ventarapida.procesos

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.preference.PreferenceManager
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.mostrarAgotadosCatalogo
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.tono
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.datos.Variable
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

    fun guardarPreferenciaListaSeleccionada(context: Context, map: MutableMap<ModeloProducto, Int>, referencia: String) {
        limpiarPreferenciaListaSeleccionada(context, referencia)

        val sharedPreferences = context.getSharedPreferences(referencia, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        // Convert the map to a map of strings
        val stringMap = map.mapKeys { (key, _) ->
            gson.toJson(key) // This will properly serialize the ModeloProducto including listaVariables
        }

        val json = gson.toJson(stringMap)
        Log.d("preferencias", "Se guardaron las preferencias $json")
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

        if (json!!.isNotEmpty()) {
            val mapType = object : TypeToken<MutableMap<String, Int>>() {}.type
            val mapString: MutableMap<String, Int> = gson.fromJson(json, mapType)

            mapString.forEach { (productoJson, cantidad) ->
                val modeloProducto = gson.fromJson(productoJson, ModeloProducto::class.java)

                if (referencia == "compra_seleccionada") {
                    DatosPersitidos.compraProductosSeleccionados[modeloProducto] = cantidad
                } else if (referencia == "venta_seleccionada") {
                    DatosPersitidos.ventaProductosSeleccionados[modeloProducto] = cantidad
                }

                Log.d("preferencias", "Se cargaron las preferencias $modeloProducto")
            }
        }
    }

    fun parsearModeloProducto(productoJson: String): ModeloProducto {
        Log.d("preferencias", "Se el Json: $productoJson")
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

        // Parse listaVariables
        val listaVariablesString = fields.find { it.startsWith("listaVariables=") }?.substringAfter("=") ?: ""
//        val listaVariables = if (listaVariablesString.isNotEmpty()) {
//            listaVariablesString.removeSurrounding("[", "]").split(";").mapNotNull { variableString ->
//                val variableFields = variableString.split(",")
//                if (variableFields.size >= 5) {
//                    Variable(
//                        idVariable = variableFields[0].substringAfter("idVariable="),
//                        nombreVariable = variableFields[1].substringAfter("nombreVariable="),
//                        cantidad = variableFields[2].substringAfter("cantidad=").toIntOrNull() ?: 0,
//                        color = variableFields[3].substringAfter("color=").takeIf { it != "null" },
//                        tamano = variableFields[4].substringAfter("tamano=").takeIf { it != "null" }
//                    )
//                } else null
//            }
//        } else {
//            emptyList()
//        }

        return ModeloProducto(
            cantidad,
            codigo,
            descripcion,
            fecha_ultima_modificacion,
            id,
            nombre,
            p_compra,
            p_diamante,
            url,
            descuento,
            precio_descuento,
            listaVariablesString
        )
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