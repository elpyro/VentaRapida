package com.example.ventarapida.ui.data

import android.net.Uri

data class ImageUploadRequest(
    val fileUri: Uri?,
    val storageRefString: String?,
    val idProducto: String?
)