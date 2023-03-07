package com.example.ventarapida.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
    }

    override fun getItemCount(): Int {
        return products.size
    }
    private var filteredList: List<ModeloProducto> = products
    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            products
        } else {
            products.filter { it.nombre.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    fun updateList(filteredList: List<ModeloProducto>) {
        this.filteredList = filteredList
        notifyDataSetChanged()
    }


    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textView_nombre)
        private val priceTextView: TextView = itemView.findViewById(R.id.textView_precio)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textView_cantidad)
        private val imagenProducto: ImageView = itemView.findViewById(R.id.imageView_producto)

        fun bind(product: ModeloProducto) {
            nameTextView.text = product.nombre
            priceTextView.text = "$ ${product.p_compra}"
            descriptionTextView.text = product.cantidad
            Picasso.get().load( product.url)
                .into(imagenProducto)
        }
    }
}
