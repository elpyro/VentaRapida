package com.castellanoseloy.cataplus.ui.promts


import com.castellanoseloy.cataplus.R

import com.castellanoseloy.cataplus.datos.Variable
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.cataplus.datos.ModeloProducto
import com.castellanoseloy.cataplus.datos.ModeloProductoFacturado
import com.castellanoseloy.cataplus.procesos.Utilidades.eliminarAcentosTildes

import java.util.Locale

class PromtEditarVariantesSeleccionadas {

    private lateinit var adaptador: SeleccionarVariantesAdaptador
    private var searchViewVariantes: SearchView? = null
    private var textView_producto: TextView? = null
    private var alertDialog: AlertDialog? = null
    private var recyclerView_productosVariables: RecyclerView? = null
    private var button_agregar: Button? = null
    private var imageView_eliminarCarrito: ImageView? = null
    private var listaVariablesSeleccionadas = mutableListOf<Variable>()



    fun agregar(
        context: Context,
        producto: ModeloProducto,
        productosSeleccionados : MutableMap<ModeloProductoFacturado, Int>,
        onVariableAgregada: (List<Variable>) -> Unit
    ) {
        listaVariablesSeleccionadas.clear()


        cargarGuardados(producto,productosSeleccionados)

        Log.d("Compra", "Lista actualizada: $listaVariablesSeleccionadas")

        // Crear el diálogo
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.promt_seleccionar_variaciones, null)
        dialogBuilder.setView(dialogView)

        // Inicializar vistas
        textView_producto = dialogView.findViewById(R.id.textView_producto)
        recyclerView_productosVariables = dialogView.findViewById(R.id.recyclerView_productosVariables)
        searchViewVariantes = dialogView.findViewById(R.id.searchView_variables)
        button_agregar = dialogView.findViewById(R.id.button_agregar)
        imageView_eliminarCarrito = dialogView.findViewById(R.id.imageView_eliminarCarrito)

        // Configurar el botón agregar
        button_agregar?.setOnClickListener {

//            if (listaVariablesSeleccionadas.isEmpty()) {
//                // Si no hay elementos seleccionados, realizar la función de eliminar
//                eliminar(onVariableAgregada)
//            } else {
//                // Buscar la clave (ModeloProducto) en el mapa basado en el id
//                val claveAEliminar = productosSeleccionados.keys.find { it.id == producto.id }
//
//                // Si se encuentra una clave que coincide, eliminar la entrada del mapa
//                if (claveAEliminar != null) {
//                    productosSeleccionados.remove(claveAEliminar)
//                    Log.w("Compra", "Producto con id ${producto.id}  eliminado")
//                } else {
//                    Log.w("Compra", "Producto con id ${producto.id} no encontrado")
//                }
//
//                // Verificar el estado del mapa después de la eliminación
//                Log.d("Compra", "Lista actualizada: ${productosSeleccionados.keys.map { it.id }}")
//
//
//                onVariableAgregada(listaVariablesSeleccionadas)
//                alertDialog?.dismiss()
//            }
        }

        // Configurar el botón eliminar carrito
        imageView_eliminarCarrito?.setOnClickListener {
            eliminar(onVariableAgregada)
        }

        // Mostrar nombre del producto
        textView_producto?.text = producto.nombre

        // Configurar el RecyclerView
        recyclerView_productosVariables?.layoutManager = GridLayoutManager(context, 1)
        adaptador = SeleccionarVariantesAdaptador(
            producto.listaVariables!! as MutableList<Variable>,
            listaVariablesSeleccionadas
        )
        recyclerView_productosVariables?.adapter = adaptador

        // Configurar el SearchView
        searchViewVariantes?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filtro(newText, producto)
                }
                return true
            }
        })
        //desbloquea searchview al seleccionarlo
        searchViewVariantes?.setOnClickListener {
            searchViewVariantes?.isIconified=false
        }

        // Mostrar el diálogo
        alertDialog = dialogBuilder.create()
        alertDialog?.show()
    }

    private fun eliminar(onVariableAgregada: (List<Variable>) -> Unit) {
        listaVariablesSeleccionadas.clear()
        onVariableAgregada(listaVariablesSeleccionadas)
        alertDialog?.dismiss()
    }

    private fun cargarGuardados(
        producto: ModeloProducto,
        productosSeleccionados: MutableMap<ModeloProductoFacturado, Int>
    ) {
        // Buscar el producto en el mapa basado en el id
        val productoExistente = productosSeleccionados.keys.find { it.id_producto == producto.id }

        productoExistente?.let { producto ->
            // Obtener la cantidad total del producto en el mapa
            val cantidadTotal = productosSeleccionados[producto] ?: 0

            // Obtener las variables del producto existente
            val variablesExistentes = producto.listaVariables ?: emptyList()

            // Actualizar o agregar las variables en la lista seleccionada
            variablesExistentes.forEach { variable ->
                // Buscar la variable en la lista seleccionada
                val variableEnLista =
                    listaVariablesSeleccionadas.find { it.idVariable == variable.idVariable }

                if (variableEnLista != null) {
                    // Actualizar la cantidad de la variable existente con la cantidad específica de la variable
                    variableEnLista.cantidad = variable.cantidad ?: 0
                } else {
                    // Agregar la variable nueva con la cantidad específica
                    listaVariablesSeleccionadas.add(
                        variable.copy(
                            cantidad = variable.cantidad ?: 0
                        )
                    )
                }
            }

            // Opcional: Eliminar variables de la lista seleccionada que no están en el producto existente
            listaVariablesSeleccionadas.removeAll { variable ->
                !variablesExistentes.any { it.idVariable == variable.idVariable }
            }
        }
    }
    private fun filtro(textoParaFiltrar: String, producto: ModeloProducto) {
        val filtro = producto.listaVariables?.filter { variable ->
            variable.nombreVariable.eliminarAcentosTildes().lowercase(Locale.getDefault())
                .contains(textoParaFiltrar.eliminarAcentosTildes().lowercase(Locale.getDefault())) ||
                    variable.color?.eliminarAcentosTildes()?.lowercase(Locale.getDefault())
                        ?.contains(textoParaFiltrar.eliminarAcentosTildes().lowercase(Locale.getDefault())) == true ||
                    variable.tamano?.eliminarAcentosTildes()?.lowercase(Locale.getDefault())
                        ?.contains(textoParaFiltrar.eliminarAcentosTildes().lowercase(Locale.getDefault())) == true
        }

        val productosOrdenados = filtro?.sortedBy { it.nombreVariable } ?: listOf()
        adaptador = SeleccionarVariantesAdaptador(
            productosOrdenados as MutableList<Variable>,
            listaVariablesSeleccionadas
        )
        recyclerView_productosVariables?.adapter = adaptador
    }

    private fun manejarCambioCantidad(variable: Variable, nuevaCantidad: Int) {
        if (nuevaCantidad > 0) {
            val existente = listaVariablesSeleccionadas.find { it.nombreVariable == variable.nombreVariable }

            if (existente != null) {
                existente.cantidad = nuevaCantidad
            } else {
                listaVariablesSeleccionadas.add(variable.copy(cantidad = nuevaCantidad))
            }
        } else {
            listaVariablesSeleccionadas.removeAll { it.nombreVariable == variable.nombreVariable }
        }

        Log.d("Compra", "Lista actualizada: $listaVariablesSeleccionadas")
    }

    private fun actualizarTextoProducto(producto: ModeloProducto, productosSeleccionados: MutableMap<ModeloProducto, Int>) {
        val cantidadTotal = productosSeleccionados[producto] ?: 0
        textView_producto?.text = "${producto.nombre} - Cantidad seleccionada: $cantidadTotal"
    }

}
