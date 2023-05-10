package com.example.ventarapida.datos

import java.io.Serializable
import com.google.firebase.Timestamp

//Solo se usa para pasar los serealizable a los intent
data class ModeloProductoFacturadoSinTimesTamp (
    val id_producto_pedido:String="",
    val id_producto:String="",
    val id_pedido:String ="",
    val id_vendedor:String ="",
    val vendedor:String="",
    var producto:String ="",
    var cantidad:String="",
    val costo:String="",
    var venta:String="",
    val precioDescuentos:String="",
    val fecha:String="",
    val hora:String="",
    val imagenUrl:String="",
    val fechaBusquedas: Timestamp? = null
    ): Serializable