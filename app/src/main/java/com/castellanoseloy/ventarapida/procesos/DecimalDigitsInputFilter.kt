package com.castellanoseloy.ventarapida.procesos

import android.text.InputFilter
import android.text.Spanned


class DecimalDigitsInputFilter(private val decimalDigits: Int) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val input = dest.toString()
        val dotIndex = input.indexOf('.')

        if (dotIndex >= 0) {
            // Hay un punto decimal en el texto existente
            if (dend <= dotIndex) {
                // No permitir más dígitos antes del punto decimal
                return ""
            }

            if (input.length - dotIndex - 1 + source.toString().length > decimalDigits) {
                // No permitir más dígitos después del punto decimal
                return ""
            }
        }

        return null // Deja que el valor de entrada se maneje normalmente
    }
}
