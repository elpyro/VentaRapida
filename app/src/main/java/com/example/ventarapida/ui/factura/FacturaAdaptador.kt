package com.example.ventarapida.ui.factura

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.MainActivity
import com.example.ventarapida.R
import com.example.ventarapida.ui.datos.ModeloProducto
import com.example.ventarapida.ui.procesos.Utilidades.formatoMonenda

import com.squareup.picasso.Picasso
import java.util.*


class FacturaAdaptador(
    val products: MutableMap<ModeloProducto, Int>,
    val viewModel: FacturaViewModel
) : RecyclerView.Adapter<FacturaAdaptador.ProductViewHolder>() {

    private var isUserEditing = false // Indica si el usuario está editando la cantidad de un producto


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
        val productKey = products.keys.toList()[position]
        // Obtener la cantidad del elemento
        val cantidad = products[productKey]

        // Vincular los datos del producto con la vista del ViewHolder
        holder.bind(productKey, cantidad, position)

        holder.itemView.setOnClickListener {
            onClickItem?.invoke(productKey, cantidad!!, position)
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


        @SuppressLint("SetTextI18n")
        fun bind(product: ModeloProducto, cantidadSeleccion: Int?, position: Int) {

            producto.text = product.nombre

            // Verificar si la cantidad es nula y si no lo es, establecer el texto de la vista de cantidad
            isUserEditing = false
            cantidadSeleccion?.let { seleccion.setText(it.toString()) }
            isUserEditing = true

            existencia.text ="X"+ (product.cantidad.toInt() - cantidadSeleccion!!.toInt())

            precio?.let { precio.setText(product.p_diamante.formatoMonenda()) }
            val total= cantidadSeleccion?.times(product.p_diamante.toInt())

            total_producto.text=total.toString().formatoMonenda()

            // Limpiar la imagen anterior
            Picasso.get().cancelRequest(imagenProducto)

            // Cargar la imagen solo si la URL no está vacía y es diferente a la anterior
            if (!product.url.isEmpty() && imagenProducto.tag != product.url) {
                imagenProducto.tag = product.url
                Picasso.get().load(product.url).into(imagenProducto)
            } else if (product.url.isEmpty()) {
                // Si la URL está vacía, mostrar una imagen por defecto o limpiar la vista
                // dependiendo del diseño que se quiera obtener
                imagenProducto.setImageResource(R.drawable.ic_menu_camera)
            }

        }

    }

}

//            seleccion.addTextChangedListener(object : TextWatcher {
//                // Este método se llama antes de que el texto cambie
//                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//                }
//
//                // Este método se llama cuando el texto cambia
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    // Si el texto no está vacío
//                    if (!s.isNullOrBlank()) {
//                        if (precioCambiadoProgramaticamente) {
//                            // Establece la variable a falso de nuevo
//                            precioCambiadoProgramaticamente = false
//                            return
//                        }
//
//                        // Obtener la cantidad seleccionada como un número entero, o 0 si no se puede analizar como número
//                        val nuevaCantidad = s.toString().toIntOrNull() ?: 0
//                        // Si la cantidad seleccionada es mayor que cero y el usuario está editando
//                        if (nuevaCantidad > 0 ) {
//                            // Hacer visible el botón restar
//
//                            val total= nuevaCantidad?.times(product.p_diamante.toInt())
//                            total_producto.text=total.toString().formatoMonenda()
//
//                            existencia.text ="X"+ (product.cantidad.toInt() - nuevaCantidad!!.toInt())
//                            viewModel.actualizarCantidadProducto(product,nuevaCantidad)
////                                    notifyItemChanged(position)
//                        }
//
//                        // Si el usuario está editando
//                    }
//                }
//
//                // Este método se llama después de que el texto cambia
//                override fun afterTextChanged(s: Editable?) {
//
//                }
//            })

//            precio.setOnFocusChangeListener { view, hasFocus ->
//                if (!hasFocus) {
//                  val precioAnterior= precio.text.toString()
////                    precioCambiadoProgramaticamente=true
////                  precio.setText(precioAnterior.eliminarPuntosComas().formatoMonenda())
//                }else{
//                    precio.post {
//                        precio.selectAll()
//                        }
//
//                    precio.addTextChangedListener(object : TextWatcher {
//                        // Este método se llama antes de que el texto cambie
//                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//                        }
//
//                        // Este método se llama cuando el texto cambia
//                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//                        }
//
//                        // Este método se llama después de que el texto cambia
//                        override fun afterTextChanged(string: Editable?) {
//                            // Si el texto no está vacío
//                            if (!string.isNullOrBlank()) {
//                                // Obtener el precio seleccionado como un número entero, o 0 si no se puede analizar como número
//                                val nuevoPrecio = string.toString().toIntOrNull() ?: 0
//                                // Si el cambio en el precio se hizo programáticamente, no hagas nada
//                                if (precioCambiadoProgramaticamente) {
//                                    // Establece la variable a falso de nuevo
//                                    precioCambiadoProgramaticamente = false
//                                    return
//                                }
//                                // Si el precio seleccionado es mayor que cero y el usuario está editando
//                                if (nuevoPrecio > -1 ) {
//                                    // Hacer visible el botón restar
//
//                                    val total= cantidadSeleccion?.times(nuevoPrecio)
//                                    total_producto.text=total.toString().formatoMonenda()
//
//                                    existencia.text ="X"+ (product.cantidad.toInt() - cantidadSeleccion!!.toInt())
//                                    viewModel.actualizarPrecio(product,nuevoPrecio, cantidadSeleccion)
//                                }
//                                // Si el usuario está editando
//                            }else{
//                                // Establece la variable a falso de nuevo
//                                precioCambiadoProgramaticamente = false
//                                precio.setText("0")
//                            }
//                        }
//                    })
//
//                }
//            }
//            seleccion.setOnFocusChangeListener { view, hasFocus ->
//                if (hasFocus) {
//                    seleccion.post {
//                        seleccion.selectAll()
//                    }
//
//
//                }
//
//            }
//




//            var precioModificadoProgramaticamente = false
//            precio.addTextChangedListener(object : TextWatcher {
//                // Este método se llama antes de que el texto cambie
//                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//                }
//
//                // Este método se llama cuando el texto cambia
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    // Si el texto no está vacío
//                    if (!precioModificadoProgramaticamente) {
//                        val precioActual = precio.text.toString()
//                        if (precioActual.isNotBlank()) {
//                            precioModificadoProgramaticamente = true
//                            precio.setText(precioActual.formatoMonenda())
//                            precio.setSelection(precio.length()) // Coloca el cursor al final del texto
//                            precioModificadoProgramaticamente = false
//                        }
//                    }
//                }
//
//                // Este método se llama después de que el texto cambia
//                override fun afterTextChanged(s: Editable?) {
//
//                }
//            })
//Solo necesario para lista de selecciones
//            if (MainActivity.productosSeleccionados.isNotEmpty() &&   MainActivity.productosSeleccionados.any { it.key.id == products[position].id }) {
//
//                //elemento seleccionado
//                //cambiar valores
//            } else {
//                //restaurar valores predeterminados
//            }