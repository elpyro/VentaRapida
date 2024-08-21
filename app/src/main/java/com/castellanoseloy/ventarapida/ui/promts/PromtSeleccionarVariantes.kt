package com.castellanoseloy.ventarapida.ui.promts


import com.castellanoseloy.ventarapida.R

import com.castellanoseloy.ventarapida.datos.Variable
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import java.util.Locale

class PromtSeleccionarVariantes {

    private lateinit var adaptador: SeleccionarVariantesAdaptador
    private var searchViewVariantes: SearchView? = null
    private var textView_producto: TextView? = null
    private var alertDialog: AlertDialog? = null
    private var recyclerView_productosVariables: RecyclerView? = null
    private var button_agregar: Button? = null
    private var listaVariablesSeleccionadas = mutableListOf<Variable>() // Mover esto fuera del método

    fun agregar(
        context: Context,
        producto: ModeloProducto,
        tipo: String,
        onVariableAgregada: (List<Variable>) -> Unit
    ) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.promt_seleccionar_variaciones, null)
        dialogBuilder.setView(dialogView)

        textView_producto = dialogView.findViewById(R.id.textView_producto)
        recyclerView_productosVariables = dialogView.findViewById(R.id.recyclerView_productosVariables)
        searchViewVariantes = dialogView.findViewById(R.id.searchView_variables)

        textView_producto?.text = producto.nombre
        val gridLayoutManager = GridLayoutManager(context, 1)
        recyclerView_productosVariables?.layoutManager = gridLayoutManager

        adaptador = SeleccionarVariantesAdaptador(producto.listaVariables!!,listaVariablesSeleccionadas) { variable, nuevaCantidad ->
            manejarCambioCantidad(variable, nuevaCantidad)
        }

        searchViewVariantes?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filtro(newText, producto)
                }
                return true
            }
        })
        recyclerView_productosVariables?.adapter = adaptador


        alertDialog = dialogBuilder.create()
        alertDialog?.show()
    }

    private fun filtro(textoParaFiltrar: String, producto: ModeloProducto) {
        val filtro = producto.listaVariables?.filter { variable ->
            variable.nombreVariable.eliminarAcentosTildes().lowercase(Locale.getDefault()).contains(textoParaFiltrar.eliminarAcentosTildes().lowercase(Locale.getDefault())) ||
                    variable.color?.eliminarAcentosTildes()?.lowercase(Locale.getDefault())?.contains(textoParaFiltrar.eliminarAcentosTildes().lowercase(Locale.getDefault())) == true ||
                    variable.tamano?.eliminarAcentosTildes()?.lowercase(Locale.getDefault())?.contains(textoParaFiltrar.eliminarAcentosTildes().lowercase(Locale.getDefault())) == true
        }

        val productosOrdenados = filtro?.sortedBy { it.nombreVariable } ?: listOf()
        adaptador = SeleccionarVariantesAdaptador(
            productosOrdenados,
            listaVariablesSeleccionadas
        ) { variable, nuevaCantidad ->
            manejarCambioCantidad(variable, nuevaCantidad)
        }

        recyclerView_productosVariables?.adapter = adaptador
    }

    private fun manejarCambioCantidad(variable: Variable, nuevaCantidad: Int) {
        // Si la nueva cantidad es mayor que 0
        if (nuevaCantidad > 0) {
            // Buscar si ya existe una variable con el mismo nombre
            val existente = listaVariablesSeleccionadas.find { it.nombreVariable == variable.nombreVariable }

            if (existente != null) {
                // Si existe, sobrescribe la cantidad
                existente.cantidad = nuevaCantidad
            } else {
                // Si no existe, agrega la variable a la lista con la nueva cantidad
                val nuevaVariable = variable.copy(cantidad = nuevaCantidad)
                listaVariablesSeleccionadas.add(nuevaVariable)
            }
        } else {
            // Si la nueva cantidad es 0 o menor, eliminar la variable de la lista si existe
            listaVariablesSeleccionadas.removeAll { it.nombreVariable == variable.nombreVariable }
        }

        Log.d("variables seleccionadas", "Lista actualizada: $listaVariablesSeleccionadas")
    }

    private fun guardar(
        idVariable: String,
        producto: List<Variable>?,
        onVariableAgregada: (List<Variable>) -> Unit
    ) {
        // Implementación de guardar, si es necesario
    }


}
