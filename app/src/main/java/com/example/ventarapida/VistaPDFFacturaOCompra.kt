package com.example.ventarapida

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.ventarapida.datos.ModeloFactura
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.crearPdf.CrearPdfFacturaOCompra
import com.example.ventarapida.procesos.FirebaseFacturaOCompra
import com.example.ventarapida.procesos.FirebaseProductoFacturadosOComprados
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import java.io.File

class VistaPDFFacturaOCompra : AppCompatActivity() {
    lateinit var menuWhatsaap: MenuItem
    var telefono: String = ""
    var datosFactura: ModeloFactura? = null
    private var progressDialogVerPDF: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vista_pdf)

        progressDialogVerPDF = ProgressDialog(this)
        progressDialogVerPDF?.setMessage("Guardando...")
        progressDialogVerPDF?.setCancelable(false)
        progressDialogVerPDF?.show()


        val id = intent.getStringExtra("id")
        val tablaReferencia = intent.getStringExtra("tablaReferencia")
        datosFactura = intent.getSerializableExtra("datosFactura") as? ModeloFactura
        val listaProductos = intent.getSerializableExtra("listaProductos") as? ArrayList<ModeloProductoFacturado>

        if (id != "enProceso") {
            cargarDesdeFirebase(id, tablaReferencia)
        }
        if (id == "enProceso") {
            cargarDesdePreferencia(tablaReferencia, datosFactura!!, listaProductos)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_vistas_pdf, menu)
        menuWhatsaap = menu.findItem(R.id.action_whatsapp)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (datosFactura?.telefono != null && datosFactura?.telefono != "" ) {
            menuWhatsaap.isVisible = true
            telefono = datosFactura?.telefono.toString()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_compartir -> {
                compartirPDF()
                return true
            }
            R.id.action_whatsapp -> {
                compartirWhatsapp(telefono)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun compartirPDF() {
        val fileName = "reporte.pdf"
        val filePath = "${this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/$fileName"

        val file = File(filePath)
        val uri = FileProvider.getUriForFile(this, "com.example.ventarapida.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Choose bar"))
    }

    private fun compartirWhatsapp(numeroTelefono: String) {
        var numeroTelefonoFormateado=numeroTelefono.replace("[\\s+]".toRegex(), "")
        Log.d("Informacion", "El numero de telefono whastsapp es: $numeroTelefonoFormateado")

        val fileName = "reporte.pdf"
        val filePath = "${this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/$fileName"

        val file = File(filePath)
        val uri = FileProvider.getUriForFile(this, "com.example.ventarapida.fileprovider", file)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_TEXT, "Adjunto el archivo PDF")

        val packageManager = packageManager
        val activities = packageManager.queryIntentActivities(intent, 0)
        val appList = ArrayList<String>()
        val packageNameList = ArrayList<String>()

        for (i in activities.indices) {
            val info = activities[i]
            val packageName = info.activityInfo.packageName
            val appName = info.loadLabel(packageManager).toString()
            if (packageName == "com.whatsapp" || packageName == "com.whatsapp.w4b") {
                appList.add(appName)
                packageNameList.add(packageName)
            }
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una aplicación")
        builder.setItems(appList.toTypedArray()) { _, which ->
            val selectedPackage = packageNameList[which]
            intent.`package` = selectedPackage
            intent.putExtra("jid", PhoneNumberUtils.stripSeparators(numeroTelefonoFormateado) + "@s.whatsapp.net")

            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "La aplicación seleccionada no está instalada", Toast.LENGTH_SHORT).show()
            }
        }

        builder.create().show()
    }

    private fun cargarDesdePreferencia(
        tablaReferencia: String?,
        datosFactura: ModeloFactura,
        listaProductos2: ArrayList<ModeloProductoFacturado>?
    ) {

        try {
            val crearPdf = CrearPdfFacturaOCompra()
            crearPdf.facturaOCompra(this, datosFactura, tablaReferencia!!, listaProductos2!!)

            visualizarPDF()
        }catch (e: Exception ){
            Toast.makeText(this, "Error creando PDF", Toast.LENGTH_LONG).show()
        }

    }

    private fun cargarDesdeFirebase(id: String?, tablaReferencia: String?) {

        val tareaFacturas = FirebaseFacturaOCompra.buscarFacturaOCompraPorId(tablaReferencia!!, id!!)

        var tablaReferenciaProductos=""
        if(tablaReferencia=="Compra")  tablaReferenciaProductos="ProductosComprados"
        if(tablaReferencia=="Factura")  tablaReferenciaProductos="ProductosFacturados"
        val tareaProductos= FirebaseProductoFacturadosOComprados.buscarProductosPorPedido(tablaReferenciaProductos, id)

        tareaFacturas.addOnSuccessListener { factura ->


            if (factura?.telefono!=""){
                menuWhatsaap.isVisible = true
                telefono=factura?.telefono.toString()
            }

            tareaProductos.addOnSuccessListener { listaProductos->
            try{
                val crearPdf= CrearPdfFacturaOCompra()
                crearPdf.facturaOCompra(this, factura!!, tablaReferencia,listaProductos as ArrayList<ModeloProductoFacturado>)

                visualizarPDF()
            }catch (e: Exception ){
            Toast.makeText(this, "Error creando PDF", Toast.LENGTH_LONG).show()
        }
            }

        }
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