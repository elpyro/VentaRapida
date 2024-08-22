package com.castellanoseloy.ventarapida.ui.promts

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.datos.Variable
import com.castellanoseloy.ventarapida.procesos.CrearTono


class SeleccionarVariantesAdaptador(
    val listaVariantes: List<Variable>,
    private val listaVariablesSeleccionadas: MutableList<Variable>,
    private val variableEditada: (Variable, Int) -> Unit // Define como privado para que no se pueda modificar desde fuera
) : RecyclerView.Adapter<SeleccionarVariantesAdaptador.ViewHolder>() {

    private val sortedFacturas = listaVariantes.sortedWith(compareBy { it.nombreVariable })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seleccionar_variaciones, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val variable = sortedFacturas[position]
        holder.bind(variable)

        holder.cardView.setOnClickListener {
            // Obtener la cantidad actual en el EditText, o 0 si está vacío
            val cantidadActual = holder.editText_seleccionVariante.text.toString().toIntOrNull() ?: 0

            // Agregar 1 a la cantidad actual
            val nuevaCantidad = cantidadActual + 1

            // Actualizar el valor en el EditText
            holder.editText_seleccionVariante.setText(nuevaCantidad.toString())

        }

        holder.editText_seleccionVariante.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val nuevaCantidad = s.toString().toIntOrNull() ?: 0
                variableEditada(variable, nuevaCantidad) // Usa el callback
                if (nuevaCantidad > 0) {
                    // Cambiar el color del CardView y mostrar el botón
                    val colorSeleccionado =
                        ContextCompat.getColor(holder.itemView.context, R.color.azul_trasparente)
                    holder.cardView.setCardBackgroundColor(colorSeleccionado)
                    holder.botonRestar.visibility = View.VISIBLE

                    val sonido = CrearTono()
                    sonido.crearTono(holder.itemView.context)
                } else {
                    // Si la cantidad es 0, ocultar el botón restar y restaurar el color original del CardView
                    holder.botonRestar.visibility = View.GONE
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        holder.botonRestar.setOnClickListener {
            // Obtener la cantidad actual en el EditText, o 0 si está vacío
            val cantidadActual = holder.editText_seleccionVariante.text.toString().toIntOrNull() ?: 0

            // Restar 1 a la cantidad actual, pero no permitir valores negativos
            val nuevaCantidad = (cantidadActual - 1).coerceAtLeast(0)

            // Actualizar el valor en el EditText
            holder.editText_seleccionVariante.setText(nuevaCantidad.toString())
        }
    }

    override fun getItemCount(): Int {
        return listaVariantes.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.textView_variante)
        val textView_detalle: TextView = itemView.findViewById(R.id.textView_detalleVariante)
        val cardView: CardView = itemView.findViewById(R.id.cardview_itemVariante)
        val editText_seleccionVariante: EditText = itemView.findViewById(R.id.editText_seleccionVariante)
        val botonRestar: ImageButton = itemView.findViewById(R.id.imageButton_restarCantidad)

        @SuppressLint("SetTextI18n")
        fun bind(variable: Variable) {
            nombre.text = "X${variable.cantidad} ${variable.nombreVariable}"
            textView_detalle.text = "${variable.color} ${variable.tamano}"

            // Buscar el valor en la listaVariablesSeleccionadas
            val variableSeleccionada = listaVariablesSeleccionadas.find { it.nombreVariable == variable.nombreVariable }
            val cantidad = variableSeleccionada?.cantidad ?: 0
            editText_seleccionVariante.setText(cantidad.toString())

            if (variable.color.isNullOrEmpty() && variable.tamano.isNullOrEmpty()) {
                textView_detalle.visibility = View.GONE
            }

            // Inicialmente, ocultar el botón restar
            botonRestar.visibility = View.GONE

            // Cambiar el color del CardView y mostrar el botón si la cantidad es mayor a 0
            if (cantidad > 0) {
                val colorSeleccionado =
                    ContextCompat.getColor(itemView.context, R.color.azul_trasparente)
                cardView.setCardBackgroundColor(colorSeleccionado)
                botonRestar.visibility = View.VISIBLE
            } else {
                cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                botonRestar.visibility = View.GONE
            }
        }
    }
}
