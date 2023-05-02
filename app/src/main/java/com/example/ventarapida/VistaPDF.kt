package com.example.ventarapida

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.example.ventarapida.procesos.CrearPdf
import com.example.ventarapida.procesos.FirebaseFacturaOCompra
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import java.io.File

class VistaPDF : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vista_pdf)

        val id = intent.getStringExtra("id")
        val tablaReferencia = intent.getStringExtra("tablaReferencia")

        val tareaFacturas = FirebaseFacturaOCompra.buscarFacturaOCompraPorId(tablaReferencia!!, id!!)


        tareaFacturas.addOnSuccessListener { factura ->

            val crearPdf=CrearPdf()
            crearPdf.facturaOCompra(this, factura!!)

            visualizarPDF()
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