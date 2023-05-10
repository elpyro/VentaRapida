package com.example.ventarapida.procesos

import android.app.ProgressDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.text.SimpleDateFormat
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

    fun esperarUnSegundo(){
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun ocultarTeclado(context: Context,view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun separarNumerosDelString(string: String): Pair<String, String?> {
        // Expresión regular para detectar números al final del string
        val regex = "(\\d+)$"
        val matchResult = Regex(regex).find(string)
        if (matchResult != null) {
            // Si hay números al final del string, separarlos del resto del string
            val numeros = matchResult.value
            val resto = string.removeSuffix(numeros)
            return Pair(resto, numeros)
        } else {
            // Si no hay números al final del string, devolver el string completo y null
            return Pair(string.trim(), null)
        }
    }


    fun obtenerFechaActual(): String {
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaActual = Date()
            return formatoFecha.format(fechaActual)
    }
    fun obtenerHoraActual(): String {
        val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val horaActual = Date()
        return formatoHora.format(horaActual)
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