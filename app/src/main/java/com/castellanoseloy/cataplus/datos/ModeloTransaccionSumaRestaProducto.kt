package com.castellanoseloy.cataplus.datos

class ModeloTransaccionSumaRestaProducto (
    val idTransaccion: String,
    val idProducto: String,
    val cantidad: String,
    val subido: String,
    var listaVariables: List<Variable>? = null
)