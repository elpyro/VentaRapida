package com.example.ventarapida.ui.factura

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity.Companion.productosSeleccionados
import com.example.ventarapida.ui.datos.ModeloProductoFacturado
import com.example.ventarapida.ui.datos.ModeloProducto
import com.example.ventarapida.ui.procesos.CrearTono
import com.example.ventarapida.ui.procesos.FirebaseFactura
import com.example.ventarapida.ui.procesos.Preferencias
import com.example.ventarapida.ui.procesos.Utilidades.eliminarPuntosComas
import com.example.ventarapida.ui.procesos.Utilidades.formatoMonenda
import com.example.ventarapida.ui.procesos.UtilidadesBaseDatos.guardarTransaccionesBd
import com.example.ventarapida.ui.procesos.UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos
import com.example.ventarapida.ui.procesos.FirebaseProductoFacturados.guardarProductoFacturado
import com.example.ventarapida.ui.procesos.FirebaseProductos.transaccionesCambiarCantidad
import java.util.*
import kotlin.collections.HashMap


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

    fun subirDatos(
        datosPedido: HashMap<String, Any>,
        productosSeleccionados: MutableMap<ModeloProducto, Int>
    ) {

        guardarTransaccionesBd(context , productosSeleccionados)

        val transaccionesPendientes=  obtenerTransaccionesSumaRestaProductos(context)
        transaccionesCambiarCantidad(context,transaccionesPendientes)

        FirebaseFactura.guardarFactura(datosPedido)

        val listaProductosFacturados = arrayListOf<ModeloProductoFacturado>()

        val idPedido = datosPedido["id_pedido"].toString()
        val horaActual = datosPedido["hora"].toString()
        val fechaActual = datosPedido["fecha"].toString()

        val descuento = datosPedido["descuento"].toString().toInt()
        val envio=datosPedido["envio"].toString().toInt()

        productosSeleccionados.forEach{ (producto, cantidadSeleccionada)->
            //calculamos el precio descuento para tener la referencia para los reportes
            val porcentajeDescuento = descuento.toDouble() / 100
            var precioDescuento:Double=producto.p_diamante.toDouble()
            precioDescuento *= (1 - porcentajeDescuento)

            precioDescuento += envio.toDouble()

        val productoFacturado = ModeloProductoFacturado(
            id_producto_pedido = UUID.randomUUID().toString(),
            id_producto = producto.id,
            id_pedido = idPedido,
            id_vendedor = "idVendedor",
            vendedor = "Nombre vendedor",
            producto = producto.nombre,
            cantidad = cantidadSeleccionada.toString(),
            costo = producto.p_compra,
            venta = producto.p_diamante,
            precioDescuentos = precioDescuento.toString().formatoMonenda()!!,
            fecha = fechaActual,
            hora=horaActual,
            imagenUrl=producto.url
        )
        listaProductosFacturados.add(productoFacturado)
        }

        guardarProductoFacturado(listaProductosFacturados)
            .addOnSuccessListener {
                Toast.makeText(context,"Producto Facturado", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(context,"Error al guardar", Toast.LENGTH_LONG).show()
            }
    }

    fun actualizarProducto(producto: ModeloProducto, nuevoDiamante: Int, cantidad:Int, nombre:String) {
        val productoEncontrado = productosSeleccionados.keys.find { it.id == producto.id }
        if (productoEncontrado != null) {
            val index = productosSeleccionados.keys.indexOf(productoEncontrado)
            productosSeleccionados.remove(productoEncontrado)
            productoEncontrado.p_diamante = nuevoDiamante.toString().eliminarPuntosComas()
            productoEncontrado.nombre = nombre
            if (cantidad>0){
                productosSeleccionados[productoEncontrado] = 0
                moverProducto(productoEncontrado, index)
                productosSeleccionados[productoEncontrado] = cantidad
            }

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