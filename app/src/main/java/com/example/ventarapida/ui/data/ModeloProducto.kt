package com.example.ventarapida.ui.data

import java.io.Serializable

data class ModeloProducto(
    val cantidad: String = "",
    val codigo: String = "",
    val descripcion: String = "",
    val fecha_ultima_modificacion: String = "",
    val id: String = "",
    val nombre: String = "",
    val p_compra: String = "",
    val p_diamante: String = "",
    val url: String = "",
    val descuento: String = "",
    val precio_descuento: String = ""

): Serializable