package com.castellanoseloy.ventarapida.ui.nuevoProducto

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.datos.Variable


class VariantesAdaptador(
    val listaVariantes: List<Variable>,
) : RecyclerView.Adapter<VariantesAdaptador.ClienteViewHolder>() {


    private val sortedFacturas = listaVariantes.sortedWith(
        compareBy { it.nombreVariable }
    )


    // Este método se llama cuando RecyclerView necesita crear un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        // Inflar el diseño del item_producto para crear la vista del ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_variable_creada, parent, false)

        return ClienteViewHolder(view)
    }

    // Este método se llama cuando RecyclerView necesita mostrar un elemento en una posición determinada
    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {

        // Obtener la clave del elemento que corresponde a la posición
        val variable = sortedFacturas[position]

        // Vincular los datos del producto con la vista del ViewHolder
        holder.bind(variable)

//
//        holder.cardView.setOnLongClickListener() {
//            onLongClickItem?.invoke(variable)
//            true
//        }

    }


    private var onChangeItem: ((Variable) -> Unit)? = null

    // Configurar el callback para el evento de click en un item de la lista
    fun setOnChangeText(callback: (Variable) -> Unit) {
        this.onChangeItem = callback
    }

    // Callback para el evento de click largo en un item de la lista
    private var onLongClickItem: ((Variable) -> Unit)? = null

    fun setOnLongClickItem(callback: (Variable) -> Unit) {
        this.onLongClickItem = callback
    }

    // Este método devuelve el número de elementos en la lista de productos
    override fun getItemCount(): Int {
        return listaVariantes.size
    }


    // ViewHolder para la vista de cada elemento de la lista de productos
    inner class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.textView_nombre_varaible)
        val textView_detalle: TextView = itemView.findViewById(R.id.textView_detalle)
        val cardView: CardView = itemView.findViewById(R.id.cardview_item_Variable)
        val textView_cantidad: TextView = itemView.findViewById(R.id.textView_cantidad)

        @SuppressLint("SetTextI18n")
        fun bind(variable: Variable) {

            if (variable.color.isNullOrEmpty() && variable.tamano.isNullOrEmpty()) textView_detalle.visibility =
                View.GONE

            nombre.text = variable.nombreVariable
            textView_detalle.text = variable.color + " " + variable.tamano
            textView_cantidad.text = variable.cantidad.toString()
        }

    }
}
