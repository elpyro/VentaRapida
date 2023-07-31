package com.example.ventarapida.ui.detalleVenta

import android.content.Context
import android.content.Intent


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity.Companion.ventaProductosSeleccionados
import com.example.ventarapida.VistaPDFFacturaOCompra
import com.example.ventarapida.datos.ModeloClientes
import com.example.ventarapida.datos.ModeloFactura
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.procesos.CrearTono
import com.example.ventarapida.procesos.FirebaseFacturaOCompra
import com.example.ventarapida.procesos.FirebaseProductoFacturadosOComprados
import com.example.ventarapida.procesos.Preferencias
import com.example.ventarapida.procesos.Utilidades.eliminarPuntosComasLetras
import com.example.ventarapida.procesos.Utilidades.formatoMonenda


import java.util.*
import kotlin.collections.HashMap


class DetalleVentaViewModel : ViewModel() {

    lateinit var context: Context // propiedad para almacenar el contexto

    companion object{
        var datosCliente= MutableLiveData<ModeloClientes>()
    }

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
            for ((producto, cantidad) in ventaProductosSeleccionados) {
                items += cantidad
                total += producto.p_diamante.toDouble() * cantidad.toDouble()
            }
            // Obtiene el porcentaje de descuento y lo resta de total
            val porcentajeDescuento = descuento.value!!.toDouble() / 100
            total *= (1 - porcentajeDescuento)

            total += envio.value!!.toDouble()

            subTotal.value =  total.toString().formatoMonenda()

            totalFactura.value =  "Total: "+ total.toString().formatoMonenda()

            referencias.value=  ventaProductosSeleccionados.count { it.value != 0  }.toString()

            itemsSeleccionados.value = items.toString()

            val preferencias= Preferencias()
            preferencias.guardarPreferenciaListaSeleccionada(context,
                ventaProductosSeleccionados,"venta_seleccionada")
    }


    fun subirDatos(
        datosPedido: HashMap<String, Any>,
        listaProductosFacturados:ArrayList<ModeloProductoFacturado>
    ) {

        FirebaseFacturaOCompra.guardarDetalleFacturaOCompra("Factura",datosPedido)

        FirebaseProductoFacturadosOComprados.guardarProductoFacturado("ProductosFacturados",listaProductosFacturados,"venta",context)

    }

    fun actualizarProducto(producto: ModeloProducto, nuevoPrecio: Double, cantidad:Int, nombre:String) {
        val productoEncontrado = ventaProductosSeleccionados.keys.find { it.id == producto.id }
        if (productoEncontrado != null) {

            ventaProductosSeleccionados.remove(productoEncontrado)
            productoEncontrado.p_diamante = nuevoPrecio.toString()
            productoEncontrado.nombre = nombre
            ventaProductosSeleccionados[productoEncontrado] = cantidad

        }

        val crearTono= CrearTono()
        crearTono.crearTono(context)
        totalFactura()
    }

    fun limpiar(context: Context) {
        ventaProductosSeleccionados.clear()
        val preferencias= Preferencias()
        preferencias.guardarPreferenciaListaSeleccionada(context, ventaProductosSeleccionados,"venta_seleccionada")


    }



    fun abrirPDFConPreferencias(
            productosSeleccionados: ArrayList<ModeloProductoFacturado>,
            datosPedido: HashMap<String, Any>
        ) {

            val modeloFactura = ModeloFactura(
                datosPedido["id_pedido"].toString(),
                datosPedido["nombre"].toString(),
                datosPedido["telefono"].toString(),
                datosPedido["documento"].toString(),
                datosPedido["direccion"].toString(),
                datosPedido["descuento"].toString(),
                datosPedido["envio"].toString(),
                datosPedido["fecha"].toString(),
                datosPedido["hora"].toString(),
                datosPedido["id_vendedor"].toString(),
                datosPedido["nombre_vendedor"].toString(),
                datosPedido["total"].toString(),
                datosPedido["fechaBusquedas"] as Long
            )


            val intent = Intent(context, VistaPDFFacturaOCompra::class.java)
            intent.putExtra("id", "enProceso")
            intent.putExtra("tablaReferencia", "Factura")
            intent.putExtra("datosFactura", modeloFactura)
            intent.putExtra("listaProductos", productosSeleccionados )
            context.startActivity(intent)

        }

}