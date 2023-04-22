package com.example.ventarapida.datos

data class ModeloFactura(
    val id_pedido: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val documento: String = "",
    val direccion: String = "",
    val descuento: String = "",
    val envio: String = "",
    val fecha: String = "",
    val hora: String = "",
    val id_vendedor: String = "",
    val nombre_vendedor: String = "",
    var total: String = ""

):java.io.Serializable