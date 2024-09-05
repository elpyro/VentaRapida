package com.castellanoseloy.cataplus.procesos

import androidx.recyclerview.widget.DiffUtil
import com.castellanoseloy.cataplus.datos.ModeloFactura

class RegistrosDiffCallback(
    private val oldList: List<ModeloFactura>,
    private val newList: MutableList<ModeloFactura>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].id_pedido == newList[newItemPosition].id_pedido
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}