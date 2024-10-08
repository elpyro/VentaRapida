package com.castellanoseloy.cataplus.ui.registros.pestanas

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.cataplus.R
import com.castellanoseloy.cataplus.datos.ModeloFactura
import com.castellanoseloy.cataplus.procesos.RegistrosDiffCallback

import java.util.*


class FacturaVentasAdaptador(
    var listaFacturas: MutableList<ModeloFactura>,
) : RecyclerView.Adapter<FacturaVentasAdaptador.FacturaViewHolder>() {




    // Este método se llama cuando RecyclerView necesita crear un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        // Inflar el diseño del item_producto para crear la vista del ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_factura_ventas, parent, false)

        return FacturaViewHolder(view)
    }

    // Este método se llama cuando RecyclerView necesita mostrar un elemento en una posición determinada
    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {

        // Obtener la clave del elemento que corresponde a la posición
        val Factura =listaFacturas[position]


        // Vincular los datos del producto con la vista del ViewHolder
        holder.bind(Factura)

        holder.cardView.setOnClickListener {
            onClickItem?.invoke(Factura)
        }
        }

    fun updateData(newList: MutableList<ModeloFactura>) {
        val diffResult = DiffUtil.calculateDiff(RegistrosDiffCallback(listaFacturas, newList))
        listaFacturas = ArrayList(newList) // Convierte a ArrayList
        diffResult.dispatchUpdatesTo(this)
    }

    private var onClickItem: ((ModeloFactura) -> Unit)? = null

    // Configurar el callback para el evento de click en un item de la lista
    fun setOnClickItem(callback: (ModeloFactura) -> Unit) {
        this.onClickItem = callback
    }

    // Este método devuelve el número de elementos en la lista de productos
    override fun getItemCount(): Int {
        return listaFacturas.size
    }


    // ViewHolder para la vista de cada elemento de la lista de productos
        inner class FacturaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cliente: TextView = itemView.findViewById(R.id.textView_cliente)
        val total: TextView = itemView.findViewById(R.id.textView_valor)
        val vendedor: TextView = itemView.findViewById(R.id.textView_vendedor)
        val fecha: TextView = itemView.findViewById(R.id.textView_fecha)
        val id: TextView = itemView.findViewById(R.id.textView_id)
        val cardView:CardView=itemView.findViewById(R.id.cardview_itemProducto)

        @SuppressLint("SetTextI18n")
        fun bind(factura: ModeloFactura) {

            cliente.text = factura.nombre
            val totalfactura=factura.total
            total.text= totalfactura
            vendedor.text=factura.nombre_vendedor
            fecha.text=factura.fecha
            id.text=factura.id_pedido.substring(0, 5)  //solo mostramos los primero 5 digitos en la vista para evitar exeso de datos

        }

    }
}
