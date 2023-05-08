package com.example.ventarapida.ui.agregarProductoFactura

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
import com.example.ventarapida.R
import com.example.ventarapida.datos.ModeloProducto

import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import com.squareup.picasso.Picasso
import java.util.*


class AgregarProductoFacturaAdaptador(
    private val products: List<ModeloProducto>,
    private val viewModel: AgregarProductoFacturaViewModel
) : RecyclerView.Adapter<AgregarProductoFacturaAdaptador.ProductViewHolder>() {
    private var isUserEditing = false // Indica si el usuario está editando la cantidad de un producto


    // Este método se llama cuando RecyclerView necesita crear un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        // Inflar el diseño del item_producto para crear la vista del ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductViewHolder(view)
    }

    // Este método se llama cuando RecyclerView necesita mostrar un elemento en una posición determinada
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        // Vincular los datos del producto con la vista del ViewHolder
        holder.bind(products[position])

        // Configurar el evento de click largo en el cardview del producto
        holder.cardview.setOnLongClickListener { motionEvent ->
            onLongClickItem?.invoke(products[position], position)
            true // Devuelve true para indicar que el evento ha sido consumido
        }

        holder.seleccion.setOnClickListener {
            holder.seleccion.post {
                holder.seleccion.selectAll()
            }
        }

    }

    // Este método devuelve el número de elementos en la lista de productos
    override fun getItemCount(): Int {
        return products.size
    }

    // Callback para el evento de click largo en un item de la lista
    private var onLongClickItem: ((ModeloProducto, Int) -> Unit)? = null

    // Configurar el callback para el evento de click largo en un item de la lista
    fun setOnLongClickItem(callback: (ModeloProducto, Int) -> Unit) {
        this.onLongClickItem = callback
    }

    // ViewHolder para la vista de cada elemento de la lista de productos
        inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val producto: TextView = itemView.findViewById(R.id.textView_nombre)
        private val precio: TextView = itemView.findViewById(R.id.textView_precio)
         val seleccion: EditText = itemView.findViewById(R.id.editText_seleccionProducto)
        private val existencia: TextView = itemView.findViewById(R.id.textView_cantidad)
         val cardview: CardView = itemView.findViewById(R.id.cardview_itemProducto)
         val botonRestar: ImageButton = itemView.findViewById(R.id.imageButton_restarCantidad)
         val imagenProducto: ImageView = itemView.findViewById(R.id.imageView_producto)
        private lateinit var existenciaSinCambios: String
        @SuppressLint("SetTextI18n")
        fun bind(product: ModeloProducto) {

            cargarProducto(product)

            cardview.setOnClickListener { motionEvent ->
                viewModel.agregarProductoSeleccionado(products[adapterPosition])
                cargarProducto(product)
            }
            // Configurar el evento de click en el botón restar cantidad del producto
            botonRestar.setOnClickListener { motionEvent ->
                viewModel.restarProductoSeleccionado(products[position])
               cargarProducto(product)
            }

            seleccion.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                if(seleccion.hasFocus()) {
                    this.existenciaSinCambios = product.cantidad
                    seleccion.addTextChangedListener(textWatcher)
                }
                if (!hasFocus) {
                    seleccion.removeTextChangedListener(textWatcher)
                }
            }
            // colocamos el editextseleccion aparte para no crear mas de un oyente

            }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    // Obtener la cantidad seleccionada como un número entero, o 0 si no se puede analizar como número
                    val cantidadSeleccionada = s.toString().toIntOrNull() ?: 0

                    existencia.text= "X"+(existenciaSinCambios.toInt() - cantidadSeleccionada).toString()

                    if (cantidadSeleccionada > 0 && isUserEditing) {
                        // Hacer visible el botón restar
                        botonRestar.visibility = View.VISIBLE
                        // Actualizar la cantidad del producto en el ViewModel
                        viewModel.actualizarCantidadProducto(
                            products[position],
                            cantidadSeleccionada
                        )
                    } else {
                        // De lo contrario, hacer invisible el botón restar
                        botonRestar.visibility = View.GONE
                    }
                    // Si el usuario está editando
                } else if (isUserEditing) {
                    existencia.text= "X$existenciaSinCambios"
                    // Actualizar la cantidad del producto en el ViewModel a cero
                    viewModel.actualizarCantidadProducto(products[position], 0)
                    botonRestar.visibility = View.GONE
                }
            }
        }
        private fun cargarProducto(product: ModeloProducto) {

            producto.text = product.nombre

            precio.text = product.p_diamante.formatoMonenda()

            existencia.text =product.cantidad

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

            if (AgregarProductoFactura.productosSeleccionadosAgregar.isNotEmpty() &&   AgregarProductoFactura.productosSeleccionadosAgregar.any { it.key.id == products[position].id }) {
                val cantidad = AgregarProductoFactura.productosSeleccionadosAgregar.filterKeys { it.id == products[position].id }.values.sum()

                if (cantidad > 0) {
                    isUserEditing = false
                    seleccion.setText(cantidad.toString())
                    isUserEditing = true

                    botonRestar.visibility = View.VISIBLE
                    if (cantidad == 1) {
                        botonRestar.setImageResource(R.drawable.baseline_delete_24)
                    } else {
                        botonRestar.setImageResource(R.drawable.baseline_arrow_drop_down_24)
                    }

                    val color = ContextCompat.getColor(itemView.context, R.color.azul_trasparente)
                    cardview.setCardBackgroundColor(color)
                    producto.setTextAppearance(R.style.ColorFuenteEnFondoGris)
                    precio.setTextAppearance(R.style.ColorFuenteEnFondoGris)
                    existencia.setTextAppearance(R.style.ColorFuenteEnFondoGris)

                    existencia.text = "X${(product.cantidad.toInt() - cantidad)}"
                }
            } else {
                isUserEditing = false
                seleccion.setText("")
                isUserEditing = true
                botonRestar.visibility = View.GONE

                cardview.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                producto.setTextAppearance(R.style.ColorFuentes)
                precio.setTextAppearance(R.style.ColorFuentes)
                existencia.setTextAppearance(R.style.ColorFuentes)

            }
        }

    }

    }

