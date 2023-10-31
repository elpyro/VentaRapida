@file:Suppress("DEPRECATION")

package com.castellanoseloy.ventarapida.ui.compra_guardada

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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class CompraGuardadaViewModel : ViewModel() {

    private lateinit var detalleFactura: Query
    private lateinit var productosRef: Query
    private lateinit var escuchadorDetalleFactura: ValueEventListener
    private lateinit var escuchadorProdutos: ValueEventListener
    private var progressDialog: ProgressDialog? = null
    val datosFactura = MutableLiveData<ModeloFactura>()
    val datosProductosComprados = MutableLiveData<List<ModeloProductoFacturado>>()
    val totalFactura = MutableLiveData<String>()
    val referencias = MutableLiveData<String>()
    val items = MutableLiveData<String>()
    val database = FirebaseDatabase.getInstance()
    fun cargarDatosFactura(modeloFactura: ModeloFactura?) {
        modeloFactura?.id_pedido?.let { obtenerFactura(it) }
    }

    private fun obtenerFactura(idPedido: String) {

        detalleFactura = database.getReference(MainActivity.datosEmpresa.id).child("Compra").orderByChild("id_pedido").equalTo(idPedido)

        escuchadorDetalleFactura= detalleFactura.addValueEventListener(object : ValueEventListener {
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
        Log.d("Escuchadores", "Se ha llamado el escuchador de SURTIDO GUARDADO")

        productosRef = database.getReference(MainActivity.datosEmpresa.id).child("ProductosComprados").orderByChild("id_pedido").equalTo(idPedido)

        escuchadorProdutos=productosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val datosFactura = mutableListOf<ModeloProductoFacturado>()
                for (facturaSnapshot in dataSnapshot.children) {
                    val factura = facturaSnapshot.getValue(ModeloProductoFacturado::class.java)
                    factura?.let { datosFactura.add(it) }
                }
                Log.d("Escuchadores", "Ocurrio un envento en el modulo SURTIDO  GUARDADO")
                datosProductosComprados.value = datosFactura
                calcularTotal()
                progressDialog?.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    private fun calcularTotal() {
        val listaProductos = datosProductosComprados.value ?: emptyList()

        // Calcular subtotal
        totalFactura.value = listaProductos.sumByDouble { it.costo.toDouble() * it.cantidad.toDouble() }.toString()

        // Calcular información adicional
        referencias.value = listaProductos.size.toString()
        items.value = listaProductos.sumBy { it.cantidad.toInt() }.toString()
    }

    fun eliminarCompra( context:Context) {
        val dbHelper = MyDatabaseHelper(context)
        val db = dbHelper.readableDatabase

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

            arrayListProductosFacturados.forEach { producto ->

                val idTransaccion = UUID.randomUUID().toString()
                val values = ContentValues().apply {
                    put("idTransaccion", idTransaccion)
                    put("idProducto", producto.id_producto)
                    put("cantidad", (producto.cantidad.toInt()).toString())
                    put("subido", "false")
                }

                // Guardamos la referencia en la base de datos para cambiar la cantidad del producto
                db.insert("transaccionesSumaRestaProductos", null, values)


                val sumarProducto = ModeloTransaccionSumaRestaProducto(
                    idTransaccion = idTransaccion,
                    idProducto = producto.id_producto,
                    cantidad = (producto.cantidad.toInt()).toString(),
                    subido = "false"
                )

                listaRestarInventario.add(sumarProducto)

            }


        db.close()
        //ejecutamos la transaccion
        FirebaseProductos.transaccionesCambiarCantidad(context, listaRestarInventario)
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
            Log.d("Escuchadores", "se ha eliminado el escuchador de los productos de sustidos guardaos")
        }
    }


}
