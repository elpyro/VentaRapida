package com.castellanoseloy.cataplus.ui.reportes

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.VistaPDFReporte
import com.castellanoseloy.cataplus.datos.ModeloProducto
import com.castellanoseloy.cataplus.datos.ModeloProductoFacturado
import com.castellanoseloy.cataplus.procesos.FirebaseProductoFacturadosOComprados
import com.castellanoseloy.cataplus.procesos.FirebaseProductos
import com.castellanoseloy.cataplus.procesos.Utilidades
import com.castellanoseloy.cataplus.procesos.crearPdf.CrearPdfCatalogo
import com.castellanoseloy.cataplus.procesos.crearPdf.CrearPdfGanancias
import com.castellanoseloy.cataplus.procesos.crearPdf.CrearPdfVentasPorVendedor
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
            ), Utilidades.convertirFechaAUnix(fechaFin), idVendedor,"ProductosFacturados"
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
        idVendedor: String
    ) {

        val nombreVendedor=DatosPersitidos.datosUsuario.nombre
        FirebaseProductoFacturadosOComprados.buscarProductosPorFecha(
            Utilidades.convertirFechaAUnix(
                fechaInicio
            ), Utilidades.convertirFechaAUnix(fechaFin), idVendedor,"ProductosFacturados"
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