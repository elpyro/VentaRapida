package com.example.ventarapida.datos

import java.io.Serializable

data class ModeloUsuario(
    val id: String= "",
    var nombre: String= "",
    var correo: String= "",
    var idEmpresa: String= "",
    var empresa: String= "",
    val perfil:String=""
    ):Serializable