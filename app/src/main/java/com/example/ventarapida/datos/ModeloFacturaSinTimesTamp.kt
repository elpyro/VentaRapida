package com.example.ventarapida.datos

import com.google.firebase.Timestamp
import java.io.Serializable

//solo se usa para pasar el serealizable a los intent
data class ModeloFacturaSinTimesTamp(
    val id_pedido: String = "",
    var nombre: String = "",
    var telefono: String = "",
    var documento: String = "",
    var direccion: String = "",
    val descuento: String = "0",
    val envio: String = "0",
    val fecha: String = "",
    val hora: String = "",
    val id_vendedor: String = "",
    val nombre_vendedor: String = "",
    var total: String = "0"
): Serializable