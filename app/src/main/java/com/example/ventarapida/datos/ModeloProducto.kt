package com.example.ventarapida.datos

import java.io.Serializable

data class ModeloProducto(
    var cantidad: String = "",
    var codigo: String = "",
    val descripcion: String = "",
    val fecha_ultima_modificacion: String = "",
    val id: String = "",
    var nombre: String = "",
    val p_compra: String = "",
    var p_diamante: String = "",
    val url: String = "",
    val descuento: String = "",
    val precio_descuento: String = "",


    ): Serializable
