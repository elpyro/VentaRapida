package com.example.ventarapida.procesos

import android.content.Context
import android.os.Environment
import com.example.ventarapida.datos.ModeloFactura
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

class CrearPdf {

    private val FONT_TITLE = Font(Font.FontFamily.TIMES_ROMAN, 16f, Font.BOLD)
    private val FONT_SUBTITLE = Font(Font.FontFamily.TIMES_ROMAN, 10f, Font.NORMAL)
    private val Datos_empresa_fuente = Font(Font.FontFamily.TIMES_ROMAN, 10f, Font.BOLD)

    private val FONT_CELL = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.NORMAL)
    private val FONT_COLUMN = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.NORMAL)

    fun facturaOCompra(context: Context, modeloFactura: ModeloFactura) {

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "factura.pdf")
        val outputStream = FileOutputStream(file)

        // Crea el documento PDF
        val document = Document()

// Agrega metadatos al PDF
        document.addAuthor("Eloy Castellanos")
        document.addCreator("Compra Rapidita")
        document.addTitle(modeloFactura.id_pedido)

// Configura los márgenes del documento
        document.setMargins(24f, 24f, 32f, 32f)

        PdfWriter.getInstance(document, outputStream)

        document.open()

// Agrega el logotipo de la empresa a la factura
//        val logo = Image.getInstance("ruta/de/la/imagen/logo.png")
//        logo.scaleAbsolute(100f, 100f)
//        document.add(logo)

        // Agrega el encabezado de la factura
        val header =
            Paragraph(modeloFactura.id_pedido, Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD))
        header.alignment = Element.ALIGN_CENTER
        document.add(header)

        document.add(Paragraph(""))

// Agrega el contenido de la factura
        document.add(Paragraph(modeloFactura.nombre))
        document.add(Paragraph(modeloFactura.fecha))
        document.add(Paragraph("Total a pagar: ${modeloFactura.total}"))

        document.add(Paragraph(""))

        // Agrega una tabla con los detalles de la factura
        val table = PdfPTable(3)
        table.addCell("Descripción")
        table.addCell("Cantidad")
        table.addCell("Precio unitario")
        table.addCell("Producto A")
        table.addCell("1")
        table.addCell("$50.00")
        table.addCell("Producto B")
        table.addCell("2")
        table.addCell("$25.00")
        table.addCell("Producto C")
        table.addCell("1")
        table.addCell("$20.00")
        document.add(table)

// Agrega el pie de página de la factura
        document.add(
            Paragraph(
                "Gracias por su compra",
                Font(Font.FontFamily.HELVETICA, 12f, Font.ITALIC)
            )
        )

// Cierra el documento
        document.close()
        outputStream.close()
    }
}
