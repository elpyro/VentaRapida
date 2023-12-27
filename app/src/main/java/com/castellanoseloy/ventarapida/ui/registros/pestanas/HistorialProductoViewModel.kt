package com.castellanoseloy.ventarapida.ui.registros.pestanas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.procesos.FirebaseProductoFacturadosOComprados


class HistorialProductoViewModel : ViewModel() {

    private val _historialProductos = MutableLiveData<List<ModeloProductoFacturado>>()
    val historialProductos: LiveData<List<ModeloProductoFacturado>> get() = _historialProductos



    fun cargarRegistros(idProducto: String?) {
        val tareaProductosComprados = FirebaseProductoFacturadosOComprados.buscarProductosFacturadosPorId("ProductosComprados", idProducto!!)
        val tareaProductosVendidos = FirebaseProductoFacturadosOComprados.buscarProductosFacturadosPorId("ProductosFacturados", idProducto)

        // Crear una lista mutable para almacenar los resultados combinados
        val listaProductosCompradosVendidos = mutableListOf<ModeloProductoFacturado>()

        // Escuchar el resultado de la tarea de productos comprados
        tareaProductosComprados.addOnSuccessListener { listaProductosComprados ->
            // Agregar todos los productos comprados a la lista combinada
            listaProductosCompradosVendidos.addAll(listaProductosComprados)

            // Escuchar el resultado de la tarea de productos vendidos
            tareaProductosVendidos.addOnSuccessListener { listaProductosVendidos ->
                // Agregar todos los productos vendidos a la lista combinada
                listaProductosCompradosVendidos.addAll(listaProductosVendidos)
                _historialProductos.value = listaProductosCompradosVendidos.sortedByDescending { it.fechaBusquedas }
            }
        }
    }
}