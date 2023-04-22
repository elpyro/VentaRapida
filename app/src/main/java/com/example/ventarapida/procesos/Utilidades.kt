package com.example.ventarapida.procesos

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
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

    fun String.eliminarPuntosComasLetras(): String {
        return this.replace(Regex("[^\\d.]"), "")
            .replace(".", "")
            .replace(",", "")
    }

    fun EditText.escribirFormatoMoneda() {
        val textWatcher = object : TextWatcher {
            var isProgrammaticChange = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isProgrammaticChange && s != null) {
                    removeTextChangedListener(this)
                    val precio = s.toString().eliminarPuntosComasLetras()
                    setText(precio.formatoMonenda())
                    setSelection(text.length)
                    addTextChangedListener(this)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        addTextChangedListener(textWatcher)
    }

    fun String.formatoMonenda(): String? {
        val formatoMoneda = DecimalFormat("###,###.###")
        formatoMoneda.decimalFormatSymbols = DecimalFormatSymbols(Locale("es", "CO"))
        formatoMoneda.maximumFractionDigits = 2
        val valorDouble = this.toDoubleOrNull()
            ?: return this // Retorna el string original si no se puede convertir a double
        return formatoMoneda.format(valorDouble)
    }
}