package com.castellanoseloy.ventarapida.procesos

import androidx.recyclerview.widget.DiffUtil
import com.castellanoseloy.ventarapida.datos.Variable


//no mueve el recyclerview cuando se actualizan los datos.
class VariableDiffCallback(
    private val oldList: List<Variable>,
    private val newList: List<Variable>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldProduct = oldList[oldItemPosition]
        val newProduct = newList[newItemPosition]
        return oldProduct.idVariable == newProduct.idVariable
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldProduct = oldList[oldItemPosition]
        val newProduct = newList[newItemPosition]
        return oldProduct == newProduct
    }
}