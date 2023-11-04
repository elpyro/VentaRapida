package com.castellanoseloy.ventarapida.procesos

import com.castellanoseloy.ventarapida.MainActivity
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfWriter


class PageNumeration : PdfPageEventHelper() {
    override fun onEndPage(writer: PdfWriter, document: Document) {
        val page = writer.pageNumber
        val text = "Página $page"
        val content = writer.directContent
        val font = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED)

        // Agregar texto adicional alineado a la izquierda
        val additionalText = "${MainActivity.datosEmpresa.pagina}  ${MainActivity.datosEmpresa.direccion}"
        val additionalContent = writer.directContent
        val additionalFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED)
        val additionalX = document.left() + 20f
        val additionalY = document.bottom() - 20f
        additionalContent.beginText()
        additionalContent.setFontAndSize(additionalFont, 10f)
        additionalContent.setTextMatrix(additionalX, additionalY)
        additionalContent.showText(additionalText)
        additionalContent.endText()

        val x = document.right() - 80f
        val y = document.bottom() - 20f
        content.beginText()
        content.setFontAndSize(font, 10f)
        content.setTextMatrix(x, y)
        content.showText(text)
        content.endText()
    }
}
