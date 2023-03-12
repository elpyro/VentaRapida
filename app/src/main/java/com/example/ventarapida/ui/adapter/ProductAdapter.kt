package com.example.ventarapida.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.R
import com.example.ventarapida.ui.data.ModeloProducto
import com.squareup.picasso.Picasso


class ProductAdapter(private val products: List<ModeloProducto>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])

        holder.cardview_itemProducto.setOnClickListener { view ->
            onClickItem?.invoke(products[position], position)
        }

    }

    override fun getItemCount(): Int {
        return products.size
    }

    private var onClickItem :  ((ModeloProducto, Int)-> Unit)?=null

    fun setOnClickItem(callback: (ModeloProducto, Int)-> Unit){
        this.onClickItem = callback
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textView_nombre)
        private val priceTextView: TextView = itemView.findViewById(R.id.textView_precio)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textView_cantidad)
        val cardview_itemProducto:CardView=itemView.findViewById(R.id.cardview_itemProducto)
        private val imagenProducto: ImageView = itemView.findViewById(R.id.imageView_producto)

        fun bind(product: ModeloProducto) {
            nameTextView.text = product.nombre
            priceTextView.text = "$ ${product.p_diamante}"
            descriptionTextView.text = product.cantidad
           if(!product.url.isEmpty()) Picasso.get().load( product.url).into(imagenProducto)

        }
    }
}
