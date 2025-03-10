package com.castellanoseloy.ventarapida.procesos.crearPdf

import android.content.Context
import android.os.Environment
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.procesos.PageNumeration
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.formatoMonenda
import com.castellanoseloy.ventarapida.procesos.Utilidades.obtenerFechaActual
import com.castellanoseloy.ventarapida.procesos.Utilidades.obtenerHoraActual
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
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

import java.net.URL

import java.util.concurrent.CountDownLatch


class CrearPdfCatalogo {

    private val FONT_TITLE = Font(Font.FontFamily.TIMES_ROMAN, 20f, Font.BOLD)
    private val FONT_SUBTITLE = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.BOLD)
    private val DATOSEMPRESAFUENTE = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.ITALIC)
    private val FONT_CELL = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.NORMAL)
    private val FONT_COLUMN = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.NORMAL)
    private val FONT_VARIANTES =Font(Font.FontFamily.HELVETICA, 6f, Font.ITALIC)

    suspend fun catalogo(
        context: Context,
        listaProductos: ArrayList<ModeloProducto>,
        aumento: Double? =null
    ) {

        //ordenar alfabetico
        listaProductos.sortBy { it.nombre }

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Cataplus.pdf")
        val outputStream = FileOutputStream(file)

        // Crea el documento PDF
        val document = Document()
        document.setMargins(24f, 24f, 32f, 32f)
        PdfWriter.getInstance(document, outputStream).setPageEvent(PageNumeration())
        document.open()

        metadata(document)
        cabezera(document, context)


        val tablaInventario = crearTabla(listaProductos,aumento ) // Pasar el CountDownLatch a la función crearTabla

        document.add(Paragraph("\n"))
        document.add(tablaInventario.tabla)

        document.close()
        outputStream.close()
    }

    private fun metadata(document: Document) {
        document.addTitle("Cataplus")
        document.addSubject("Catalogo")
        document.addAuthor("Eloy Castellanos")
        document.addCreator("Eloy Castellanos")
    }

    private fun cabezera(document: Document, context: Context) {

        val titulo: Paragraph?
        titulo = Paragraph("Catálogo", FONT_TITLE)
        titulo.alignment = Element.ALIGN_CENTER


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

            val temp = Paragraph(titulo)
            temp.alignment = Element.ALIGN_CENTER
            cell.addElement(temp)

            table.addCell(cell)
        }

        run {
            // CELDA3 - Logo
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

    data class TablaInventario(val tabla: PdfPTable)

    private suspend fun crearTabla(
        dataTable: List<ModeloProducto>,
        porcentajeAjuste: Double? = null // Nuevo parámetro opcional para ajustar precio
    ): TablaInventario {
        val table1 = PdfPTable(5)
        table1.widthPercentage = 100f
        table1.setWidths(floatArrayOf(1f, 8f, 1.5f, 3f, 2.5f))
        table1.headerRows = 1
        table1.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table1.defaultCell.horizontalAlignment = Element.ALIGN_CENTER

        var cell: PdfPCell
        val headers = listOf("Id", "Producto", "Cant.", "Precio", "Imagen")
        headers.forEach { header ->
            cell = PdfPCell(Phrase(header, FONT_COLUMN))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)
            table1.addCell(cell)
        }

        var alternate = false
        val ltGray = BaseColor(221, 221, 221) // #DDDDDD
        val images: List<Image?> = loadImagesAsync(dataTable)

        for ((index, temp) in dataTable.withIndex()) {
            val cellColor = if (alternate) ltGray else BaseColor.WHITE

            // **Calcular precio final**
            var precioFinal :Double = temp.p_diamante.toDouble()
            if (porcentajeAjuste != null) {
                precioFinal = temp.p_compra.toDouble()
            }

            // **Aplicar porcentaje de ajuste si es necesario**
            if (porcentajeAjuste != null) {
                precioFinal += precioFinal * (porcentajeAjuste / 100)
            }

            // **Celda ID**
            cell = PdfPCell()
            setCellFormat(cell, cellColor, (index + 1).toString())
            table1.addCell(cell)

            // **Celda Producto con Variantes**
            val stringBuilder = StringBuilder()
            temp.listaVariables?.filter { it.cantidad >= 1 }?.let { variablesFiltradas ->
                if (variablesFiltradas.isNotEmpty()) {
                    stringBuilder.append("-Variantes: \n")
                    stringBuilder.append(
                        variablesFiltradas.joinToString(" | ") { "${it.nombreVariable}: ${it.cantidad}" }
                    )
                }
            }

            val nombreParagraph = Paragraph(temp.nombre, FONT_CELL)
            val variantesParagraph = Paragraph(stringBuilder.toString(), FONT_VARIANTES)
            val combinedParagraph = Paragraph().apply {
                add(nombreParagraph)
                add(variantesParagraph)
            }

            cell = PdfPCell()
            cell.addElement(combinedParagraph)
            cell.backgroundColor = cellColor
            cell.horizontalAlignment = Element.ALIGN_LEFT
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            table1.addCell(cell)

            // **Celda Cantidad**
            val cantidadFinal = if (temp.cantidad.isNotEmpty() && temp.cantidad.toInt() >= 1) temp.cantidad else ""
            cell = PdfPCell()
            setCellFormat(cell, cellColor, cantidadFinal)
            table1.addCell(cell)

            // **Celda Precio**
            cell = PdfPCell()
            setCellFormat(cell, cellColor, precioFinal.toString().formatoMonenda()!!)
            table1.addCell(cell)

            // **Celda Imagen**
            if (images[index] != null) {
                val logoCell = PdfPCell(images[index])
                logoCell.horizontalAlignment = Element.ALIGN_CENTER
                logoCell.verticalAlignment = Element.ALIGN_MIDDLE
                logoCell.backgroundColor = cellColor
                logoCell.setPadding(2F)
                table1.addCell(logoCell)
            } else {
                cell = PdfPCell()
                setCellFormat(cell, cellColor, "No disponible")
                cell.setPadding(4F)
                cell.verticalAlignment = Element.ALIGN_MIDDLE
                table1.addCell(cell)
            }

            alternate = !alternate
        }

        return TablaInventario(table1)
    }


    private suspend fun loadImage(imageUrl: String): Image? {
        return withContext(Dispatchers.IO) {
            try {
                if (imageUrl.isNotEmpty()) {
                    val connection = URL(imageUrl).openConnection()
                    connection.connect()
                    val stream = connection.getInputStream()
                    val byteArray = stream.readBytes()
                    val imagenProducto = Image.getInstance(byteArray)
                    imagenProducto.scaleToFit(70f, 70f)
                    imagenProducto
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private suspend fun loadImagesAsync(dataTable: List<ModeloProducto>): List<Image?> {
        return coroutineScope {
            dataTable.map { modeloProducto ->
                async {
                    loadImage(modeloProducto.url)
                }
            }.awaitAll()
        }
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