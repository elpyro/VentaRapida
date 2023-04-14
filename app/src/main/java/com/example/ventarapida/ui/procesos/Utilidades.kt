package com.example.ventarapida.ui.procesos

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.util.*

object Utilidades {


    fun String.eliminarAcentosTildes(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        val pattern = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        return pattern.replace(normalized, "").lowercase(Locale.getDefault())
    }

    fun String.eliminarPuntosComas(): String {
        return this.replace(".", "").replace(",", "")
    }

    fun String.formatoMonenda(): String? {
        val formatoMoneda = DecimalFormat("###,###.###")
        formatoMoneda.decimalFormatSymbols = DecimalFormatSymbols(Locale("es", "CO"))
        formatoMoneda.maximumFractionDigits = 2
        val valorDouble = this.toDoubleOrNull() ?: return this // Retorna el string original si no se puede convertir a double
        val textoFormateado = formatoMoneda.format(valorDouble)
        return textoFormateado
    }
}