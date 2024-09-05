package com.castellanoseloy.cataplus.datos

class ModeloActualizadorQuickSell (
    val sku: String,
    val stock: Int
)

data class UpdateRequest(
    val updates: List<ModeloActualizadorQuickSell>
)