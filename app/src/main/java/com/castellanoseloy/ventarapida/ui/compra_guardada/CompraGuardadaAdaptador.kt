package com.castellanoseloy.ventarapida.ui.compra_guardada

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.formatoMonenda
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

import java.text.SimpleDateFormat

class CompraGuardadaAdaptador(
    val listaFacturas: MutableList<ModeloProductoFacturado>,
) : RecyclerView.Adapter<CompraGuardadaAdaptador.FacturaViewHolder>() {

    val formatoFecha = SimpleDateFormat("dd/MM/yyyy")
    val sortedFacturas = listaFacturas.sortedWith(
        compareByDescending<ModeloProductoFacturado> { formatoFecha.parse(it.fecha) }
            .thenByDescending { it.hora }
    )


    // Este método se llama cuando RecyclerView necesita crear un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        // Inflar el diseño del item_producto para crear la vista del ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_seleccionado, parent, false)

        return FacturaViewHolder(view)
    }

    // Este método se llama cuando RecyclerView necesita mostrar un elemento en una posición determinada
    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {

        // Obtener la clave del elemento que corresponde a la posición
        val ProductoFactura =sortedFacturas[position]


        // Vincular los datos del producto con la vista del ViewHolder
        holder.bind(ProductoFactura)

        holder.cardView.setOnClickListener {
            onClickItem?.invoke(ProductoFactura)
        }
    }


    private var onClickItem: ((ModeloProductoFacturado) -> Unit)? = null

    // Configurar el callback para el evento de click en un item de la lista
    fun setOnClickItem(callback: (ModeloProductoFacturado) -> Unit) {
        this.onClickItem = callback
    }

    // Este método devuelve el número de elementos en la lista de productos
    override fun getItemCount(): Int {
        return listaFacturas.size
    }


    // ViewHolder para la vista de cada elemento de la lista de productos
    inner class FacturaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val producto: TextView = itemView.findViewById(R.id.textView_producto)
        val existencia: TextView = itemView.findViewById(R.id.textView_diponible)
        val total_producto: TextView = itemView.findViewById(R.id.textView_total_productucto)
        val seleccion: TextView = itemView.findViewById(R.id.Textview_seleccion)
        val precio: TextView = itemView.findViewById(R.id.Textview_precio)
        val imagenProducto: ImageView = itemView.findViewById(R.id.imageView_foto_producto)
        val cardView: CardView =itemView.findViewById(R.id.cardview_itemProducto)

        @SuppressLint("SetTextI18n")
        fun bind(factura: ModeloProductoFacturado) {

            producto.text = factura.producto
            seleccion.text=factura.cantidad
            precio.text=factura.costo.formatoMonenda()
            existencia.visibility=View.GONE
            val total= factura.cantidad.toInt() * factura.costo.toDouble()
            total_producto.text= total.toString().formatoMonenda()

            Utilidades.cargarImagen(factura.imagenUrl, imagenProducto)
        }

    }
}
