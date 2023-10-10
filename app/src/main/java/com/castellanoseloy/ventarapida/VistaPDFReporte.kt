package com.castellanoseloy.ventarapida

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.castellanoseloy.ventarapida.R
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import java.io.File

class VistaPDFReporte : AppCompatActivity() {
    private var progressDialogVerPDF: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vista_pdf)

        progressDialogVerPDF = ProgressDialog(this)
        progressDialogVerPDF?.setMessage("Cargando...")
        progressDialogVerPDF?.setCancelable(false)
        progressDialogVerPDF?.show()

        visualizarPDF()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_vistas_pdf, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_compartir -> {
                compartirPDF()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun compartirPDF() {
        val fileName = "reporte.pdf"
        val filePath = "${this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/$fileName"

        val file = File(filePath)
        val uri = FileProvider.getUriForFile(this, "com.castellanoseloy.ventarapida.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Choose bar"))
    }



    private fun visualizarPDF() {

        val fileName = "reporte.pdf"
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

        //ocultamos los prgress que esten activos
        MainActivity.progressDialog?.dismiss()
        progressDialogVerPDF?.dismiss()
    }


}