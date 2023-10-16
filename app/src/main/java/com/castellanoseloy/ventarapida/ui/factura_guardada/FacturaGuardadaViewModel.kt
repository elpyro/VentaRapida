package com.castellanoseloy.ventarapida.ui.factura_guardada

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.util.Log
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
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class FacturaGuardadaViewModel : ViewModel() {

    private lateinit var detalleFactura: Query
    private lateinit var productosRef: Query
    private lateinit var escuchadorDetalleFactura: ValueEventListener
    private lateinit var escuchadorProdutos: ValueEventListener
    private var progressDialog: ProgressDialog? = null
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
        detalleFactura = database.getReference(MainActivity.datosEmpresa.id).child("Factura").orderByChild("id_pedido").equalTo(idPedido)

        escuchadorDetalleFactura=detalleFactura.addValueEventListener(object : ValueEventListener {
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
        productosRef = database.getReference(MainActivity.datosEmpresa.id).child("ProductosFacturados").orderByChild("id_pedido").equalTo(idPedido)

        escuchadorProdutos= productosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val datosFactura = mutableListOf<ModeloProductoFacturado>()
                for (facturaSnapshot in dataSnapshot.children) {
                    val factura = facturaSnapshot.getValue(ModeloProductoFacturado::class.java)
                    factura?.let { datosFactura.add(it) }
                }
                Log.d("Escuchadores", "Ocurrio un envento en el modulo FACTURA GUARDADA")
                datosProductosFacturados.value = datosFactura
                calcularTotal()
                progressDialog?.dismiss()
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

    fun processDialogo(requireContext: Context) {
        progressDialog = ProgressDialog(requireContext)
        progressDialog?.setMessage("Cargando...") // Mensaje que se mostrará
        progressDialog?.setCancelable(false) // Para evitar que se cierre al tocar fuera de él
        progressDialog?.show()
    }

    fun detenerEscuchadores(){
        if (::escuchadorDetalleFactura.isInitialized && ::detalleFactura.isInitialized) {
            detalleFactura.removeEventListener(escuchadorDetalleFactura)
        }

        if (::escuchadorProdutos.isInitialized && ::productosRef.isInitialized) {
            productosRef.removeEventListener(escuchadorProdutos)
        }
    }
}
