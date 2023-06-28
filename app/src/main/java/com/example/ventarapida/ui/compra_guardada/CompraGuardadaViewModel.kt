package com.example.ventarapida.ui.compra_guardada

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity
import com.example.ventarapida.datos.ModeloFactura
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.datos.ModeloTransaccionSumaRestaProducto
import com.example.ventarapida.procesos.FirebaseFacturaOCompra
import com.example.ventarapida.procesos.FirebaseProductoFacturadosOComprados
import com.example.ventarapida.procesos.FirebaseProductos

import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class CompraGuardadaViewModel : ViewModel() {

    val datosFactura = MutableLiveData<ModeloFactura>()
    val datosProductosComprados = MutableLiveData<List<ModeloProductoFacturado>>()
    val totalFactura = MutableLiveData<String>()
    val referencias = MutableLiveData<String>()
    val items = MutableLiveData<String>()

    fun cargarDatosFactura(modeloFactura: ModeloFactura?) {
        modeloFactura?.id_pedido?.let { obtenerFactura(it) }
    }

    private fun obtenerFactura(idPedido: String) {
        val database = FirebaseDatabase.getInstance()
        val productosRef = database.getReference(MainActivity.datosEmpresa.id).child("Compra").orderByChild("id_pedido").equalTo(idPedido)
        productosRef.keepSynced(true)
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
        val productosRef = database.getReference(MainActivity.datosEmpresa.id).child("ProductosComprados").orderByChild("id_pedido").equalTo(idPedido)
        productosRef.keepSynced(true)
        productosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val datosFactura = mutableListOf<ModeloProductoFacturado>()
                for (facturaSnapshot in dataSnapshot.children) {
                    val factura = facturaSnapshot.getValue(ModeloProductoFacturado::class.java)
                    factura?.let { datosFactura.add(it) }
                }
                datosProductosComprados.value = datosFactura
                calcularTotal()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun calcularTotal() {
        val listaProductos = datosProductosComprados.value ?: emptyList()

        // Calcular subtotal
        totalFactura.value = listaProductos.sumByDouble { it.costo.toDouble() * it.cantidad.toDouble() }.toString().formatoMonenda()

        // Calcular información adicional
        referencias.value = listaProductos.size.toString().formatoMonenda()
        items.value = listaProductos.sumByDouble { it.cantidad.toDouble() }.toString().formatoMonenda()
    }

    fun eliminarCompra( context:Context) {

        val arrayListProductosFacturados = ArrayList(datosProductosComprados.value ?: emptyList())
        val listaRestarInventario = arrayListOf<ModeloTransaccionSumaRestaProducto>()

        FirebaseFacturaOCompra.eliminarFacturaOCompra("Compra",datosFactura.value!!.id_pedido)

        //se marca como venta para que reste al inventario y guarda la transaccion en la base de datos
        FirebaseProductoFacturadosOComprados.eliminarProductoFacturado(
            "ProductosComprados",
            arrayListProductosFacturados,
            context,
            "venta"
        )

        arrayListProductosFacturados.forEach{producto->
            //calculamos el precio descuento para tener la referencia para los reportes

                val restarProducto = ModeloTransaccionSumaRestaProducto(
                    idTransaccion =  datosFactura.value!!.id_pedido,  //la transaccion tiene el mismo id
                    idProducto = producto.id_producto,
                    cantidad = producto.cantidad,
                    subido ="false"
                )
                listaRestarInventario.add(restarProducto)
            }

        //ejecuta la transaccion y borra las que hay en la base de datos  si se ejecuta
        FirebaseProductos.transaccionesCambiarCantidad(context, listaRestarInventario)
    }


}
