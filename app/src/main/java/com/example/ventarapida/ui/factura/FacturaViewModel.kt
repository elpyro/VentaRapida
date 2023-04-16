package com.example.ventarapida.ui.factura

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity.Companion.productosSeleccionados
import com.example.ventarapida.ui.datos.ModeloProducto
import com.example.ventarapida.ui.datos.ModificarCantidadProducto
import com.example.ventarapida.ui.procesos.CrearTono
import com.example.ventarapida.ui.procesos.Preferencias
import com.example.ventarapida.ui.procesos.Utilidades.eliminarPuntosComas
import com.example.ventarapida.ui.procesos.Utilidades.formatoMonenda
import com.example.ventarapida.ui.procesos.UtilidadesFirebase.CambiarCantidad
import com.google.firebase.database.*
import com.google.gson.Gson
import java.util.*


class FacturaViewModel : ViewModel() {

    lateinit var context: Context // propiedad para almacenar el contexto

    var subTotal= MutableLiveData<String>()
    var totalFactura= MutableLiveData<String>()
    var referencias= MutableLiveData<String>()
    var itemsSeleccionados= MutableLiveData<String>()
    var envio = MutableLiveData<String>().apply { value = "0" }
    var descuento= MutableLiveData<String>().apply { value = "0" }
    var mensajeToast= MutableLiveData<String>()


    fun totalFactura(){

            var total = 0.0
            var items=0
            for ((producto, cantidad) in productosSeleccionados) {
                items += cantidad
                total += producto.p_diamante.eliminarPuntosComas().toDouble() * cantidad.toDouble()
            }
            // Obtiene el porcentaje de descuento y lo resta de total
            val porcentajeDescuento = descuento.value!!.toDouble() / 100
            total *= (1 - porcentajeDescuento)

            total += envio.value!!.toDouble()

            subTotal.value =  total.toString().formatoMonenda()

            totalFactura.value =  "Total: "+ total.toString().formatoMonenda()

            referencias.value= productosSeleccionados.size.toString()

            itemsSeleccionados.value = items.toString()

            val preferencias= Preferencias()
            preferencias.guardarPreferenciaListaSeleccionada(context,
                productosSeleccionados)
    }
        fun mensaje(producto: ModeloProducto){
            mensajeToast.value=producto.nombre
            val crearTono= CrearTono()
            crearTono.crearTono(context)
        }

    fun subirColaModificarCantidad() {
        val prefs = context.getSharedPreferences("actualizar_cantidades", Context.MODE_PRIVATE)
        val listaString = prefs.getString("productos", "") ?: ""
        var miLista = mutableListOf<ModificarCantidadProducto>()

        if (!listaString.isEmpty()) {
            miLista.addAll(Gson().fromJson(listaString, Array<ModificarCantidadProducto>::class.java).toList())
        }

        productosSeleccionados.forEach { (producto, cantidadSeleccionada) ->
            val idTransaccion = UUID.randomUUID().toString()
            val miObjeto = ModificarCantidadProducto(idTransaccion,producto.id!!, cantidadSeleccionada.toString())

            miLista.add(miObjeto)

            val guardarLista = Gson().toJson(miLista)
            prefs.edit().putString("productos", guardarLista).apply()

            CambiarCantidad(context,idTransaccion, producto.id!!, cantidadSeleccionada.toString())
        }
    }






    fun actualizarProducto(producto: ModeloProducto, nuevoDiamante: Int, cantidad:Int, nombre:String) {
        val productoEncontrado = productosSeleccionados.keys.find { it.id == producto.id }
        if (productoEncontrado != null) {
            val index = productosSeleccionados.keys.indexOf(productoEncontrado)
            productosSeleccionados.remove(productoEncontrado)
            productoEncontrado.p_diamante = nuevoDiamante.toString().eliminarPuntosComas()
            productoEncontrado.nombre = nombre
            productosSeleccionados[productoEncontrado] = 0
            moverProducto(productoEncontrado, index)
            productosSeleccionados[productoEncontrado] = cantidad
        }

        val crearTono= CrearTono()
        crearTono.crearTono(context)
        totalFactura()
    }

    fun moverProducto(producto: ModeloProducto, nuevaPosicion: Int) {
        val indexActual = productosSeleccionados.keys.indexOf(producto)
        if (indexActual != -1 && indexActual != nuevaPosicion) {
            productosSeleccionados.remove(producto)
            val listaMutable = productosSeleccionados.keys.toMutableList()
            listaMutable.add(nuevaPosicion, producto)
            productosSeleccionados.clear()
            listaMutable.forEach { p -> productosSeleccionados.put(p, productosSeleccionados[p] ?: 1) }
        }
    }

    fun limpiarProductosSelecionados(context: Context) {
        productosSeleccionados.clear()
        val preferencias=Preferencias()
        preferencias.guardarPreferenciaListaSeleccionada(context, productosSeleccionados)

    }
}