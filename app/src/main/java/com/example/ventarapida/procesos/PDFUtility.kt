package com.example.ventarapida.procesos

import android.content.Context
import com.itextpdf.text.*
import com.itextpdf.text.pdf.Barcode128
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.util.Date


class PDFUtility {

//        private const val TAG = "PDFUtility"
    val FONT_TITLE = Font(Font.FontFamily.TIMES_ROMAN, 16f, Font.BOLD)
    val FONT_SUBTITLE = Font(Font.FontFamily.TIMES_ROMAN, 10f, Font.NORMAL)
    val FONT_CELL = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.NORMAL)
    val FONT_COLUMN = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.NORMAL)



    interface OnDocumentClose {
        fun onPDFDocumentClose(file: File)
    }


        fun createPdf(
            context: Context,
            callback: OnDocumentClose,
            items: List<Array<String>>,
            filePath: String,
        ) {
            val document = Document()
            val file = File(filePath)
            val writer = PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()
            setMetaData(document)
            addHeader(context, document)
            document.add(Paragraph("\n\n"))
            document.add(createDataTable(items))
            document.add(Paragraph("\n\n"))
            document.add(createSignBox())
            document.close()
            writer.close()
            callback.onPDFDocumentClose(file)
        }

        private fun addEmptyLine(document: Document, number: Int) {
            repeat(number) {
                document.add(Paragraph("\n"))
            }
        }

        private fun setMetaData(document: Document) {
            document.addTitle("PDF Title")
            document.addSubject("PDF Subject")
            document.addKeywords("PDF, iText")
            document.addAuthor("Author Name")
            document.addCreator("Creator Name")
        }

        private fun addHeader(context: Context, document: Document) {
            val header = Paragraph("PDF Header", FONT_TITLE)
            header.alignment = Element.ALIGN_CENTER
            document.add(header)
            document.add(Paragraph("PDF Subheader", FONT_SUBTITLE))
            document.add(Paragraph("PDF Date: ${Date()}"))
        }

        private fun createDataTable(dataTable: List<Array<String>>): PdfPTable {
            val table = PdfPTable(dataTable[0].size)
            dataTable.forEach { row ->
                row.forEach { cellValue ->
                    val cell = PdfPCell(Phrase(cellValue, FONT_CELL))
                    cell.horizontalAlignment = Element.ALIGN_CENTER
                    table.addCell(cell)
                }
            }
            return table
        }

        private fun createSignBox(): PdfPTable {
            val table = PdfPTable(1)
            val cell = PdfPCell(Phrase("Signature Box"))
            cell.border = Rectangle.BOTTOM
            cell.minimumHeight = 60f
            table.addCell(cell)
            return table
        }

        private fun getImage(imageByte: ByteArray, isTintingRequired: Boolean): Image {
            val image = Image.getInstance(imageByte)
            if (isTintingRequired) {
                image.backgroundColor = BaseColor.YELLOW
            }
            return image
        }

        private fun getBarcodeImage(pdfWriter: PdfWriter, barcodeText: String): Image {
            val barcode = Barcode128()
            barcode.code = barcodeText
            return barcode.createImageWithBarcode(
                pdfWriter.directContent,
                BaseColor.BLACK,
                BaseColor.BLACK
            )
        }

//    private fun createDataTable(dataTable: List<Array<String>>): PdfPTable? {
//        val table1 = PdfPTable(2)
//        table1.widthPercentage = 100f
//        table1.setWidths(floatArrayOf(1f, 2f))
//        table1.headerRows = 1
//        table1.defaultCell.verticalAlignment = Element.ALIGN_CENTER
//        table1.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
//        var cell: PdfPCell
//        run {
//            cell = PdfPCell(Phrase("COLUMN - 1", FONT_COLUMN))
//            cell.horizontalAlignment = Element.ALIGN_CENTER
//            cell.verticalAlignment = Element.ALIGN_MIDDLE
//            cell.setPadding(4f)
//            table1.addCell(cell)
//            cell = PdfPCell(Phrase("COLUMN - 2", FONT_COLUMN))
//            cell.horizontalAlignment = Element.ALIGN_CENTER
//            cell.verticalAlignment = Element.ALIGN_MIDDLE
//            cell.setPadding(4f)
//            table1.addCell(cell)
//        }
//        val top_bottom_Padding = 8f
//        val left_right_Padding = 4f
//        var alternate = false
//        val lt_gray = BaseColor(221, 221, 221) //#DDDDDD
//        var cell_color: BaseColor?
//        val size = dataTable.size
//        for (i in 0 until size) {
//            cell_color = if (alternate) lt_gray else BaseColor.WHITE
//            val temp = dataTable[i]
//            cell = PdfPCell(Phrase(temp[0], FONT_CELL))
//            cell.horizontalAlignment = Element.ALIGN_LEFT
//            cell.verticalAlignment = Element.ALIGN_MIDDLE
//            cell.paddingLeft = left_right_Padding
//            cell.paddingRight = left_right_Padding
//            cell.paddingTop = top_bottom_Padding
//            cell.paddingBottom = top_bottom_Padding
//            cell.backgroundColor = cell_color
//            table1.addCell(cell)
//            cell = PdfPCell(Phrase(temp[1], FONT_CELL))
//            cell.horizontalAlignment = Element.ALIGN_CENTER
//            cell.verticalAlignment = Element.ALIGN_MIDDLE
//            cell.paddingLeft = left_right_Padding
//            cell.paddingRight = left_right_Padding
//            cell.paddingTop = top_bottom_Padding
//            cell.paddingBottom = top_bottom_Padding
//            cell.backgroundColor = cell_color
//            table1.addCell(cell)
//            alternate = !alternate
//        }
//        return table1
//    }

}
