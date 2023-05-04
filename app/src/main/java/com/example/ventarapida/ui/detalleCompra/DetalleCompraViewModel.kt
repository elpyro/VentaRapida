package com.example.ventarapida.ui.detalleCompra

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity
import com.example.ventarapida.VistaPDFFacturaOCompra
import com.example.ventarapida.datos.ModeloFactura
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
        productosSeleccionados: MutableMap<ModeloProducto, Int>,
        listaProductosFacturados:ArrayList<ModeloProductoFacturado>
    ) {


        UtilidadesBaseDatos.guardarTransaccionesBd("compra", context, productosSeleccionados)
        val transaccionesPendientes =
            UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos(context)
        FirebaseProductos.transaccionesCambiarCantidad(context, transaccionesPendientes)

        FirebaseFacturaOCompra.guardarFacturaOCompra("Compra",datosPedido)

        FirebaseProductoFacturadosOComprados.guardarProductoFacturado(
            "ProductosComprados",
            listaProductosFacturados
        )
            .addOnSuccessListener {
                MainActivity.progressDialog?.dismiss()
            }

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
            datosPedido["total"].toString()
        )


        val intent = Intent(context, VistaPDFFacturaOCompra::class.java)
        intent.putExtra("id", "enProceso")
        intent.putExtra("tablaReferencia", "Compra")
        intent.putExtra("datosFactura", modeloFactura)
        intent.putExtra("listaProductos", productosSeleccionados )
        context.startActivity(intent)

    }
}