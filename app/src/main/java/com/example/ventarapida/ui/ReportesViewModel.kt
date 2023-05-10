package com.example.ventarapida.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.ventarapida.VistaPDFReporte
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.procesos.CrearPdfInventario
import com.example.ventarapida.procesos.FirebaseProductos.buscarProductos


class ReportesViewModel : ViewModel() {
    fun crearInventarioPdf(context: Context, mayorCero: Boolean) {

        val tareaBuscarProductos= buscarProductos(mayorCero)
        tareaBuscarProductos
            .addOnSuccessListener {listaProductos->
                val crearPdf= CrearPdfInventario()
                crearPdf.inventario(context, listaProductos as ArrayList<ModeloProducto>)

                val intent = Intent(context, VistaPDFReporte::class.java)
                context.startActivity(intent)
            }
    }

}