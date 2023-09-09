package com.example.ventarapida.ui.reportes

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity
import com.example.ventarapida.VistaPDFReporte
import com.example.ventarapida.databinding.FragmentReporteVendedorBinding
import com.example.ventarapida.databinding.FragmentReportesBinding
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.FirebaseProductoFacturadosOComprados
import com.example.ventarapida.procesos.FirebaseProductos
import com.example.ventarapida.procesos.Utilidades
import com.example.ventarapida.procesos.crearPdf.CrearPdfCatalogo
import com.example.ventarapida.procesos.crearPdf.CrearPdfGanancias
import com.example.ventarapida.procesos.crearPdf.CrearPdfInventario
import com.example.ventarapida.procesos.crearPdf.CrearPdfMasVendidos
import com.example.ventarapida.procesos.crearPdf.CrearPdfMayorGanancia
import com.example.ventarapida.procesos.crearPdf.CrearPdfVentasPorVendedor
import kotlinx.coroutines.runBlocking

class ReporteVendedorViewModel : ViewModel() {
    private val _reporteCompletado = MutableLiveData<Unit>()
    val reporteCompletado: LiveData<Unit> = _reporteCompletado


    fun crearCatalogo(context: Context, mayorCero: Boolean) {

        val tareaBuscarProductos = FirebaseProductos.buscarProductos(mayorCero)
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

    fun ReporteGananciaPorVendedor(
        context: Context,
        fechaInicio: String,
        fechaFin: String,
        idVendedor: String,
        nombreVendedor:String
    ) {

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
                    Toast.makeText(context,"No hay registros disponibles", Toast.LENGTH_LONG).show()
                    _reporteCompletado.value = Unit
                }
            }
    }


    fun ReportePorVendedor(
        context: Context,
        fechaInicio: String,
        fechaFin: String,
        idVendedor: String,
        binding: FragmentReporteVendedorBinding
    ) {

        val nombreVendedor=MainActivity.datosUsuario.nombre
        FirebaseProductoFacturadosOComprados.buscarProductosPorFecha(
            Utilidades.convertirFechaAUnix(
                fechaInicio
            ), Utilidades.convertirFechaAUnix(fechaFin), idVendedor
        )
            .addOnSuccessListener { productos ->
                val crearPdf= CrearPdfVentasPorVendedor()
                crearPdf.ventas(context, fechaInicio, fechaFin, productos as ArrayList<ModeloProductoFacturado>,nombreVendedor )

                _reporteCompletado.value = Unit
                val intent = Intent(context, VistaPDFReporte::class.java)
                context.startActivity(intent)
            }
    }






}