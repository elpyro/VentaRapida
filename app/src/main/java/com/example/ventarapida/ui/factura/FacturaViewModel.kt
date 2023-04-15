package com.example.ventarapida.ui.factura

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity
import com.example.ventarapida.MainActivity.Companion.productosSeleccionados
import com.example.ventarapida.ui.datos.ModeloProducto
import com.example.ventarapida.ui.procesos.CrearTono
import com.example.ventarapida.ui.procesos.Preferencias
import com.example.ventarapida.ui.procesos.Utilidades.eliminarPuntosComas
import com.example.ventarapida.ui.procesos.Utilidades.formatoMonenda


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
            mensajeToast.value=producto.nombre.toString()
            val crearTono= CrearTono()
            crearTono.crearTono(context)
        }



    fun actualizarProducto(producto: ModeloProducto, nuevoDiamante: Int, cantidad:Int, nombre:String) {
        val productoEncontrado = productosSeleccionados.keys.find { it.id == producto.id }
        if (productoEncontrado != null) {
            val index = productosSeleccionados.keys.indexOf(productoEncontrado)
            MainActivity.productosSeleccionados.remove(productoEncontrado)
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
}