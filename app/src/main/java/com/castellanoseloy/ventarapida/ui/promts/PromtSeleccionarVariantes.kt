package com.castellanoseloy.ventarapida.ui.promts


import android.Manifest
import android.app.Activity
import com.castellanoseloy.ventarapida.R

import com.castellanoseloy.ventarapida.datos.Variable
import android.app.AlertDialog
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.procesos.Utilidades.eliminarAcentosTildes

import java.util.Locale

class PromtSeleccionarVariantes : Fragment() {

    private lateinit var adaptador: SeleccionarVariantesAdaptador
    private var searchViewVariantes: SearchView? = null
    private var textView_producto: TextView? = null
    private var alertDialog: AlertDialog? = null
    private var recyclerView_productosVariables: RecyclerView? = null
    private var button_agregar: Button? = null
    private var imageView_eliminarCarrito: ImageView? = null
    private var listaVariablesSeleccionadas = mutableListOf<Variable>()
    private var textView_listaSeleccion: TextView? = null
    val REQUEST_CODE_VOICE_SEARCH = 1100

    fun agregar(
        context: Context,
        producto: ModeloProducto,
        productosSeleccionados : MutableMap<ModeloProducto, Int>,
        onVariableAgregada: (List<Variable>) -> Unit
    ) {



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
        textView_listaSeleccion=dialogView.findViewById(R.id.textView_listaSeleccion)


        Log.d("PromtSeleccionarVariables", "Pruductos Seleccionados: $productosSeleccionados")
        cargarGuardados(producto,productosSeleccionados)

        Log.d("PromtSeleccionarVariables", "Lista actualizada: $listaVariablesSeleccionadas")


        // Obtener referencia al botón de micrófono
        val imageViewMicrofono = dialogView.findViewById<ImageView>(R.id.imageView_microfono)

        // Configurar el listener del botón de micrófono
        imageViewMicrofono.setOnClickListener {

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                (context as Activity).startActivityForResult(intent, REQUEST_CODE_VOICE_SEARCH)

        }

        // Configurar el botón agregar
        button_agregar?.setOnClickListener {

            // Buscar la clave (ModeloProducto) en el mapa basado en el id
            agregarAlPedido(productosSeleccionados, producto, onVariableAgregada)
        }

        // Configurar el botón eliminar carrito
        imageView_eliminarCarrito?.setOnClickListener {
            listaVariablesSeleccionadas.clear()
            onVariableAgregada(listaVariablesSeleccionadas)
            alertDialog?.dismiss()
        }

        // Mostrar nombre del producto
        textView_producto?.text = producto.nombre

        // Configurar el RecyclerView
        configurarReciclerView(context,  producto.listaVariables as MutableList<Variable>)

        // Configurar el SearchView
        searchViewVariantes?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filtro(newText, producto,context)
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
        // Hacer que el diálogo se cierre al tocar fuera de él
        alertDialog?.setCanceledOnTouchOutside(true)

        // Configurar un listener para realizar la acción de agregar si se cierra el diálogo
        alertDialog?.setOnDismissListener {
            // Verificar si el diálogo se cerró sin haber presionado el botón agregar
            if (alertDialog?.isShowing == false) {
                agregarAlPedido(productosSeleccionados, producto, onVariableAgregada)
            }
        }
        alertDialog?.show()
    }

    private fun agregarAlPedido(
        productosSeleccionados: MutableMap<ModeloProducto, Int>,
        producto: ModeloProducto,
        onVariableAgregada: (List<Variable>) -> Unit
    ) {
        val claveAEliminar = productosSeleccionados.keys.find { it.id == producto.id }

        // Si se encuentra una clave que coincide, eliminar la entrada del mapa
        if (claveAEliminar != null) {
            productosSeleccionados.remove(claveAEliminar)
            Log.w("PromtSeleccionarVariables", "Producto con id ${producto.id}  eliminado")
        } else {
            Log.w("PromtSeleccionarVariables", "Producto con id ${producto.id} no encontrado")
        }

        // Verificar el estado del mapa después de la eliminación
        Log.d(
            "PromtSeleccionarVariables",
            "Agregada Lista actualizada: ${productosSeleccionados.keys.map { it.id }}"
        )


        onVariableAgregada(listaVariablesSeleccionadas)
        alertDialog?.dismiss()
    }

    private fun configurarReciclerView(
        context: Context,
        listaVariables: MutableList<Variable> = mutableListOf()
    ) {
        recyclerView_productosVariables?.layoutManager = GridLayoutManager(context, 1)
        adaptador = SeleccionarVariantesAdaptador(
            listaVariables,
            listaVariablesSeleccionadas
        )
        recyclerView_productosVariables?.adapter = adaptador
        adaptador.setOnChangeItem { variable, nuevaCantidad ->
            manejarCambioCantidad(variable, nuevaCantidad)
        }
    }

    private fun cargarGuardados(
        producto: ModeloProducto,
        productosSeleccionados: MutableMap<ModeloProducto, Int>
    ) {
        // Buscar el producto en el mapa basado en el id
        val productoExistente = productosSeleccionados.keys.find { it.id == producto.id }

        productoExistente?.let { producto ->

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
        }

        // Contar cuántas variables tienen una cantidad mayor que 0 en la lista seleccionada
        val cantidadTotalDeVariables = listaVariablesSeleccionadas.sumOf { it.cantidad ?: 0 }

        // Imprimir o utilizar la cantidad total como desees
        Log.d("PromtSeleccionarVariables","Cantidad total de variables seleccionadas: $cantidadTotalDeVariables")
        textView_listaSeleccion?.text=cantidadTotalDeVariables.toString()
    }


    private fun filtro(textoParaFiltrar: String, producto: ModeloProducto,context: Context) {
        val filtro = producto.listaVariables?.filter { variable ->
            variable.nombreVariable.eliminarAcentosTildes().lowercase(Locale.getDefault())
                .contains(textoParaFiltrar.eliminarAcentosTildes().lowercase(Locale.getDefault())) ||
                    variable.color?.eliminarAcentosTildes()?.lowercase(Locale.getDefault())
                        ?.contains(textoParaFiltrar.eliminarAcentosTildes().lowercase(Locale.getDefault())) == true ||
                    variable.tamano?.eliminarAcentosTildes()?.lowercase(Locale.getDefault())
                        ?.contains(textoParaFiltrar.eliminarAcentosTildes().lowercase(Locale.getDefault())) == true
        }


        val productosOrdenados = filtro?.sortedBy { it.nombreVariable } ?: emptyList()

        configurarReciclerView(context, productosOrdenados.toMutableList())


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
        val cantidadTotalDeVariables = listaVariablesSeleccionadas.sumOf { it.cantidad ?: 0 }

        // Imprimir o utilizar la cantidad total como desees
        Log.d("PromtSeleccionarVariables","Cantidad total de variables seleccionadas: $cantidadTotalDeVariables")


        Log.d("PromtSeleccionarVariables", "Lista actualizada candidad cambiada: $listaVariablesSeleccionadas")
        textView_listaSeleccion?.text=cantidadTotalDeVariables.toString()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_VOICE_SEARCH && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = results?.get(0)
            if (query != null) {
                Log.d("PromtSeleccionarVariables", "Resultado de búsqueda por voz: $query")
                Log.d("PromtSeleccionarVariables", "searchViewVariantes: $searchViewVariantes")

                // Actualizar el campo de búsqueda con el resultado de la búsqueda por voz
                searchViewVariantes?.isIconified = false
                searchViewVariantes?.setQuery(query, true)
            } else {
                Log.d("PromtSeleccionarVariables", "No se obtuvo ningún resultado de búsqueda por voz")
            }
        } else {
            Log.d("PromtSeleccionarVariables", "requestCode o resultCode no coinciden")
        }
    }

}
