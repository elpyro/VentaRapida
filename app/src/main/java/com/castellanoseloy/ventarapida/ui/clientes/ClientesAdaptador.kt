package com.castellanoseloy.ventarapida.ui.clientes

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.datos.ModeloClientes

import java.util.*


class ClientesAdaptador(
    val listaClientes: MutableList<ModeloClientes>,
) : RecyclerView.Adapter<ClientesAdaptador.ClienteViewHolder>() {


    private val sortedFacturas = listaClientes.sortedWith(
        compareBy { it.nombre }
    )


    // Este método se llama cuando RecyclerView necesita crear un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        // Inflar el diseño del item_producto para crear la vista del ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clientes, parent, false)

        return ClienteViewHolder(view)
    }

    // Este método se llama cuando RecyclerView necesita mostrar un elemento en una posición determinada
    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {

        // Obtener la clave del elemento que corresponde a la posición
        val cliente =sortedFacturas[position]

        // Vincular los datos del producto con la vista del ViewHolder
        holder.bind(cliente)

        holder.cardView.setOnClickListener() {
            onClickItem?.invoke(cliente)
        }

        holder.cardView.setOnLongClickListener() {
            onLongClickItem?.invoke(cliente)
            true
        }

    }


    private var onClickItem: ((ModeloClientes) -> Unit)? = null

    // Configurar el callback para el evento de click en un item de la lista
    fun setOnClickItem(callback: (ModeloClientes) -> Unit) {
        this.onClickItem = callback
    }

    // Callback para el evento de click largo en un item de la lista
    private var onLongClickItem: ((ModeloClientes) -> Unit)? = null

    fun setOnLongClickItem(callback: (ModeloClientes) -> Unit) {
        this.onLongClickItem = callback
    }

    // Este método devuelve el número de elementos en la lista de productos
    override fun getItemCount(): Int {
        return listaClientes.size
    }




    // ViewHolder para la vista de cada elemento de la lista de productos
        inner class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.textView_cliente)
        val direccion: TextView = itemView.findViewById(R.id.textView_direccion)
         val cardView:CardView=itemView.findViewById(R.id.cardview_itemCliente)

        @SuppressLint("SetTextI18n")
        fun bind(cliente: ModeloClientes) {

            nombre.text = cliente.nombre
            direccion.text= cliente.direccion

        }

    }
}
