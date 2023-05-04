package com.example.ventarapida

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import com.example.ventarapida.datos.ModeloFactura
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.CrearPdf
import com.example.ventarapida.procesos.FirebaseFacturaOCompra
import com.example.ventarapida.procesos.FirebaseProductoFacturadosOComprados
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import java.io.File
import java.util.UUID
import kotlin.collections.ArrayList

class VistaPDFFacturaOCompra : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vista_pdf)

        val id = intent.getStringExtra("id")
        val tablaReferencia = intent.getStringExtra("tablaReferencia")
        val datosFactura = intent.getSerializableExtra("datosFactura") as? ModeloFactura
        val listaProductos = intent.getSerializableExtra("listaProductos") as? ArrayList<ModeloProductoFacturado>

        if(id!="enProceso"){
            cargarDesdeFirebase(id, tablaReferencia)
        }
        if(id=="enProceso"){
            cargarDesdePreferencia(tablaReferencia, datosFactura!!, listaProductos)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_vistas_pdf, menu)
        return true
    }

    private fun cargarDesdePreferencia(
        tablaReferencia: String?,
        datosFactura: ModeloFactura,
        listaProductos2: ArrayList<ModeloProductoFacturado>?
    ) {

        val crearPdf = CrearPdf()
        crearPdf.facturaOCompra(this, datosFactura, tablaReferencia!!, listaProductos2!!)

        visualizarPDF()
    }

    private fun cargarDesdeFirebase(id: String?, tablaReferencia: String?) {

        val tareaFacturas = FirebaseFacturaOCompra.buscarFacturaOCompraPorId(tablaReferencia!!, id!!)

        var tablaReferenciaProductos=""
        if(tablaReferencia=="Compra")  tablaReferenciaProductos="ProductosComprados"
        if(tablaReferencia=="Factura")  tablaReferenciaProductos="ProductosFacturados"
        val tareaProductos= FirebaseProductoFacturadosOComprados.buscarProductosPorPedido(tablaReferenciaProductos, id)

        tareaFacturas.addOnSuccessListener { factura ->

            tareaProductos.addOnSuccessListener { listaProductos->

                val crearPdf=CrearPdf()
                crearPdf.facturaOCompra(this, factura!!, tablaReferencia,listaProductos as ArrayList<ModeloProductoFacturado>)

                visualizarPDF()

            }

        }
    }

    private fun visualizarPDF() {

        val fileName = "factura.pdf"
        val filePath = "${this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/$fileName"
        val pdfView = findViewById<PDFView>(R.id.pdfView)

        // Carga el archivo PDF desde la carpeta de activos
        pdfView.fromFile(File(filePath))
            .enableSwipe(true) // permite el deslizamiento horizontal
            .swipeHorizontal(false) // desplaza verticalmente por defecto
            .enableDoubletap(true) // permite doble toque para hacer zoom
            .defaultPage(0) // muestra la primera página por defecto
            .fitEachPage(true) // ajusta cada página al ancho del dispositivo
            .pageFitPolicy(FitPolicy.BOTH) // ajusta tanto el ancho como el alto de cada página
            .load()
    }


}