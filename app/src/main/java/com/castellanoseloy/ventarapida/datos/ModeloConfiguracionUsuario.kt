package com.castellanoseloy.ventarapida.datos

import java.io.Serializable

class ModeloConfiguracionUsuario(
    val mostrarPreciosCompra: Boolean= false,
    val editarFacturas:Boolean=true,
    val mostrarReporteGanancia: Boolean=true,
    val agregarInformacionAdicional:Boolean=true
): Serializable