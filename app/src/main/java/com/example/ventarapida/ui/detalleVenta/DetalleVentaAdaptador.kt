package com.example.ventarapida.ui.detalleVenta

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.MainActivity
import com.example.ventarapida.R
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import com.squareup.picasso.NetworkPolicy

import com.squareup.picasso.Picasso
import java.util.*


class DetalleVentaAdaptador(
    val products: MutableMap<ModeloProducto, Int>,
) : RecyclerView.Adapter<DetalleVentaAdaptador.ProductViewHolder>() {

    private val sortedProducts = MainActivity.ventaProductosSeleccionados.keys.sortedBy { it.nombre }


    // Este método se llama cuando RecyclerView necesita crear un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        // Inflar el diseño del item_producto para crear la vista del ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_seleccionado, parent, false)

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
            onClickItem?.invoke(producto, cantidad!!, position)
        }
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

        @SuppressLint("SetTextI18n")
        fun bind(product: ModeloProducto, cantidadSeleccion: Int?) {

            producto.text = product.nombre

            // Verificar si la cantidad es nula y si no lo es, establecer el texto de la vista de cantidad

            cantidadSeleccion?.let { seleccion.setText(it.toString().formatoMonenda()) }

            existencia.text ="X"+ (product.cantidad.toInt() - cantidadSeleccion!!.toInt())

            precio?.let { precio.setText(product.p_diamante.formatoMonenda()) }
            val total= cantidadSeleccion?.times(product.p_diamante.toInt())

            total_producto.text=total.toString().formatoMonenda()

            // Limpiar la imagen anterior
          //  Picasso.get().cancelRequest(imagenProducto)

            // Cargar la imagen solo si la URL no está vacía y es diferente a la anterior
            if (!product.url.isEmpty() && imagenProducto.tag != product.url) {
                imagenProducto.tag = product.url
                Picasso.get()
                    .load(product.url)
                    .networkPolicy(NetworkPolicy.OFFLINE) // Configurar la política de caché y persistencia
                    .into(imagenProducto, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            // La imagen se cargó exitosamente desde la caché o persistencia
                        }

                        override fun onError(e: Exception) {
                            // Ocurrió un error al cargar la imagen desde la caché o persistencia
                            // Intentar cargar la imagen desde la red
                            Picasso.get().load(product.url).into(imagenProducto)
                        }
                    })
            } else if (product.url.isEmpty()) {
                // Si la URL está vacía, mostrar una imagen por defecto o limpiar la vista
                // dependiendo del diseño que se quiera obtener
                imagenProducto.setImageResource(R.drawable.ic_menu_camera)
            }

        }

    }
}
