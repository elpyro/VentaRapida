package com.castellanoseloy.ventarapida.procesos

import androidx.recyclerview.widget.DiffUtil
import com.castellanoseloy.ventarapida.datos.ModeloFactura
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado

class ProductosCompradosFacturadosDiffCallback(
    private val oldList: List<ModeloProductoFacturado>,
    private val newList: MutableList<ModeloProductoFacturado>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].id_pedido == newList[newItemPosition].id_pedido
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}