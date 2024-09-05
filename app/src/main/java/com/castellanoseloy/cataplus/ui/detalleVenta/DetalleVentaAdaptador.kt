package com.castellanoseloy.cataplus.ui.detalleVenta

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.cataplus.R
import com.castellanoseloy.cataplus.datos.ModeloProducto
import com.castellanoseloy.cataplus.procesos.Utilidades
import com.castellanoseloy.cataplus.procesos.Utilidades.formatoMonenda
import com.castellanoseloy.cataplus.procesos.Utilidades.mostrarVariantesAdaptador

import java.util.*


class DetalleVentaAdaptador(
    var products: MutableMap<ModeloProducto, Int>,
) : RecyclerView.Adapter<DetalleVentaAdaptador.ProductViewHolder>() {

    private val sortedProducts = products.keys.sortedBy { it.nombre }


    // Este método se llama cuando RecyclerView necesita crear un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        // Inflar el diseño del item_producto para crear la vista del ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_seleccionado, parent, false)
        Log.d("ListaCompra", "tamaño de la lista: ${products.size}")
        return ProductViewHolder(view)
    }

    // Este método se llama cuando RecyclerView necesita mostrar un elemento en una posición determinada
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {

        // Obtener la clave del elemento que corresponde a la posición
        val producto =sortedProducts[position]
        // Obtener la cantidad del elemento
        val cantidad = products[producto]

        // Vincular los datos del producto con la vista del ViewHolder
        holder.bind(producto, cantidad)

        holder.cardView.setOnClickListener {
            Log.d("ListaCompra", "invocaste al: $cantidad de $producto")
            onClickItem?.invoke(producto, cantidad!!, position)
        }
        holder.imagenProducto.setOnClickListener {
            conClickImangen?.invoke(producto)
        }
    }

    private var conClickImangen: ((ModeloProducto) -> Unit)? = null
    fun setOnClickImangen(callback: (ModeloProducto) -> Unit) {
        this.conClickImangen = callback
    }

    private var onClickItem: ((ModeloProducto, Int, Int) -> Unit)? = null

    // Configurar el callback para el evento de click en un item de la lista
    fun setOnClickItem(callback: (ModeloProducto, Int, Int) -> Unit) {
        this.onClickItem = callback
    }

    // Este método devuelve el número de elementos en la lista de productos
    override fun getItemCount(): Int {
        return products.size
    }


    // ViewHolder para la vista de cada elemento de la lista de productos
        inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         val producto: TextView = itemView.findViewById(R.id.textView_producto)
        val existencia: TextView = itemView.findViewById(R.id.textView_diponible)
        val total_producto: TextView = itemView.findViewById(R.id.textView_total_productucto)
        val seleccion: TextView = itemView.findViewById(R.id.Textview_seleccion)
        val precio: TextView = itemView.findViewById(R.id.Textview_precio)
        val imagenProducto: ImageView = itemView.findViewById(R.id.imageView_foto_producto)
        val cardView:CardView=itemView.findViewById(R.id.cardview_itemProducto)
        val textView_variante:TextView=itemView.findViewById(R.id.textView_variante)

        @SuppressLint("SetTextI18n")
        fun bind(product: ModeloProducto, cantidadSeleccion: Int?) {
            Log.d("ListaCompra", "mostrando $cantidadSeleccion de $product")
            producto.text = product.nombre

            mostrarVariantesAdaptador(product.listaVariables?: emptyList(),textView_variante)
            // Verificar si la cantidad es nula y si no lo es, establecer el texto de la vista de cantidad
            cantidadSeleccion?.let { seleccion.text = it.toString() }

            try {
                existencia.text ="X"+ (product.cantidad.toInt() - cantidadSeleccion!!.toInt())
            } catch (_: Exception) {
            }

            precio.let { precio.text = product.p_diamante.formatoMonenda() }
            val total= cantidadSeleccion?.times(product.p_diamante.toDouble())

            total_producto.text=total.toString().formatoMonenda()

            Utilidades.cargarImagen(product.url, imagenProducto)

        }

    }
}
