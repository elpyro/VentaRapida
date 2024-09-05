package com.castellanoseloy.cataplus.ui.promts


import com.castellanoseloy.cataplus.R

import com.castellanoseloy.cataplus.datos.Variable
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import java.util.UUID

class PromtAgregarVariante {
    private var editText_cantidadVariante: EditText? = null
    private var button_eliminar: Button? = null
    private var alertDialog: AlertDialog? = null
    private var editTextNombre: EditText? = null
    private var editTextColor: EditText? = null
    private var editTextTamano: EditText? = null
    private var button_agregar: Button? = null

    fun agregar(
        context: Context,
        producto: List<Variable>?,
        variable: Variable?,
        onVariableAgregada: (List<Variable>) -> Unit
    ) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.promt_agregar_variante, null)
        dialogBuilder.setView(dialogView)

        editTextNombre = dialogView.findViewById(R.id.edit_text_variante)
        editTextColor = dialogView.findViewById(R.id.editText_color)
        editTextTamano = dialogView.findViewById(R.id.editText_tamano)
        button_agregar = dialogView.findViewById(R.id.button_agregar)
        button_eliminar = dialogView.findViewById(R.id.button_eliminar)
        editText_cantidadVariante = dialogView.findViewById(R.id.editText_cantidadVariante)

        Log.d("ModeloProducto", "variable que llega al promt: $variable")

        // Si se está editando una variable existente, cargar sus datos
        var idVariable = variable?.idVariable ?: UUID.randomUUID().toString()
        if (variable != null) {
            editTextNombre?.setText(variable.nombreVariable)
            editTextColor?.setText(variable.color)
            editTextTamano?.setText(variable.tamano)
            editText_cantidadVariante?.setText(variable.cantidad.toString())
            button_eliminar?.visibility = View.VISIBLE
            editText_cantidadVariante?.visibility = View.VISIBLE

        }

        button_agregar?.setOnClickListener {
            guardar(idVariable, producto, onVariableAgregada)
        }

        button_eliminar?.setOnClickListener {
            // Mostrar diálogo de confirmación
            eliminar(context, producto, idVariable, onVariableAgregada)
        }

        alertDialog = dialogBuilder.create()
        alertDialog?.show()
    }

    private fun guardar(
        idVariable: String,
        producto: List<Variable>?,
        onVariableAgregada: (List<Variable>) -> Unit
    ) {
        val nombre = editTextNombre?.text.toString().trim()
        val color = editTextColor?.text.toString().trim()
        val tamano = editTextTamano?.text.toString().trim()
        val cantidad = editText_cantidadVariante?.text.toString().toIntOrNull() ?: 0

        if (nombre.isNotEmpty()) {
            val nuevaVariable = Variable(
                idVariable = idVariable,
                nombreVariable = nombre,
                color = color,
                tamano = tamano,
                cantidad = cantidad
            )

            // Crear una lista mutable a partir de la lista de productos
            val listaActualizada = producto?.toMutableList() ?: mutableListOf()

            // Buscar si la variable existe para actualizarla
            val index = listaActualizada.indexOfFirst { it.idVariable == idVariable }
            if (index != -1) {
                // Si existe, actualizar la variable
                listaActualizada[index] = nuevaVariable
            } else {
                // Si no existe, agregarla
                listaActualizada.add(nuevaVariable)
            }

            // Ejecutar el callback pasando la lista actualizada
            onVariableAgregada(listaActualizada)

            // Cerrar el diálogo
            alertDialog?.dismiss()
        } else {
            editTextNombre?.error = "El nombre de la variable es requerido"
        }
    }

    private fun eliminar(
        context: Context,
        producto: List<Variable>?,
        idVariable: String,
        onVariableAgregada: (List<Variable>) -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle("Confirmación")
            .setMessage("¿Estás seguro de que quieres eliminar esta variable?")
            .setPositiveButton("Eliminar") { _, _ ->
                // Crear una lista mutable a partir de la lista de productos
                val listaActualizada = producto?.toMutableList() ?: mutableListOf()

                // Buscar y eliminar la variable de la lista
                val index = listaActualizada.indexOfFirst { it.idVariable == idVariable }
                if (index != -1) {
                    listaActualizada.removeAt(index)
                }

                // Ejecutar el callback pasando la lista actualizada
                onVariableAgregada(listaActualizada)

                // Cerrar el diálogo
                alertDialog?.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
