package com.example.ventarapida.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.ventarapida.VistaPDFReporte
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.CrearPdfInventario
import com.example.ventarapida.procesos.FirebaseProductos.buscarProductos


class ReportesViewModel : ViewModel() {
    fun crearInventarioPdf(context: Context, mayorCero: Boolean) {

        val tareaBuscarProductos = buscarProductos(mayorCero)
        tareaBuscarProductos
            .addOnSuccessListener { listaProductos ->
                val crearPdf = CrearPdfInventario()
                crearPdf.inventario(context, listaProductos as ArrayList<ModeloProducto>)

                val intent = Intent(context, VistaPDFReporte::class.java)
                context.startActivity(intent)
            }
    }

    data class ProductoKey(val idProducto: String, val nombreProducto: String, val costo: String, val venta: String)

    fun crearListaMasVendidos(productos: List<ModeloProductoFacturado>?): Map<ProductoKey, Int> {
        // Crear un mapa para contar la cantidad de ventas por producto
        val ventasPorProducto = mutableMapOf<ProductoKey, Int>()

        // Contar la cantidad de ventas por producto
        for (producto in productos!!) {
            val idProducto = producto.id_producto
            val nombreProducto = producto.producto
            val costo = producto.costo
            val venta = producto.venta
            val productoKey = ProductoKey(idProducto, nombreProducto, costo, venta)

            if (ventasPorProducto.containsKey(productoKey)) {
                val cantidadActual = ventasPorProducto[productoKey] ?: 0
                ventasPorProducto[productoKey] = cantidadActual + producto.cantidad.toInt()
            } else {
                ventasPorProducto[productoKey] = producto.cantidad.toInt()
            }
        }

        // Ordenar la lista de ventas por producto de mayor a menor
        val ventasOrdenadas = ventasPorProducto.toList().sortedByDescending { (_, value) -> value }

        // Retornar la lista de ventas por producto ordenada
        return ventasOrdenadas.toMap()
    }
    data class ProductoLLave(
        val idProducto: String,
        val nombreProducto: String
    )

    fun crearListaMayorGanancia(productos: List<ModeloProductoFacturado>?): Map<ProductoLLave, Double> {
        val gananciasPorProducto = mutableMapOf<ProductoLLave, Double>()

        for (producto in productos!!) {
            val idProducto = producto.id_producto
            val nombreProducto = producto.producto

            val ganancia = ( producto.venta.toDouble()-producto.costo.toDouble() ) * producto.cantidad.toInt()
            val productoLLave = ProductoLLave(idProducto, nombreProducto)

            if (gananciasPorProducto.containsKey(productoLLave)) {
                val gananciaActual = gananciasPorProducto[productoLLave] ?: 0.0
                gananciasPorProducto[productoLLave] = gananciaActual + ganancia
            } else {
                gananciasPorProducto[productoLLave] = ganancia
            }
        }

        val gananciasOrdenadas = gananciasPorProducto.toList().sortedByDescending { (_, value) -> value }

        return gananciasOrdenadas.toMap()
    }


}