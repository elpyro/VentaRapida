package com.castellanoseloy.ventarapida.ui.detalleCompra

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.castellanoseloy.ventarapida.MainActivity
import com.castellanoseloy.ventarapida.VistaPDFFacturaOCompra
import com.castellanoseloy.ventarapida.datos.ModeloFactura
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.procesos.*
import com.castellanoseloy.ventarapida.procesos.Utilidades.formatoMonenda
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
            total += producto.p_compra.toDouble() * cantidad.toDouble()
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
        listaProductosFacturados:ArrayList<ModeloProductoFacturado>
    ) {

        FirebaseFacturaOCompra.guardarDetalleFacturaOCompra("Compra",datosPedido)

        FirebaseProductoFacturadosOComprados.guardarProductoFacturado("ProductosComprados",listaProductosFacturados,"compra",context)

    }

    fun actualizarProducto(producto: ModeloProducto, nuevoPrecio: Double, cantidad:Int, nombre:String) {

        val productoEncontrado = MainActivity.compraProductosSeleccionados.keys.find { it.id == producto.id }
        if (productoEncontrado != null) {

            MainActivity.compraProductosSeleccionados.remove(productoEncontrado)
            productoEncontrado.p_compra = nuevoPrecio.toString()
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

    fun eliminarProducto(item: ModeloProducto) {
        MainActivity.compraProductosSeleccionados.remove(item)
        Toast.makeText(context,"Se ha eliminado: ${item.nombre}", Toast.LENGTH_LONG).show()
        val crearTono= CrearTono()
        crearTono.crearTono(context)
        totalFactura()
    }
}