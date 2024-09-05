package com.castellanoseloy.cataplus.ui.promts

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.cataplus.R
import com.castellanoseloy.cataplus.datos.Variable


class SeleccionarVariantesAdaptador(
    val listaVariantes: MutableList<Variable>,
    private val listaVariablesSeleccionadas: MutableList<Variable>,
) : RecyclerView.Adapter<SeleccionarVariantesAdaptador.ViewHolder>() {
    private var isUserEditing = false
    private val sortedFacturas = listaVariantes.sortedWith(compareBy { it.nombreVariable })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seleccionar_variaciones, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val variable = sortedFacturas[position]
        holder.bind(variable)

        // Elimina cualquier TextWatcher previo para evitar problemas de reciclaje
        holder.editText_seleccionVariante.removeTextChangedListener(holder.currentTextWatcher)

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val nuevaCantidad = s.toString().toIntOrNull() ?: 0

                if (isUserEditing) {
                    if (holder.editText_seleccionVariante.hasFocus()) {
                        isUserEditing = true
                    } else {
                        isUserEditing = false
                    }
                    Log.d("Adaptador", "EditText cambiado de $variable a: $nuevaCantidad")
                    CambioUi(nuevaCantidad, holder)

                    onChangeClickItem?.invoke(variable, nuevaCantidad)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        // Asigna y guarda el nuevo TextWatcher
        holder.currentTextWatcher = textWatcher
        holder.editText_seleccionVariante.addTextChangedListener(textWatcher)

        // Configuración de click listeners
        holder.cardView.setOnClickListener {
            val cantidadActual =
                holder.editText_seleccionVariante.text.toString().toIntOrNull() ?: 0
            val nuevaCantidad = cantidadActual + 1
            isUserEditing = true
            holder.editText_seleccionVariante.setText(nuevaCantidad.toString())
        }

        holder.botonRestar.setOnClickListener {
            // Obtener la cantidad actual en el EditText, o 0 si está vacío
            val cantidadActual =
                holder.editText_seleccionVariante.text.toString().toIntOrNull() ?: 0
            // Restar 1 a la cantidad actual, pero no permitir valores negativos
            val nuevaCantidad = (cantidadActual - 1).coerceAtLeast(0)
            isUserEditing = true
            holder.editText_seleccionVariante.setText(nuevaCantidad.toString())

        }
    }


    private fun CambioUi(
        nuevaCantidad: Int,
        holder: ViewHolder
    ) {
        if (nuevaCantidad > 0) {
            // Cambiar el color del CardView y mostrar el botón

            val colorSeleccionado =
                ContextCompat.getColor(holder.itemView.context, R.color.azul_trasparente)
            holder.cardView.setCardBackgroundColor(colorSeleccionado)
            holder.botonRestar.visibility = View.VISIBLE

        } else {
            // Si la cantidad es 0, ocultar el botón restar y restaurar el color original del CardView
            holder.botonRestar.visibility = View.GONE
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
        }
    }


    // Callback para el evento de click largo en un item de la lista
    private var onChangeClickItem: ((Variable, Int) -> Unit)? = null

    // Configurar el callback para el evento de click largo en un item de la lista
    fun setOnChangeItem(callback: (Variable, Int) -> Unit) {
        this.onChangeClickItem = callback
    }


    override fun getItemCount(): Int {
        return listaVariantes.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.textView_variante)
        val textView_detalle: TextView = itemView.findViewById(R.id.textView_detalleVariante)
        val cardView: CardView = itemView.findViewById(R.id.cardview_itemVariante)
        val editText_seleccionVariante: EditText =
            itemView.findViewById(R.id.editText_seleccionVariante)
        val botonRestar: ImageButton = itemView.findViewById(R.id.imageButton_restarCantidad)

        // Almacenar el TextWatcher actual para poder eliminarlo si es necesario
        var currentTextWatcher: TextWatcher? = null

        @SuppressLint("SetTextI18n")
        fun bind(variable: Variable) {
            nombre.text = "${variable.nombreVariable}"
            textView_detalle.text = "x${variable.cantidad} ${variable.color} ${variable.tamano}"

            // Buscar el valor en la listaVariablesSeleccionadas
            val variableSeleccionada =
                listaVariablesSeleccionadas.find { it.nombreVariable == variable.nombreVariable }
            val cantidad = variableSeleccionada?.cantidad ?: 0
            isUserEditing = false
            if(cantidad>0)   editText_seleccionVariante.setText(cantidad.toString()) else   editText_seleccionVariante.setText("")
            isUserEditing = true


            // Inicialmente, ocultar el botón restar
            botonRestar.visibility = View.GONE

            CambioUi(cantidad,this)
        }
    }

}
