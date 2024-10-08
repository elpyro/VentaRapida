package com.castellanoseloy.cataplus.procesos.crearPdf

import android.content.Context
import android.os.Environment
import android.util.Log
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.datos.ModeloFactura
import com.castellanoseloy.cataplus.datos.ModeloProductoFacturado
import com.castellanoseloy.cataplus.procesos.PageNumeration
import com.castellanoseloy.cataplus.procesos.Utilidades
import com.castellanoseloy.cataplus.procesos.Utilidades.formatoMonenda
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

class CrearPdfFacturaOCompra {

    private val FONT_TITLE = Font(Font.FontFamily.TIMES_ROMAN, 20f, Font.BOLD)
    private val FONT_SUBTITLE = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.BOLD)
    private val DATOSEMPRESAFUENTE = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.ITALIC)
    private val FONT_CURSIVA=Font(Font.FontFamily.HELVETICA, 12f, Font.ITALIC)
    private val FONT_GARANTIA=Font(Font.FontFamily.HELVETICA, 7f, Font.ITALIC)
    private val FONT_CELL = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.NORMAL)
    private val FONT_COLUMN = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.NORMAL)
    private val FONT_VARIANTES =Font(Font.FontFamily.HELVETICA, 6f, Font.ITALIC)

    fun facturaOCompra(
        context: Context,
        modeloFactura: ModeloFactura,
        tipo: String,
        listaProductos: ArrayList<ModeloProductoFacturado>
    ) {

        //ordenar alfabetico
        listaProductos.sortBy { it.producto }

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Cataplus.pdf")
        val outputStream = FileOutputStream(file)

        // Crea el documento PDF
        val document = Document()
        document.setMargins(24f, 24f, 32f, 32f)
        PdfWriter.getInstance(document, outputStream).setPageEvent(PageNumeration())
        document.open()

        metadata(document)
        cabezera( document,tipo, modeloFactura, context)
        document.add(Paragraph("\n"))
        datosFactura( document, tipo, modeloFactura, listaProductos)
        document.add(Paragraph("\n"))
        document.add(crearTabla(tipo, listaProductos))
        document.add(Paragraph("\n"))
        pieDocumento(document,tipo)


        document.close()
        outputStream.close()
    }

    private fun metadata(document: Document) {
        document.addTitle("Cataplus")
        document.addSubject("Factura")
        document.addAuthor("Eloy Castellanos")
        document.addCreator("Eloy Castellanos")
    }

    private fun cabezera( document: Document, tipo:String, modeloFactura: ModeloFactura, context:Context) {

        var titulo: Paragraph? =null
        if (tipo == "Compra"){
            titulo= Paragraph("Compra", FONT_TITLE)
        }else{
            titulo= Paragraph("Recibo de Venta", FONT_TITLE)
        }

        titulo.alignment = Element.ALIGN_CENTER


        if (DatosPersitidos.datosEmpresa!=null) {
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

                var temp = Paragraph("Fecha: "+modeloFactura.fecha,FONT_SUBTITLE)
                temp.alignment = Element.ALIGN_LEFT
                cell.addElement(temp)

                temp = Paragraph("Ref: "+modeloFactura.id_pedido.substring(0, 5),FONT_SUBTITLE)
                temp.alignment = Element.ALIGN_LEFT
                cell.addElement(temp)

                temp = Paragraph(DatosPersitidos.datosEmpresa.nombre,DATOSEMPRESAFUENTE)
                temp.alignment = Element.ALIGN_LEFT
                cell.addElement(temp)

                temp = Paragraph(DatosPersitidos.datosEmpresa.telefono1 ,DATOSEMPRESAFUENTE)
                temp.alignment = Element.ALIGN_LEFT
                cell.addElement(temp)

                temp = Paragraph(DatosPersitidos.datosEmpresa.telefono2 ,DATOSEMPRESAFUENTE)
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

            run{
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

                var usuario="Vendedor"
                if(tipo.equals("Compra"))usuario="Surtidor"
                logoCell = PdfPCell( Phrase( "$usuario: "+modeloFactura.nombre_vendedor, DATOSEMPRESAFUENTE ))
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
    }

    private fun datosFactura(
        document: Document,
        tipo: String,
        modeloFactura: ModeloFactura,
        listaProductos: List<ModeloProductoFacturado>
    ) {

        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(5f,  5f))
        table.defaultCell.border = PdfPCell.NO_BORDER
        table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table.defaultCell.horizontalAlignment = Element.ALIGN_CENTER

        var cell: PdfPCell

        run {
            //  CELDA1
            cell = PdfPCell()
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.border = PdfPCell.NO_BORDER


            if (tipo == "Compra"){
                var temp = Paragraph("Tienda: "+modeloFactura.nombre,FONT_CELL)
                temp.alignment = Element.ALIGN_LEFT
                temp.setLeading(0f, 1f)
                cell.addElement(temp)
            }else{
                var temp = Paragraph("Cliente: "+modeloFactura.nombre,FONT_CELL)
                temp.alignment = Element.ALIGN_LEFT
                temp.setLeading(0f, 1f)
                cell.addElement(temp)

                temp = Paragraph("CC: "+modeloFactura.documento,FONT_CELL)
                temp.alignment = Element.ALIGN_LEFT
                temp.setLeading(0f, 1f)
                cell.addElement(temp)

                temp = Paragraph("Telefono: "+modeloFactura.telefono,FONT_CELL)
                temp.alignment = Element.ALIGN_LEFT
                temp.setLeading(0f, 1f)
                cell.addElement(temp)

                temp = Paragraph("Dirección: "+modeloFactura.direccion,FONT_CELL)
                temp.alignment = Element.ALIGN_LEFT
                temp.setLeading(0f, 1f)
                cell.addElement(temp)

            }
            cell.setLeading(0f, 1f)
            table.addCell(cell)
        }

        run {
            //INCLUIR ADICIONALMENTE LA PREFERNCIA INF SUPERIOR
            //  CELDA2
            cell = PdfPCell()
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.border = PdfPCell.NO_BORDER

            if(tipo=="Factura") {

                var temp = Paragraph(DatosPersitidos.preferencia_informacion_superior, FONT_CELL)
                temp.alignment = Element.ALIGN_LEFT
                temp.setLeading(0f, 1f)
                cell.addElement(temp)

            }

            table.addCell(cell)
        }

        document.add(table)
        document.add(Paragraph("\n"))

        val tablaTotales = PdfPTable(2)
        tablaTotales.widthPercentage = 100f
        tablaTotales.setWidths(floatArrayOf(7f,  3f))
        tablaTotales.defaultCell.border = PdfPCell.NO_BORDER
        tablaTotales.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        tablaTotales.defaultCell.horizontalAlignment = Element.ALIGN_CENTER


        run {

            cell = PdfPCell()
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.border = PdfPCell.NO_BORDER

            val referencias=listaProductos.size
            val items= listaProductos.sumBy { it.cantidad.toInt() }.toString()

            var temp = Paragraph("Referencias: $referencias, Items: ${items}", FONT_CURSIVA)
            temp.alignment = Element.ALIGN_LEFT
            cell.addElement(temp)

            tablaTotales.addCell(cell)
        }

        run {
            //  CELDA2
            cell = PdfPCell()
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.border = PdfPCell.NO_BORDER

            var subTotal: Double= 0.0

            if(tipo=="Compra")
                subTotal =
                    listaProductos.sumByDouble { it.cantidad.toDouble() * it.costo.toDouble() }

            if(tipo=="Factura")
                subTotal =
                    listaProductos.sumByDouble { it.cantidad.toDouble() * it.venta.toDouble() }

            val descuento = if (modeloFactura.descuento.isNotBlank()) modeloFactura.descuento else "0"
            val envio =  if (modeloFactura.envio.isNotBlank()) modeloFactura.envio else "0"

            var totalDescuento = subTotal!!.toDouble()
            val porcentajeDescuento = descuento.toDouble() / 100
            totalDescuento *= (1 - porcentajeDescuento)
            totalDescuento += envio.toDouble()
            val total = totalDescuento


            var temp = Paragraph("Total: " + total.toString().formatoMonenda(), FONT_SUBTITLE)
            temp.alignment = Element.ALIGN_RIGHT
            cell.addElement(temp)

            var precioModificado=false
            if(descuento!="0"){
                precioModificado=true
                temp = Paragraph("Descuento: %${descuento.formatoMonenda()}",FONT_CELL)
                temp.alignment = Element.ALIGN_RIGHT
                cell.addElement(temp)
            }

            if(envio!="0") {
                precioModificado=true
                temp = Paragraph("Envio: ${envio.formatoMonenda()}", FONT_CELL)
                temp.alignment = Element.ALIGN_RIGHT
                cell.addElement(temp)
            }
            if(precioModificado==true){
                temp = Paragraph("Sub-Total: "+subTotal.toString().formatoMonenda(),FONT_CELL)
                temp.alignment = Element.ALIGN_RIGHT
                cell.addElement(temp)
            }


            tablaTotales.addCell(cell)
        }

        document.add(tablaTotales)
    }


    private fun crearTabla(tipo: String, dataTable: List<ModeloProductoFacturado>): PdfPTable? {
        val table1 = PdfPTable(4)
        table1.widthPercentage = 100f
        table1.setWidths(floatArrayOf(8f,1.5f,3f,4f))
        table1.headerRows = 1
        table1.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table1.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
        var cell: PdfPCell
        run {
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

            cell = PdfPCell(Phrase("Precio", FONT_COLUMN))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)
            table1.addCell(cell)

            cell = PdfPCell(Phrase("Total", FONT_COLUMN))
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

            var precio= "0"
            if (tipo=="Compra")  precio= temp.costo
            if (tipo=="Factura")  precio= temp.venta

            Log.d("DatosPdf","producto ${temp}")
            // Inicializar la cadena con el nombre del producto


            // Crear un StringBuilder para construir la lista de variables
            val stringBuilder = StringBuilder()

            temp.listaVariables?.let { lista ->
                if (lista.isNotEmpty()) {
                    // Filtrar las variables con cantidad mayor o igual a 1
                    val variablesFiltradas = lista.filter { it.cantidad >= 1 }

                    if (variablesFiltradas.isNotEmpty()) {
                        stringBuilder.append("-Variantes: \n")  // Puedes personalizar este prefijo si lo prefieres
                        // Usar barra vertical '|' como separador entre las variables
                        val variablesString = variablesFiltradas.joinToString(" | ") { variable ->
                            "${variable.nombreVariable}: ${variable.cantidad}"
                        }
                        stringBuilder.append(variablesString)
                    }
                }
            }

// Crear un Paragraph para el nombre del producto con la fuente original
            val nombreParagraph = Paragraph(temp.producto, FONT_CELL)

// Crear un Paragraph para las variantes con la fuente FONT_VARIANTES
            val variantesParagraph = Paragraph(stringBuilder.toString(), FONT_VARIANTES)

// Crear un contenedor Paragraph que combine ambos elementos
            val combinedParagraph = Paragraph()
            combinedParagraph.add(nombreParagraph)
            combinedParagraph.add(variantesParagraph)

// Crear la celda y agregar el párrafo combinado
            cell = PdfPCell()
            cell.addElement(combinedParagraph)
            cell.backgroundColor = cell_color
            cell.horizontalAlignment = Element.ALIGN_LEFT
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            table1.addCell(cell)


            cell = PdfPCell()
            setCellFormat(cell, cell_color!!, temp.cantidad)
            table1.addCell(cell)

            cell = PdfPCell()
            setCellFormat(cell, cell_color!!, precio.formatoMonenda()!!)
            table1.addCell(cell)

            val totalProducto=temp.cantidad.toInt() * precio.toDouble()
            cell = PdfPCell()
            setCellFormat(cell, cell_color!!, totalProducto.toString().formatoMonenda()!!)
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
        if(tipo=="Factura"){
            document.add(Paragraph(""))
            document.add(
                Paragraph(
                    DatosPersitidos.preferencia_informacion_inferior,FONT_CELL)
            )

            document.add(Paragraph("\n"))
            if(!DatosPersitidos.datosEmpresa.garantia.isEmpty()) {
                document.add(
                    Paragraph(
                       "Terminos de garantía:")
                )
            }
            document.add(Paragraph("\n"))
            document.add(
                Paragraph(
                    DatosPersitidos.datosEmpresa.garantia,FONT_GARANTIA)
            )
        }
    }


}
