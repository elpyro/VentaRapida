package com.example.ventarapida.datos

import java.io.Serializable
import java.util.Date

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
    val url:String= "",
    val plan:String="",
    val proximo_pago:String ="",
    val ultimo_pago: String ="",
    val idDuenoCuenta: String =""
        ):Serializable