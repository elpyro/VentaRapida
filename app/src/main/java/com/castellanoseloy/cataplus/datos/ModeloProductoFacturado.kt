package com.castellanoseloy.cataplus.datos

import android.util.Log
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.Serializable

data class ModeloProductoFacturado(
    val id_producto_pedido: String = "",
    val id_producto: String = "",
    val id_pedido: String = "",
    val id_vendedor: String = "",
    val vendedor: String = "",
    var producto: String = "",
    var cantidad: String = "",
    var costo: String = "",
    var venta: String = "",
    var precioDescuentos: String = "",
    val porcentajeDescuento: String = "",
    var productoEditado: String = "",
    val fecha: String = "",
    val hora: String = "",
    val imagenUrl: String = "",
    val estadoRecaudo: String = "",
    val recaudador: String = "",
    val recaudadoFecha: String = "",
    val fechaBusquedas: Long = 0,
    var tipoOperacion: String = "",
    var listaVariables: List<Variable>? = null
) : Serializable {

    // Constructor sin argumentos (ya proporcionado por defecto)
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        "",
        null
    )


    fun convertirListaVariablesToString(listaVariables: List<Variable>): String {
        val gson = Gson()
        val jsonListaVariables = gson.toJson(listaVariables) // Convierte la lista en JSON
        Log.d("ModeloProductoFacturado", "Lista de Variables en JSON: $jsonListaVariables")
        return jsonListaVariables // Devuelve el JSON como un string
    }

    fun convertirStringToListaVariables(jsonString: String?): List<Variable>? {
        if (jsonString.isNullOrEmpty()) return null

        val gson = Gson()
        val type = object : TypeToken<List<Variable>>() {}.type
        return try {
            gson.fromJson<List<Variable>>(jsonString, type)
        } catch (e: JsonSyntaxException) {
            Log.e("ConversionError", "Error al convertir JSON a List<Variable>: ${e.message}")
            null
        }
    }

}




