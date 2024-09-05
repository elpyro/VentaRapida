package com.castellanoseloy.cataplus.datos

import java.io.Serializable

data class ModeloClientes (
    val id: String= "",
    var nombre: String= "",
    var documento: String= "",
    var telefono: String= "",
    var direccion: String= "",
): Serializable