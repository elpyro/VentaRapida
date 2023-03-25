package com.example.ventarapida.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.R
import com.example.ventarapida.ui.data.ModeloProducto
import com.example.ventarapida.ui.home.HomeViewModel
import com.squareup.picasso.Picasso


class ProductAdapter(private val products: List<ModeloProducto>, private val viewModel: HomeViewModel) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {

        holder.bind(products[position])



        holder.cardview.setOnLongClickListener { motionEvent ->
            onLongClickItem?.invoke(products[position], position)
            true // Devuelve true para indicar que el evento ha sido consumido
        }

        holder.cardview.setOnClickListener { motionEvent ->

           viewModel.agregarProductoSeleccionado(products[position])
            holder.bind(products[position])
//Todo moviemiento pendiente
//            setOnClickItem?.invoke( position,vistaCopiada)
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    private var onLongClickItem :  ((ModeloProducto, Int)-> Unit)?=null

    fun setOnLongClickItem(callback: (ModeloProducto, Int)-> Unit){
        this.onLongClickItem = callback
    }


//    private var setOnClickItem :  (( Int, View)-> Unit)?=null
//    fun setOnClickItem(callback: ( Int, View)-> Unit){
//        this.setOnClickItem = callback
//    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         val producto: TextView = itemView.findViewById(R.id.textView_nombre)
         val precio: TextView = itemView.findViewById(R.id.textView_precio)
        val seleccion: TextView = itemView.findViewById(R.id.editText_seleccionProducto)
         val descripcion: TextView = itemView.findViewById(R.id.textView_cantidad)
        val cardview:CardView=itemView.findViewById(R.id.cardview_itemProducto)
        val botonRestar:ImageButton=itemView.findViewById(R.id.imageButton_restarCantidad)
        private val imagenProducto: ImageView = itemView.findViewById(R.id.imageView_producto)

        @SuppressLint("SetTextI18n")
        fun bind(product: ModeloProducto) {


                    producto.text = product.nombre
                    precio.text = "$ ${product.p_diamante}"
                    descripcion.text = "X"+product.cantidad
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

            if (viewModel.productosSeleccionados.isNotEmpty() && viewModel.productosSeleccionados.containsKey(products[position])) {
                val cantidad = viewModel.productosSeleccionados.getOrDefault(products[position], 0)
                if (cantidad > 0) {
                    seleccion.setText(cantidad.toString())
                    botonRestar.visibility = View.VISIBLE
                    val color = ContextCompat.getColor(itemView.context, R.color.azul_trasparente)
                    cardview.setCardBackgroundColor(color)
                    descripcion.text =  "X"+(product.cantidad.toInt() - cantidad).toString()
                }

            }else{
                seleccion.setText("0")
                botonRestar.visibility = View.GONE
                cardview.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }




        }
    }

}
