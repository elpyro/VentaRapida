package com.castellanoseloy.cataplus.procesos.crearPdf

import android.content.Context
import android.os.Environment
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.datos.ModeloProductoFacturado
import com.castellanoseloy.cataplus.procesos.PageNumeration
import com.castellanoseloy.cataplus.procesos.Utilidades
import com.castellanoseloy.cataplus.procesos.Utilidades.formatoMonenda
import com.castellanoseloy.cataplus.procesos.Utilidades.obtenerFechaActual
import com.castellanoseloy.cataplus.procesos.Utilidades.obtenerHoraActual
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

class CrearPdfSurtido {

    private val FONT_TITLE = Font(Font.FontFamily.TIMES_ROMAN, 20f, Font.BOLD)
    private val FONT_SUBTITLE = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.BOLD)
    private val DATOSEMPRESAFUENTE = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.ITALIC)
    private val FONT_CELL = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.NORMAL)
    private val FONT_COLUMN = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.NORMAL)

    fun surtido(
        context: Context,
        fechaInicio:String,
        fechaFin:String,
        listaProductos: ArrayList<ModeloProductoFacturado>,
        nombreVendedor:String
    ) {

        //ya se ha organizado el orden en buscarProductosPorFecha

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Cataplus.pdf")
        val outputStream = FileOutputStream(file)

        // Crea el documento PDF
        val document = Document()
        document.setMargins(24f, 24f, 32f, 32f)
        PdfWriter.getInstance(document, outputStream).setPageEvent(PageNumeration())
        document.open()

        metadata(document)
        cabezera(document, context,fechaInicio,fechaFin,nombreVendedor)

        val tablaGanancias = crearTabla(listaProductos)

        val totalCostos = tablaGanancias.costos

        document.add(Paragraph("\n"))
        val parrafoTotal = Paragraph("Surtido: " +totalCostos.toString().formatoMonenda(), FONT_TITLE)
        parrafoTotal.alignment = Element.ALIGN_RIGHT
        document.add(Paragraph(parrafoTotal))

        document.add(Paragraph("\n"))
        document.add(tablaGanancias.tabla)

        document.add(Paragraph("\n"))
        document.add(Paragraph(parrafoTotal))

        document.close()
        outputStream.close()
    }

    private fun metadata(document: Document) {
        document.addTitle("Cataplus")
        document.addSubject("Reporte")
        document.addAuthor("Eloy Castellanos")
        document.addCreator("Eloy Castellanos")
    }

    private fun cabezera(
        document: Document,
        context: Context,
        fechaInicio: String,
        fechaFin: String,
        nombreVendedor: String
    ) {

        val table = PdfPTable(3)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(3f, 3f, 3f))
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

            var temp = Paragraph("Fecha: " + obtenerFechaActual(), FONT_SUBTITLE)
            temp.alignment = Element.ALIGN_LEFT
            cell.addElement(temp)

            temp = Paragraph("Hora: " + obtenerHoraActual(), FONT_SUBTITLE)
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

            val titulo: Paragraph?
            titulo = Paragraph("Reporte de Surtido", FONT_TITLE)
            titulo.alignment = Element.ALIGN_CENTER

            val temp = Paragraph(titulo)
            temp.alignment = Element.ALIGN_CENTER
            cell.addElement(temp)

            if(!nombreVendedor.equals("Todos")){
                val paragraphInicio = Paragraph("Administrador: $nombreVendedor")
                paragraphInicio.alignment = Element.ALIGN_CENTER
                cell.addElement(paragraphInicio)
            }

            if (fechaInicio != "01/01/2000") {
                val paragraphInicio = Paragraph("Desde: $fechaInicio")
                paragraphInicio.alignment = Element.ALIGN_CENTER
                cell.addElement(paragraphInicio)
            }

            if (fechaFin != "01/01/2050") {
                val paragraphFin = Paragraph("Hasta: $fechaFin")
                paragraphFin.alignment = Element.ALIGN_CENTER
                cell.addElement(paragraphFin)
            }



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

            logoTable.addCell(Utilidades.generarLogoCell(context,logoDrawable))


            var logoCell = PdfPCell(Phrase(DatosPersitidos.datosEmpresa.nombre, FONT_SUBTITLE))
            logoCell.horizontalAlignment = Element.ALIGN_CENTER
            logoCell.verticalAlignment = Element.ALIGN_CENTER
            logoCell.border = PdfPCell.NO_BORDER
            logoTable.addCell(logoCell)


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

    data class TablaInventario(val tabla: PdfPTable, val costos:Double)

    private fun crearTabla(dataTable: List<ModeloProductoFacturado>): TablaInventario {
        val table1 = PdfPTable(6)
        table1.widthPercentage = 100f
        table1.setWidths(floatArrayOf(2f, 6f, 3f, 2f,1.3f, 2f))
        table1.headerRows = 1
        table1.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table1.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
        var cell: PdfPCell
        run {

            cell = PdfPCell(Phrase("Fecha", FONT_COLUMN))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)
            table1.addCell(cell)

            cell = PdfPCell(Phrase("Producto", FONT_COLUMN))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)
            table1.addCell(cell)

            cell = PdfPCell(Phrase("Administrador", FONT_COLUMN))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)
            table1.addCell(cell)

            cell = PdfPCell(Phrase("ID.", FONT_COLUMN))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)
            table1.addCell(cell)

            cell = PdfPCell(Phrase("Cant.", FONT_COLUMN))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)
            table1.addCell(cell)

            cell = PdfPCell(Phrase("Costo", FONT_COLUMN))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)
            table1.addCell(cell)




        }

        var alternate = false
        val lt_gray = BaseColor(221, 221, 221) //#DDDDDD
        var cell_color: BaseColor?
        val size = dataTable.size
        var totalCostos=0.0
        for (i in 0 until size) {
            cell_color = if (alternate) lt_gray else BaseColor.WHITE
            val temp = dataTable[i]

            cell = PdfPCell()
            setCellFormat(cell, cell_color!!, temp.fecha)
            table1.addCell(cell)

            cell = PdfPCell()
            setCellFormat(cell, cell_color!!, temp.producto)
            cell.horizontalAlignment = Element.ALIGN_LEFT
            table1.addCell(cell)

            cell = PdfPCell()
            setCellFormat(cell, cell_color,temp.vendedor)
            table1.addCell(cell)

            cell = PdfPCell()
            setCellFormat(cell, cell_color, temp.id_pedido.substring(0, 5) )
            table1.addCell(cell)

            cell = PdfPCell()
            setCellFormat(cell, cell_color, temp.cantidad)
            table1.addCell(cell)

            cell = PdfPCell()
            setCellFormat(cell, cell_color, temp.costo.formatoMonenda()!!)
            table1.addCell(cell)



            totalCostos += temp.costo.toDouble()* temp.cantidad.toDouble()

            alternate = !alternate
        }
        return TablaInventario(table1, totalCostos)
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

}