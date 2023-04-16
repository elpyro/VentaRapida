package com.example.ventarapida.ui.procesos

import android.content.Context
import com.example.ventarapida.ui.datos.ModificarCantidadProducto
import com.google.gson.Gson
import java.util.*
class PreferenciaModificarCantidad {

    fun eliminarRegistroPreferencia(context: Context, id_transaccion: String) {
        val prefs = context.getSharedPreferences("actualizar_cantidades", Context.MODE_PRIVATE)
        val listaString = prefs.getString("productos", "") ?: ""
        val miLista = mutableListOf<ModificarCantidadProducto>()
        if (listaString.isNotEmpty()) {
            miLista.addAll(Gson().fromJson(listaString, Array<ModificarCantidadProducto>::class.java).toList())
        }
        miLista.find { it.id_transaccion == id_transaccion }?.let { producto ->
            miLista.remove(producto)
            prefs.edit().putString("productos", Gson().toJson(miLista)).apply()
        }
    }

    fun crearColaSubida(context: Context) {
        val prefs = context.getSharedPreferences("actualizar_cantidades", Context.MODE_PRIVATE)
        val listaString = prefs.getString("productos", "") ?: ""
        val preferenciaCargada = if (listaString.isNotEmpty()) {
            Gson().fromJson(listaString, Array<ModificarCantidadProducto>::class.java).toList()
        } else {
            emptyList()
        }
        preferenciaCargada.forEach { (id_transaccion, id_producto, cantidad) ->
            UtilidadesFirebase.CambiarCantidad(context, id_transaccion, id_producto, cantidad)
        }
    }
}