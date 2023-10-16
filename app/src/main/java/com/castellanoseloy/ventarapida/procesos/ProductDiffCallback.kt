package com.castellanoseloy.ventarapida.procesos

import androidx.recyclerview.widget.DiffUtil
import com.castellanoseloy.ventarapida.datos.ModeloProducto

class ProductDiffCallback(
    private val oldList: List<ModeloProducto>,
    private val newList: List<ModeloProducto>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].id == newList[newItemPosition].id
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}