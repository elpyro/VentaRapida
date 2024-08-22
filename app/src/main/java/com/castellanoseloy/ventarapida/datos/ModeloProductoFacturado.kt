package com.castellanoseloy.ventarapida.datos

import java.io.Serializable

data class ModeloProductoFacturado(
    val id_producto_pedido: String = "",
    val id_producto: String = "",
    val id_pedido: String = "",
    val id_vendedor: String = "",
    val vendedor: String = "",
    var producto: String = "",
    var cantidad: String = "",
    var costo: String = "",
    var venta: String = "",
    var precioDescuentos: String = "",
    val porcentajeDescuento: String = "",
    var productoEditado: String = "",
    val fecha: String = "",
    val hora: String = "",
    val imagenUrl: String = "",
    val estadoRecaudo: String = "",
    val recaudador: String = "",
    val recaudadoFecha: String = "",
    val fechaBusquedas: Long = 0,
    var tipoOperacion: String = "",
    var listaVariables: List<Variable>? = null
) : Serializable {

    // Constructor sin argumentos (ya proporcionado por defecto)
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        "",
        null
    )

    fun getUpdates(): Map<String, Any?> {
        // Convertir la lista de Variable a una lista de mapas
        val listaVariablesMap = listaVariables?.map { it.toMap() } ?: emptyList()

        // Crear el HashMap con los valores actualizables
        return hashMapOf(
            "id_producto_pedido" to id_producto_pedido,
            "id_producto" to id_producto,
            "id_pedido" to id_pedido,
            "id_vendedor" to id_vendedor,
            "vendedor" to vendedor,
            "producto" to producto,
            "cantidad" to cantidad,
            "costo" to costo,
            "venta" to venta,
            "precioDescuentos" to precioDescuentos,
            "porcentajeDescuento" to porcentajeDescuento,
            "productoEditado" to productoEditado,
            "fecha" to fecha,
            "hora" to hora,
            "imagenUrl" to imagenUrl,
            "estadoRecaudo" to estadoRecaudo,
            "recaudador" to recaudador,
            "recaudadoFecha" to recaudadoFecha,
            "fechaBusquedas" to fechaBusquedas,
            "tipoOperacion" to tipoOperacion,
            "listaVariables" to listaVariablesMap // Incluir la lista de variables como un mapa
        )
    }
}


