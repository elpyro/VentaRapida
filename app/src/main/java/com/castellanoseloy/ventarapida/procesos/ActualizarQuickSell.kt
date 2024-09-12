package com.castellanoseloy.ventarapida.procesos

import android.util.Log
import com.castellanoseloy.ventarapida.datos.ModeloActualizadorQuickSell
import com.castellanoseloy.ventarapida.datos.UpdateRequest
import okhttp3.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class ActualizarQuickSell(private val sku: String, private val quantity: Int) {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun updateInventory() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Crear el objeto de actualización
                val update = ModeloActualizadorQuickSell(sku, quantity)
                val updateRequest = UpdateRequest(listOf(update))

                // Convertir el objeto a JSON
                val json = gson.toJson(updateRequest)

                // Crear el cuerpo de la solicitud
                val body =
                    RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)

                // Construir la solicitud
                val request = Request.Builder()
                    .url("https://merchant.api.quicksell.co/public/v1/m/inventory/skus")
                    .put(body)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader(
                        "x-api-key",
                        "ZzuHkQm32INQ/ZU9G5nbF4nIQGC6AoFq5T952tvp349dM3CgBLEkVj4L5lGvnAZP"
                    )
                    .build()

                client.newCall(request).execute().use { response ->  // Aquí se asegura el cierre del ResponseBody
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            // Manejar éxito (e.g., mostrar un mensaje de éxito)
                            Log.i("QuickSell","Inventario quicksell actualizado con éxito de: $sku , cantidad: $quantity")
                        } else {
                            // Manejar error (e.g., mostrar un mensaje de error)
                            Log.i("QuickSell","Error en la actualización del inventario:  de: $sku con el código de respuesta: ${response.code}")
                        }
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    // Manejar error (e.g., mostrar un mensaje de error)
                    e.printStackTrace()
                }
            }
        }
    }
}
