package com.castellanoseloy.ventarapida.ui.factura_guardada

import android.content.ContentValues
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.castellanoseloy.ventarapida.MainActivity
import com.castellanoseloy.ventarapida.baseDatos.MyDatabaseHelper
import com.castellanoseloy.ventarapida.datos.ModeloFactura
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.datos.ModeloTransaccionSumaRestaProducto
import com.castellanoseloy.ventarapida.procesos.FirebaseFacturaOCompra
import com.castellanoseloy.ventarapida.procesos.FirebaseProductoFacturadosOComprados
import com.castellanoseloy.ventarapida.procesos.FirebaseProductos
import com.castellanoseloy.ventarapida.procesos.Utilidades.formatoMonenda
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class FacturaGuardadaViewModel : ViewModel() {

    val datosFactura = MutableLiveData<ModeloFactura>()
    val datosProductosFacturados = MutableLiveData<List<ModeloProductoFacturado>>()
    val subTotal = MutableLiveData<String>()
    val totalFactura = MutableLiveData<String>()
    val referencias = MutableLiveData<String>()
    val items = MutableLiveData<String>()
    var datosCargados=false
    fun cargarDatosFactura(modeloFactura: ModeloFactura?) {
        modeloFactura?.id_pedido?.let { obtenerFactura(it) }
    }

    private fun obtenerFactura(idPedido: String) {
        val database = FirebaseDatabase.getInstance()
        val productosRef = database.getReference(MainActivity.datosEmpresa.id).child("Factura").orderByChild("id_pedido").equalTo(idPedido)
        productosRef.keepSynced(true)
        productosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val datosFacturaRecuperados = mutableListOf<ModeloFactura>()
                for (facturaSnapshot in dataSnapshot.children) {
                    val factura = facturaSnapshot.getValue(ModeloFactura::class.java)
                    factura?.let { datosFacturaRecuperados.add(it) }
                }

                datosFactura.value = datosFacturaRecuperados.firstOrNull()
                datosCargados=true
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

     fun buscarProductos(idPedido: String) {
        val database = FirebaseDatabase.getInstance()
        val productosRef = database.getReference(MainActivity.datosEmpresa.id).child("ProductosFacturados").orderByChild("id_pedido").equalTo(idPedido)
         productosRef.keepSynced(true)
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

    fun calcularTotal() {
        val listaProductos = datosProductosFacturados.value ?: emptyList()

        // Calcular subtotal
        subTotal.value = listaProductos.sumOf { it.venta.toDouble() * it.cantidad.toDouble() }.toString()

        // Calcular total
        val subtotal = subTotal.value?.toDouble() ?: 0.00
        val envio = datosFactura.value?.envio?.toDouble() ?: 0.00
        val descuento = datosFactura.value?.descuento?.toDouble() ?: 0.0
        var totalDescuento = subtotal
        val porcentajeDescuento = descuento / 100
        totalDescuento *= (1 - porcentajeDescuento)
        totalDescuento += envio
        totalFactura.value = totalDescuento.toString().formatoMonenda()

        // Calcular información adicional
        referencias.value = listaProductos.size.toString()
        items.value = listaProductos.sumOf { it.cantidad.toInt() }.toString()
    }

    fun eliminarFactura(context:Context) {
        val dbHelper = MyDatabaseHelper(context)
        val db = dbHelper.readableDatabase

        val arrayListProductosFacturados = ArrayList(datosProductosFacturados.value ?: emptyList())
        val listaSumarInventario = arrayListOf<ModeloTransaccionSumaRestaProducto>()

        FirebaseFacturaOCompra.eliminarFacturaOCompra("Factura",datosFactura.value!!.id_pedido)

        //se marca como compra para que sume al inventario
        FirebaseProductoFacturadosOComprados.eliminarProductoFacturado(
            "ProductosFacturados",
            arrayListProductosFacturados,
            context,
            "compra"
        )

        arrayListProductosFacturados.forEach{producto->

            val idTransaccion = UUID.randomUUID().toString()
            val values = ContentValues().apply {
                put("idTransaccion", idTransaccion)
                put("idProducto", producto.id_producto)
                put("cantidad", (-1 * producto.cantidad.toInt()).toString())
                put("subido", "false")
            }

            // Guardamos la referencia en la base de datos para cambiar la cantidad del producto
            db.insert("transaccionesSumaRestaProductos", null, values)


            val sumarProducto = ModeloTransaccionSumaRestaProducto(
                idTransaccion = idTransaccion,
                idProducto =producto.id_producto,
                cantidad = (-1 * producto.cantidad.toInt()).toString(),
                subido ="false"
            )

            listaSumarInventario.add(sumarProducto)

        }
        db.close()
        //ejecutamos la transaccion
        FirebaseProductos.transaccionesCambiarCantidad(context, listaSumarInventario)

    }

}
