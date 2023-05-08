package com.example.ventarapida.ui.factura_guardada

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.datos.ModeloFactura
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.FirebaseProductoFacturadosOComprados

import com.example.ventarapida.procesos.Utilidades.eliminarPuntosComasLetras
import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FacturaGuardadaViewModel : ViewModel() {


    val datosFactura = MutableLiveData<ModeloFactura>()


    val datosProductosFacturados = MutableLiveData<List<ModeloProductoFacturado>>()
    val subTotal = MutableLiveData<String>()
    val totalFactura = MutableLiveData<String>()
    val referencias = MutableLiveData<String>()
    val items = MutableLiveData<String>()

    fun cargarDatosFactura(modeloFactura: ModeloFactura?) {
        modeloFactura?.id_pedido?.let { obtenerFactura(it) }
    }

    private fun obtenerFactura(idPedido: String) {
        val database = FirebaseDatabase.getInstance()
        val productosRef = database.getReference("Factura").orderByChild("id_pedido").equalTo(idPedido)

        productosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val datosFacturaRecuperados = mutableListOf<ModeloFactura>()
                for (facturaSnapshot in dataSnapshot.children) {
                    val factura = facturaSnapshot.getValue(ModeloFactura::class.java)
                    factura?.let { datosFacturaRecuperados.add(it) }
                }
                datosFactura.value = datosFacturaRecuperados.firstOrNull()

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

     fun buscarProductos(idPedido: String) {
        val database = FirebaseDatabase.getInstance()
        val productosRef = database.getReference("ProductosFacturados").orderByChild("id_pedido").equalTo(idPedido)

        productosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val datosFactura = mutableListOf<ModeloProductoFacturado>()
                for (facturaSnapshot in dataSnapshot.children) {
                    val factura = facturaSnapshot.getValue(ModeloProductoFacturado::class.java)
                    factura?.let { datosFactura.add(it) }
                }
                datosProductosFacturados.value = datosFactura
                calcularTotal()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun calcularTotal() {
        val listaProductos = datosProductosFacturados.value ?: emptyList()

        // Calcular subtotal
        subTotal.value = listaProductos.sumOf { it.venta.toDouble() * it.cantidad.toDouble() }.toString().formatoMonenda()

        // Calcular total
        val subtotal = subTotal.value?.eliminarPuntosComasLetras()?.toDouble() ?: 0.0
        val envio = datosFactura.value?.envio?.toDouble() ?: 0.0
        val descuento = datosFactura.value?.descuento?.toDouble() ?: 0.0
        var totalDescuento = subtotal
        val porcentajeDescuento = descuento / 100
        totalDescuento *= (1 - porcentajeDescuento)
        totalDescuento += envio
        totalFactura.value = totalDescuento.toString().formatoMonenda()

        // Calcular información adicional
        referencias.value = listaProductos.size.toString().formatoMonenda()
        items.value = listaProductos.sumOf { it.cantidad.toDouble() }.toString().formatoMonenda()
    }

    fun eliminarFactura(context:Context) {


        val arrayListProductosFacturados = ArrayList(datosProductosFacturados.value ?: emptyList())

        FirebaseProductoFacturadosOComprados.eliminarProductoFacturado(
            "ProductosFacturados",
            arrayListProductosFacturados,
            context,
            "compra"
        )


//        //Restar cantidades de la factura
//        val productosSeleccionados = mutableMapOf<ModeloProducto, Int>()
//
//        datosProductosFacturados.value?.forEach { productoFacturado ->
//            val producto = ModeloProducto(
//                id = productoFacturado.id_producto
//            )
//            val cantidad = -1 * ( productoFacturado.cantidad.toInt())
//            productosSeleccionados[producto] = cantidad
//        }
////TODO

//        //crear cola de transacciones para restar
//        UtilidadesBaseDatos.guardarTransaccionesBd("venta",context, productosSeleccionados)
//        val transaccionesPendientes =
//            UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos(context)
//        FirebaseProductos.transaccionesCambiarCantidad(context, transaccionesPendientes)
//
//        FirebaseFacturaOCompra.eliminarFacturaOCompra("Factura", modeloFactura.id_pedido)
//

    }

}