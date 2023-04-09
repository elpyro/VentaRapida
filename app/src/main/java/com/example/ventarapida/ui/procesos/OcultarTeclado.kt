package com.example.ventarapida.ui.procesos

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

class OcultarTeclado(private val context: Context) {

        fun hideKeyboard(view: View) {
                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
}