package com.example.ventarapida.ui.datos

import java.io.Serializable

data class ModificarCantidadProducto (
    var id_transaccion: String = "",
    var id_producto: String = "",
    var cantidad: String = "",
): Serializable