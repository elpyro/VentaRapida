package com.example.ventarapida.ui.detalleCompra

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.*
import com.example.ventarapida.procesos.Utilidades.eliminarPuntosComasLetras
import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import java.util.*
import kotlin.collections.HashMap

class DetalleCompraViewModel : ViewModel() {

    lateinit var context: Context // propiedad para almacenar el contexto

    var subTotal= MutableLiveData<String>()
    var totalFactura= MutableLiveData<String>()
    var referencias= MutableLiveData<String>()
    var itemsSeleccionados= MutableLiveData<String>()
    var mensajeToast= MutableLiveData<String>()


    fun totalFactura(){

        var total = 0.0
        var items=0
        for ((producto, cantidad) in MainActivity.compraProductosSeleccionados) {
            //multiplicar por -1 para sumar al inventario
            items +=  cantidad
            total += producto.p_compra.eliminarPuntosComasLetras().toDouble() * cantidad.toDouble()
        }

        subTotal.value =  total.toString().formatoMonenda()

        totalFactura.value =  "Total: "+ total.toString().formatoMonenda()

        referencias.value=  MainActivity.compraProductosSeleccionados.count { it.value != 0  }.toString()

        itemsSeleccionados.value = items.toString()

        val preferencias= Preferencias()
        preferencias.guardarPreferenciaListaSeleccionada(context,
            MainActivity.compraProductosSeleccionados,"compra_seleccionada")
    }


    fun subirDatos(
        datosPedido: HashMap<String, Any>,
        productosSeleccionados: MutableMap<ModeloProducto, Int>
    ) {


        UtilidadesBaseDatos.guardarTransaccionesBd("compra",context, productosSeleccionados)
        val transaccionesPendientes =
            UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos(context)
        FirebaseProductos.transaccionesCambiarCantidad(context, transaccionesPendientes)

        FirebaseFacturaOCompra.guardarFacturaOCompra("Compra",datosPedido)

        val listaProductosFacturados = arrayListOf<ModeloProductoFacturado>()

        val idPedido = datosPedido["id_pedido"].toString()
        val horaActual = datosPedido["hora"].toString()
        val fechaActual = datosPedido["fecha"].toString()


        productosSeleccionados.forEach{ (producto, cantidadSeleccionada)->
            //calculamos el precio descuento para tener la referencia para los reportes
            if (cantidadSeleccionada!=0){

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
                    fecha = fechaActual,
                    hora=horaActual,
                    imagenUrl=producto.url
                )
                listaProductosFacturados.add(productoFacturado)
            }
        }

        FirebaseProductoFacturadosOComprados.guardarProductoFacturado("ProductosComprados" ,listaProductosFacturados)

    }

    fun actualizarProducto(producto: ModeloProducto, nuevoPrecio: Int, cantidad:Int, nombre:String) {

        val productoEncontrado = MainActivity.compraProductosSeleccionados.keys.find { it.id == producto.id }
        if (productoEncontrado != null) {

            MainActivity.compraProductosSeleccionados.remove(productoEncontrado)
            productoEncontrado.p_compra = nuevoPrecio.toString().eliminarPuntosComasLetras()
            productoEncontrado.nombre = nombre
            MainActivity.compraProductosSeleccionados[productoEncontrado] = cantidad

        }

        val crearTono= CrearTono()
        crearTono.crearTono(context)
        totalFactura()
    }

    fun limpiarProductosSelecionados(context: Context) {
        MainActivity.compraProductosSeleccionados.clear()
        val preferencias= Preferencias()
        preferencias.guardarPreferenciaListaSeleccionada(context,
            MainActivity.compraProductosSeleccionados,"compra_seleccionada")

    }
}