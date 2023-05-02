package com.example.ventarapida.ui.agregarProductoFactura

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.datos.ModeloFactura

import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.*
import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class AgregarProductoFacturaViewModel : ViewModel() {

    lateinit var context: Context // propiedad para almacenar el contexto
    val productosLiveData = MutableLiveData<List<ModeloProducto>>()
    val totalSeleccionLiveData= MutableLiveData<String>()

    var totalCarritoLiveData= MutableLiveData<String>()
    fun restarProductoSeleccionado( producto: ModeloProducto){
        val id_producto = producto.id
        val productoEncontrado = AgregarProductoFactura.productosSeleccionadosAgregar.keys.find { it.id == id_producto }
        if (productoEncontrado != null) {
            val nuevaCantidad = AgregarProductoFactura.productosSeleccionadosAgregar[productoEncontrado]!! - 1
            AgregarProductoFactura.productosSeleccionadosAgregar.remove(productoEncontrado)
            if (nuevaCantidad > 0) {
                AgregarProductoFactura.productosSeleccionadosAgregar[producto] = nuevaCantidad
            }
        }
        calcularTotal()
    }
    fun agregarProductoSeleccionado(producto: ModeloProducto) {


        val id_producto=producto.id
        val productoEncontrado = AgregarProductoFactura.productosSeleccionadosAgregar.keys.find { it.id == id_producto }
        if (productoEncontrado != null) {

            val nuevaCantidad = AgregarProductoFactura.productosSeleccionadosAgregar[productoEncontrado]!! + 1
            AgregarProductoFactura.productosSeleccionadosAgregar.remove(productoEncontrado)
            if (nuevaCantidad > 0) {
                AgregarProductoFactura.productosSeleccionadosAgregar[producto] = nuevaCantidad
            }

        }else{
            AgregarProductoFactura.productosSeleccionadosAgregar[producto] = 1
        }

        val crearTono= CrearTono()
        crearTono.crearTono(context)
        calcularTotal()

    }

    fun calcularTotal(){

        var total = 0.0

        for ((producto, cantidad) in AgregarProductoFactura.productosSeleccionadosAgregar) {
            total += producto.p_diamante.toDouble() * cantidad.toDouble()
        }

        totalCarritoLiveData.value =  total.toString().formatoMonenda()

        totalSeleccionLiveData.value= AgregarProductoFactura.productosSeleccionadosAgregar.size.toString()

        //aqui va el codigo si quiere guardar la preferencia
    }


    fun actualizarCantidadProducto(producto: ModeloProducto, nuevaCantidad: Int) {
        val id_producto=producto.id
        val productoEncontrado = AgregarProductoFactura.productosSeleccionadosAgregar.keys.find { it.id == id_producto }
        if (productoEncontrado != null) {
            if (nuevaCantidad > 0) {
                AgregarProductoFactura.productosSeleccionadosAgregar[productoEncontrado] = nuevaCantidad
            } else {
                AgregarProductoFactura.productosSeleccionadosAgregar.remove(productoEncontrado)
            }

        }else{
            AgregarProductoFactura.productosSeleccionadosAgregar[producto] = nuevaCantidad
        }

        val crearTono= CrearTono()
        crearTono.crearTono(context)
        calcularTotal()
    }

    fun eliminarCarrito(){
        AgregarProductoFactura.productosSeleccionadosAgregar.clear()
        calcularTotal()
    }

    fun getProductos(): LiveData<List<ModeloProducto>> {

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val productReference = firebaseDatabase.getReference("Productos")

        productReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productos = mutableListOf<ModeloProducto>()

                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(ModeloProducto::class.java)
                    productos.add(producto!!)
                }

                productosLiveData.value = productos
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductViewModel", "Error al cargar productos", error.toException())
            }
        })

        return productosLiveData
    }

    fun subirDatos(context: Context, modeloFactura:ModeloFactura){

        UtilidadesBaseDatos.guardarTransaccionesBd("venta",context, AgregarProductoFactura.productosSeleccionadosAgregar)
        val transaccionesPendientes =
            UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos(context)
        FirebaseProductos.transaccionesCambiarCantidad(context, transaccionesPendientes)

        val listaProductosFacturados = arrayListOf<ModeloProductoFacturado>()
        AgregarProductoFactura.productosSeleccionadosAgregar.forEach{ (producto, cantidadSeleccionada)->
            //calculamos el precio descuento para tener la referencia para los reportes
            if (cantidadSeleccionada!=0){

                val porcentajeDescuento = modeloFactura!!.descuento.toDouble() / 100
                var precioDescuento:Double=producto.p_diamante.toDouble()
                precioDescuento *= (1 - porcentajeDescuento)
                precioDescuento += modeloFactura!!.envio.toDouble()

                val productoFacturado = ModeloProductoFacturado(
                    id_producto_pedido = UUID.randomUUID().toString(),
                    id_producto = producto.id,
                    id_pedido = modeloFactura!!.id_pedido,
                    id_vendedor = "idVendedor",
                    vendedor = "Nombre vendedor",
                    producto = producto.nombre,
                    cantidad = cantidadSeleccionada.toString(),
                    costo = producto.p_compra,
                    venta = producto.p_diamante,
                    precioDescuentos = precioDescuento.toString().formatoMonenda()!!,
                    fecha = modeloFactura!!.fecha,
                    hora=modeloFactura!!.hora,
                    imagenUrl=producto.url
                )
                listaProductosFacturados.add(productoFacturado)
            }
        }

        FirebaseProductoFacturadosOComprados.guardarProductoFacturado("ProductosFacturados",listaProductosFacturados)
    }
}