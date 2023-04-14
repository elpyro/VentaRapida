package com.example.ventarapida.ui.datos

import java.io.Serializable

data class ModeloProducto(
    val cantidad: String = "",
    var codigo: String = "",
    val descripcion: String = "",
    val fecha_ultima_modificacion: String = "",
    val id: String = "",
    var nombre: String = "",
    val p_compra: String = "",
    var p_diamante: String = "",
    val url: String = "",
    val descuento: String = "",
    val precio_descuento: String = "",


    ): Serializable

//{
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other == null || javaClass != other.javaClass) return false
//
//        other as ModeloProducto
//
//        if (cantidad != other.cantidad) return false
//        if (codigo != other.codigo) return false
//        if (descripcion != other.descripcion) return false
//        if (fecha_ultima_modificacion != other.fecha_ultima_modificacion) return false
//        if (id != other.id) return false
//        if (nombre != other.nombre) return false
//        if (p_compra != other.p_compra) return false
//        if (p_diamante != other.p_diamante) return false
//        if (url != other.url) return false
//        if (descuento != other.descuento) return false
//        if (precio_descuento != other.precio_descuento) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = cantidad.hashCode()
//        result = 31 * result + codigo.hashCode()
//        result = 31 * result + descripcion.hashCode()
//        result = 31 * result + fecha_ultima_modificacion.hashCode()
//        result = 31 * result + id.hashCode()
//        result = 31 * result + nombre.hashCode()
//        result = 31 * result + p_compra.hashCode()
//        result = 31 * result + p_diamante.hashCode()
//        result = 31 * result + url.hashCode()
//        result = 31 * result + descuento.hashCode()
//        result = 31 * result + precio_descuento.hashCode()
//        return result
//    }
//}