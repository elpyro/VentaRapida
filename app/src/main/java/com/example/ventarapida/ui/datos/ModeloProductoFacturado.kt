package com.example.ventarapida.ui.datos

import java.io.Serializable

data class ModeloProductoFacturado (
    val id_producto_pedido:String="",
    val id_producto:String="",
    val id_pedido:String ="",
    val id_vendedor:String ="",
    val vendedor:String="",
    val producto:String ="",
    val cantidad:String="",
    val costo:String="",
    val venta:String="",
    val precioDescuentos:String="",
    val fecha:String="",
    val hora:String="",
    val imagenUrl:String=""
    ): Serializable