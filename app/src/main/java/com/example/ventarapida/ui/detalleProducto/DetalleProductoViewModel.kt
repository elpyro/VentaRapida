package com.example.ventarapida.ui.detalleProducto

import androidx.lifecycle.ViewModel
import com.example.ventarapida.ui.data.ModeloProducto

class DetalleProductoViewModel : ViewModel() {
    // Lista de productos
    val listaProductos = mutableListOf<ModeloProducto>()

    // Posición actual del producto en la lista
    var posicionActual = 0

    fun actualizarListaProductos(nuevaLista: List<ModeloProducto>) {
        listaProductos.clear()
        listaProductos.addAll(nuevaLista)
    }
    fun actualizarPosiscion(posicionRecibida:Int){
        posicionActual=posicionRecibida
    }
}
