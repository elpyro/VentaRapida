package com.castellanoseloy.cataplus.ui.detalleProducto

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.cataplus.R
import com.castellanoseloy.cataplus.datos.Variable


class DetalleVariantesAdaptador(
    val listaVariantes: List<Variable>,
) : RecyclerView.Adapter<DetalleVariantesAdaptador.ClienteViewHolder>() {


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
        val variable =sortedFacturas[position]

        // Vincular los datos del producto con la vista del ViewHolder
        holder.bind(variable)

        // Configurar el click listener para el item de la lista
        holder.cardView.setOnClickListener {
            onClickItem?.invoke(variable)
        }


//        holder.cardView.setOnLongClickListener() {
//            onLongClickItem?.invoke(variable)
//            true
//        }
    }


    private var onClickItem: ((Variable) -> Unit)? = null

    // Configurar el callback para el evento de click en un item de la lista
    fun setOnClickItem(callback: (Variable) -> Unit) {
        this.onClickItem = callback
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
        val textView_cantidad: TextView = itemView.findViewById(R.id.textView_cantidad)
         val cardView:CardView=itemView.findViewById(R.id.cardview_item_Variable)

        @SuppressLint("SetTextI18n")
        fun bind(variable: Variable) {
            nombre.text = variable.nombreVariable
            if(variable.color.isNullOrEmpty() && variable.tamano.isNullOrEmpty()) textView_detalle.visibility=View.GONE
            textView_detalle.text= variable.color+" "+variable.tamano

            textView_cantidad.setText(variable.cantidad.toString())


        }


    }
}
