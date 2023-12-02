package com.castellanoseloy.ventarapida.procesos.crearPdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import androidx.core.content.ContextCompat
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.datosUsuario
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.datos.ModeloFactura
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.procesos.PageNumeration
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.formatoMonenda
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class CrearPdfReciboIngreso {

    private val FONT_TITLE = Font(Font.FontFamily.TIMES_ROMAN, 16f, Font.BOLD)
    private val FONT_SUBTITLE = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.BOLD)
    private val DATOSEMPRESAFUENTE = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.ITALIC)
    private val FONT_CURSIVA = Font(Font.FontFamily.HELVETICA, 12f, Font.ITALIC)
    private val FONT_GARANTIA = Font(Font.FontFamily.HELVETICA, 7f, Font.ITALIC)
    private val FONT_CELL = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.NORMAL)
    private val FONT_COLUMN = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.NORMAL)

    fun facturaOCompra(
        context: Context,
        modeloFactura: ModeloFactura,
        tipo: String,
        listaProductos: ArrayList<ModeloProductoFacturado>
    ) {

        //ordenar alfabetico
        listaProductos.sortBy { it.producto }

        val file =
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Cataplus.pdf")
        val outputStream = FileOutputStream(file)

        // Crea el documento PDF
        val document = Document()
        document.setMargins(24f, 24f, 32f, 32f)
        PdfWriter.getInstance(document, outputStream).setPageEvent(PageNumeration())
        document.open()

        metadata(document)
        cabezera(document, tipo, modeloFactura, context)
        document.add(Paragraph("\n"))
        datosFactura(document, tipo, modeloFactura, listaProductos)
        document.add(Paragraph("\n"))
        document.add(crearTabla(tipo, listaProductos))
        document.add(Paragraph("\n"))
        pieDocumento(document, tipo)


        document.close()
        outputStream.close()
    }

    private fun metadata(document: Document) {
        document.addTitle("Cataplus")
        document.addSubject("Factura")
        document.addAuthor("Eloy Castellanos")
        document.addCreator("Eloy Castellanos")
    }

    private fun cabezera(
        document: Document,
        tipo: String,
        modeloFactura: ModeloFactura,
        context: Context
    ) {

        var titulo: Paragraph? = null

        titulo = Paragraph("Recibo Ingreso de Mercancía", FONT_TITLE)

        titulo.alignment = Element.ALIGN_CENTER


        if (DatosPersitidos.datosEmpresa != null) {
            val table = PdfPTable(3)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(4f, 2f, 4f))
            table.defaultCell.border = PdfPCell.NO_BORDER
            table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
            table.defaultCell.horizontalAlignment = Element.ALIGN_CENTER

            var cell: PdfPCell

            run {
                //  CELDA1
                cell = PdfPCell()
                cell.horizontalAlignment = Element.ALIGN_CENTER
                cell.border = PdfPCell.NO_BORDER
                cell.setPadding(4f)
                cell.isUseAscender = true

                var temp = Paragraph("Fecha: " + modeloFactura.fecha, FONT_SUBTITLE)
                temp.alignment = Element.ALIGN_LEFT
                cell.addElement(temp)

                temp = Paragraph("Ref: " + modeloFactura.id_pedido.substring(0, 5), FONT_SUBTITLE)
                temp.alignment = Element.ALIGN_LEFT
                cell.addElement(temp)

                temp = Paragraph(DatosPersitidos.datosEmpresa.nombre, DATOSEMPRESAFUENTE)
                temp.alignment = Element.ALIGN_LEFT
                cell.addElement(temp)

                temp = Paragraph(DatosPersitidos.datosEmpresa.telefono1, DATOSEMPRESAFUENTE)
                temp.alignment = Element.ALIGN_LEFT
                cell.addElement(temp)

                temp = Paragraph(DatosPersitidos.datosEmpresa.telefono2, DATOSEMPRESAFUENTE)
                temp.alignment = Element.ALIGN_LEFT
                cell.addElement(temp)


                table.addCell(cell)
            }

            run {
                //  CELDA2
                cell = PdfPCell()
                cell.horizontalAlignment = Element.ALIGN_CENTER
                cell.border = PdfPCell.NO_BORDER
                cell.setPadding(4f)
                cell.isUseAscender = true

                var temp = Paragraph(titulo)
                temp.alignment = Element.ALIGN_CENTER
                cell.addElement(temp)

                table.addCell(cell)
            }

            run {
                //  CELDA3
                val logoTable = PdfPTable(1)
                logoTable.widthPercentage = 100f
                logoTable.defaultCell.border = PdfPCell.NO_BORDER
                logoTable.horizontalAlignment = Element.ALIGN_RIGHT
                logoTable.defaultCell.verticalAlignment = Element.ALIGN_RIGHT

                val logoDrawable = DatosPersitidos.logotipo.drawable

                logoTable.addCell(Utilidades.generarLogoCell(context, logoDrawable))


                var logoCell = PdfPCell(Phrase(DatosPersitidos.datosEmpresa.nombre, FONT_SUBTITLE))
                logoCell.horizontalAlignment = Element.ALIGN_CENTER
                logoCell.verticalAlignment = Element.ALIGN_CENTER
                logoCell.border = PdfPCell.NO_BORDER
                logoTable.addCell(logoCell)

                cell = PdfPCell(
                    Phrase(
                        "Surtidor: " + datosUsuario.nombre,
                        DATOSEMPRESAFUENTE
                    )
                )
                cell.horizontalAlignment = Element.ALIGN_CENTER
                cell.verticalAlignment = Element.ALIGN_CENTER
                cell.border = PdfPCell.NO_BORDER
                logoTable.addCell(cell)


                cell = PdfPCell(logoTable)
                cell.horizontalAlignment = Element.ALIGN_RIGHT
                cell.verticalAlignment = Element.ALIGN_CENTER
                cell.isUseAscender = true
                cell.border = PdfPCell.NO_BORDER
                cell.setPadding(2f)
                table.addCell(cell)

            }
            document.add(table)
        }
    }

    private fun datosFactura(
        document: Document,
        tipo: String,
        modeloFactura: ModeloFactura,
        listaProductos: List<ModeloProductoFacturado>
    ) {

        // Celda para agregar a la tabla
        val cell: PdfPCell = PdfPCell()
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.border = PdfPCell.NO_BORDER

        // Contenido que deseas agregar a la celda
        val referencias = listaProductos.size
        val items = listaProductos.sumBy { it.cantidad.toInt() }.toString()
        val temp = Paragraph("Referencias: $referencias, Items: $items", FONT_CURSIVA)
        temp.alignment = Element.ALIGN_LEFT
        cell.addElement(temp)

        document.add(temp)
    }




    private fun crearTabla(tipo: String, dataTable: List<ModeloProductoFacturado>): PdfPTable? {
        val table1 = PdfPTable(4)
        table1.widthPercentage = 100f
        table1.setWidths(floatArrayOf(1f, 8f, 2f, 3f))
        table1.headerRows = 1
        table1.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table1.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
        var cell: PdfPCell
        run {

            cell = PdfPCell(Phrase("ID", FONT_COLUMN))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)
            table1.addCell(cell)

            cell = PdfPCell(Phrase("Producto", FONT_COLUMN))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)
            table1.addCell(cell)

            cell = PdfPCell(Phrase("Cant.", FONT_COLUMN))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)
            table1.addCell(cell)

            cell = PdfPCell(Phrase("Recibido", FONT_COLUMN))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)
            table1.addCell(cell)


        }

        var alternate = false
        val lt_gray = BaseColor(221, 221, 221) //#DDDDDD
        var cell_color: BaseColor?
        val size = dataTable.size
        for (i in 0 until size) {
            cell_color = if (alternate) lt_gray else BaseColor.WHITE
            val temp = dataTable[i]


            cell = PdfPCell()
            setCellFormat(cell, cell_color!!, (i+1).toString())
            cell.horizontalAlignment = Element.ALIGN_CENTER
            table1.addCell(cell)


            cell = PdfPCell()
            setCellFormat(cell, cell_color!!, temp.producto)
            cell.horizontalAlignment = Element.ALIGN_LEFT
            table1.addCell(cell)


            cell = PdfPCell()
            setCellFormat(cell, cell_color!!, temp.cantidad)
            table1.addCell(cell)

            cell = PdfPCell()
            setCellFormat(cell, cell_color!!, "")
            table1.addCell(cell)

            alternate = !alternate
        }
        return table1
    }

    private fun setCellFormat(cell: PdfPCell, backgroundColor: BaseColor, text: String) {
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.paddingLeft = 4f
        cell.paddingRight = 4f
        cell.paddingTop = 8f
        cell.paddingBottom = 8f
        cell.backgroundColor = backgroundColor
        cell.phrase = Phrase(text, FONT_CELL)
    }

    private fun pieDocumento(document: Document, tipo: String) {
        // Agrega el pie de página de la factura
        document.add(Paragraph(""))//espacio en blanco

        val pieDocumento1 = Paragraph("____________________________")
        pieDocumento1.alignment = Element.ALIGN_CENTER // Alinea el texto al centro
        document.add(pieDocumento1)

        val pieDocumento = Paragraph("Recibido por")
        pieDocumento.alignment = Element.ALIGN_CENTER // Alinea el texto al centro
        document.add(pieDocumento)
    }
}
