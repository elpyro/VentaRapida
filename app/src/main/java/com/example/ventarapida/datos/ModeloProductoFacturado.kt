package com.example.ventarapida.datos

import java.io.Serializable


data class ModeloProductoFacturado(
    val id_producto_pedido:String="",
    val id_producto:String="",
    val id_pedido:String ="",
    val id_vendedor:String ="",
    val vendedor:String="",
    var producto:String ="",
    var cantidad:String="",
    var costo:String="",
    var venta:String="",
    val precioDescuentos:String="",
    val porcentajeDescuento:String="",
    var productoEditado:String="",
    val fecha:String="",
    val hora:String="",
    val imagenUrl:String="",
    val fechaBusquedas: Long = 0
    ): Serializable