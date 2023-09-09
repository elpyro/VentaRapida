package com.example.ventarapida.ui.reportes

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.R
import com.example.ventarapida.VistaPDFReporte
import com.example.ventarapida.databinding.FragmentReportesBinding
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.FirebaseProductoFacturadosOComprados
import com.example.ventarapida.procesos.crearPdf.CrearPdfInventario
import com.example.ventarapida.procesos.FirebaseProductos.buscarProductos
import com.example.ventarapida.procesos.Utilidades
import com.example.ventarapida.procesos.crearPdf.CrearPdfCatalogo
import com.example.ventarapida.procesos.crearPdf.CrearPdfGanancias
import com.example.ventarapida.procesos.crearPdf.CrearPdfMasVendidos
import com.example.ventarapida.procesos.crearPdf.CrearPdfMayorGanancia
import com.example.ventarapida.procesos.crearPdf.CrearPdfVentasPorVendedor
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking


class ReportesViewModel : ViewModel() {
    private val _reporteCompletado = MutableLiveData<Unit>()
    val reporteCompletado: LiveData<Unit> = _reporteCompletado
    fun crearInventarioPdf(context: Context, mayorCero: Boolean) {

        val tareaBuscarProductos = buscarProductos(mayorCero)
        tareaBuscarProductos
            .addOnSuccessListener { listaProductos ->
                if(listaProductos.isNotEmpty()){
                val crearPdf = CrearPdfInventario()
                crearPdf.inventario(context, listaProductos as ArrayList<ModeloProducto>)

                _reporteCompletado.value = Unit
                val intent = Intent(context, VistaPDFReporte::class.java)
                context.startActivity(intent)
                }else{
                    Toast.makeText(context,"No hay registros disponibles",Toast.LENGTH_LONG).show()
                    _reporteCompletado.value = Unit
                }
            }
    }

    fun crearCatalogo(context: Context, mayorCero: Boolean) {

        val tareaBuscarProductos = buscarProductos(mayorCero)
        tareaBuscarProductos
            .addOnSuccessListener { listaProductos ->
                if(listaProductos.isNotEmpty()){
                runBlocking {
                    val crearPdf = CrearPdfCatalogo()
                    crearPdf.catalogo(context, listaProductos as ArrayList<ModeloProducto>)

                    _reporteCompletado.value = Unit
                    val intent = Intent(context, VistaPDFReporte::class.java)
                    context.startActivity(intent)
                }
            }else{
            Toast.makeText(context,"No hay registros disponibles",Toast.LENGTH_LONG).show()
                    _reporteCompletado.value = Unit
        }
            }
    }


     fun ReportePorVendedor(
         context: Context,
         fechaInicio: String,
         fechaFin: String,
         idVendedor: String,
         binding: FragmentReportesBinding
     ) {

        val nombreVendedor=binding?.spinnerVendedor?.selectedItem.toString()
        FirebaseProductoFacturadosOComprados.buscarProductosPorFecha(
            Utilidades.convertirFechaAUnix(
                fechaInicio
            ), Utilidades.convertirFechaAUnix(fechaFin), idVendedor
        )
            .addOnSuccessListener { productos ->
                if(productos.isNotEmpty()){
                    val crearPdf= CrearPdfVentasPorVendedor()
                    crearPdf.ventas(context, fechaInicio, fechaFin, productos as ArrayList<ModeloProductoFacturado>,nombreVendedor )

                    _reporteCompletado.value = Unit
                    val intent = Intent(context, VistaPDFReporte::class.java)
                    context.startActivity(intent)
                }else{
                    Toast.makeText(context,"No hay registros disponibles",Toast.LENGTH_LONG).show()
                    _reporteCompletado.value = Unit
                }
            }
    }

     fun ReporteMasVendidos(context: Context,fechaInicio: String, fechaFin: String) {
        FirebaseProductoFacturadosOComprados.buscarProductosPorFecha(
            Utilidades.convertirFechaAUnix(
                fechaInicio
            ), Utilidades.convertirFechaAUnix(fechaFin), "false"
        )
            .addOnSuccessListener { productos ->
                if(productos.isNotEmpty()){
                    var listaMasVendidos= crearListaMasVendidos(productos)

                    val crearPdf= CrearPdfMasVendidos()
                    crearPdf.masVendidos(context, fechaInicio, fechaFin,listaMasVendidos)

                    _reporteCompletado.value = Unit
                    val intent = Intent(context, VistaPDFReporte::class.java)
                    context.startActivity(intent)
                }else{
                    Toast.makeText(context,"No hay registros disponibles",Toast.LENGTH_LONG).show()
                    _reporteCompletado.value = Unit
                }
            }
    }

     fun ReporteMayorGanancia(context: Context, fechaInicio: String, fechaFin: String) {
        FirebaseProductoFacturadosOComprados.buscarProductosPorFecha(
            Utilidades.convertirFechaAUnix(
                fechaInicio
            ), Utilidades.convertirFechaAUnix(fechaFin), "false"
        )
            .addOnSuccessListener { productos ->
                if(productos.isNotEmpty()){
                var listaMasVendidos= crearListaMayorGanancia(productos)

                    val crearPdf= CrearPdfMayorGanancia()
                    crearPdf.mayorGanancia(context, fechaInicio, fechaFin,listaMasVendidos)

                    _reporteCompletado.value = Unit
                    val intent = Intent(context, VistaPDFReporte::class.java)
                    context.startActivity(intent)
                }else{
                    Toast.makeText(context,"No hay registros disponibles",Toast.LENGTH_LONG).show()
                    _reporteCompletado.value = Unit
                }
            }
    }

    fun ReporteGanancia(context: Context, fechaInicio: String, fechaFin: String) {


        FirebaseProductoFacturadosOComprados.buscarProductosPorFecha(
            Utilidades.convertirFechaAUnix(
                fechaInicio
            ), Utilidades.convertirFechaAUnix(fechaFin), "false"
        )
            .addOnSuccessListener { productos ->
                if(productos.isNotEmpty()){
                    val crearPdf= CrearPdfGanancias()
                    crearPdf.ganacias(context, fechaInicio, fechaFin, productos as ArrayList<ModeloProductoFacturado>,"Todos")

                    _reporteCompletado.value = Unit

                    val intent = Intent(context, VistaPDFReporte::class.java)
                    context.startActivity(intent)
                }else{
                    Toast.makeText(context,"No hay registros disponibles",Toast.LENGTH_LONG).show()
                    _reporteCompletado.value = Unit
                }
            }

    }

    fun ReporteGananciaPorVendedor(
        context: Context,
        fechaInicio: String,
        fechaFin: String,
        idVendedor: String,
        binding: FragmentReportesBinding
    ) {
        val nombreVendedor=binding?.spinnerVendedor?.selectedItem.toString()
        FirebaseProductoFacturadosOComprados.buscarProductosPorFecha(
            Utilidades.convertirFechaAUnix(
                fechaInicio
            ), Utilidades.convertirFechaAUnix(fechaFin), idVendedor
        )
            .addOnSuccessListener { productos ->

                if(productos.isNotEmpty()){
                    val crearPdf= CrearPdfGanancias()
                    crearPdf.ganacias(context, fechaInicio, fechaFin, productos as ArrayList<ModeloProductoFacturado>,nombreVendedor)

                    _reporteCompletado.value = Unit

                    val intent = Intent(context, VistaPDFReporte::class.java)
                    context.startActivity(intent)
                }else{
                    Toast.makeText(context,"No hay registros disponibles",Toast.LENGTH_LONG).show()
                    _reporteCompletado.value = Unit
                }
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
    data class ProductoLlave(
        val idProducto: String,
        val nombreProducto: String
    )

    fun crearListaMayorGanancia(productos: List<ModeloProductoFacturado>?): Map<ProductoLlave, Pair<Double, Int>> {
        val gananciasPorProducto = mutableMapOf<ProductoLlave, Pair<Double, Int>>()

        for (producto in productos!!) {
            val idProducto = producto.id_producto
            val nombreProducto = producto.producto

            val ganancia = (producto.venta.toDouble() - producto.costo.toDouble()) * producto.cantidad.toInt()
            val productoLlave = ProductoLlave(idProducto, nombreProducto)

            if (gananciasPorProducto.containsKey(productoLlave)) {
                val (gananciaActual, vecesVendido) = gananciasPorProducto[productoLlave] ?: Pair(0.0, 0)
                gananciasPorProducto[productoLlave] = Pair(gananciaActual + ganancia, vecesVendido + producto.cantidad.toInt())
            } else {
                gananciasPorProducto[productoLlave] = Pair(ganancia, producto.cantidad.toInt())
            }
        }

        val gananciasOrdenadas = gananciasPorProducto.toList().sortedByDescending { (_, value) -> value.first }

        return gananciasOrdenadas.toMap()
    }


}