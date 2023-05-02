package com.example.ventarapida.datos

import java.io.Serializable

data class ModeloDatosEmpresa (
    val id: String= "",
    val nombre: String= "",
    val documento: String= "",
    val pagina: String= "",
    val correo: String= "",
    val telefono1: String= "",
    val telefono2: String= "",
    val direccion: String= "",
    val garantia: String= "",
    val premiun:String= "",
    val ultimo_pago:String= "",
    val url:String= ""
        ):Serializable