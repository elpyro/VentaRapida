package com.castellanoseloy.cataplus.datos

import android.util.Log
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.Serializable

data class ModeloProducto(
    var cantidad: String = "",
    var codigo: String = "",
    val descripcion: String = "",
    val fecha_ultima_modificacion: String = "",
    val id: String = "",
    var nombre: String = "",
    var p_compra: String = "",
    var p_diamante: String = "",
    val url: String = "",
    val descuento: String = "",
    val precio_descuento: String = "",
    val comentario: String = "",
    val proveedor: String = "",
    var editado: String = "", // evalúa si el usuario editó el producto antes de vender un producto
    var listaVariables: List<Variable>? = null
) : Serializable {

    override fun toString(): String {
        return Gson().toJson(this)
    }

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
        null
    )


    fun getUpdates(): Map<String, Any?> {
        // Convertir la lista de Variable a una lista de mapas
        val listaVariablesMap = listaVariables?.map { it.toMap() } ?: emptyList()

        // Crear el HashMap
        return hashMapOf(
            "id" to id,
            "nombre" to nombre,
            "cantidad" to cantidad,
            "p_compra" to p_compra,
            "p_diamante" to p_diamante,
            "comentario" to comentario,
            "proveedor" to proveedor,
            "listaVariables" to listaVariablesMap
        )
    }
}

data class Variable(
    val idVariable: String = "",
    val nombreVariable: String = "",
    var cantidad: Int = 0,
    val color: String? = null,
    val tamano: String? = null
) :Serializable {
    // Constructor sin argumentos (ya proporcionado por defecto)
    constructor() : this("", "", 0, null, null)

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "idVariable" to idVariable,
            "nombreVariable" to nombreVariable,
            "cantidad" to cantidad,
            "color" to color,
            "tamano" to tamano
        )
    }
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


