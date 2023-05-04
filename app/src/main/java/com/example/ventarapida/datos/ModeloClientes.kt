package com.example.ventarapida.datos

import java.io.Serializable

data class ModeloClientes (
    val id: String= "",
    val nombre: String= "",
    val documento: String= "",
    val telefono: String= "",
    val direccion: String= "",
): Serializable