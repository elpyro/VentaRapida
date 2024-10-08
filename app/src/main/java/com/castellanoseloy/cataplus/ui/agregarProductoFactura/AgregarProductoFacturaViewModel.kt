package com.castellanoseloy.cataplus.ui.agregarProductoFactura

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.datos.ModeloFactura
import com.castellanoseloy.cataplus.datos.ModeloProducto
import com.castellanoseloy.cataplus.datos.ModeloProductoFacturado
import com.castellanoseloy.cataplus.datos.ModeloTransaccionSumaRestaProducto
import com.castellanoseloy.cataplus.procesos.*
import com.castellanoseloy.cataplus.procesos.Utilidades.formatoMonenda
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class AgregarProductoFacturaViewModel : ViewModel() {

    lateinit var context: Context
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
        val productReference = firebaseDatabase.getReference(DatosPersitidos.datosEmpresa.id).child("Productos")
        productReference.keepSynced(true)
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

        //convertirmos los productos seleccionados en un ModeloProductoFacturado
        val listaProductosFacturados = mutableListOf<ModeloProductoFacturado>()
        val listaDescontarInventario = arrayListOf<ModeloTransaccionSumaRestaProducto>()
        var recaudo="Pendiente"
        if(DatosPersitidos.datosUsuario.perfil.equals("Administrador")) {
            recaudo = "No aplica"
        }
        AgregarProductoFactura.productosSeleccionadosAgregar.forEach{ (producto, cantidadSeleccionada)->
            //calculamos el precio descuento para tener la referencia para los reportes
            if (cantidadSeleccionada!=0){

                val porcentajeDescuento = modeloFactura.descuento.toDouble() / 100
                var precioDescuento:Double=producto.p_diamante.toDouble()
                precioDescuento *= (1 - porcentajeDescuento)

                val id_producto_pedido = UUID.randomUUID().toString()
                val productoFacturado = ModeloProductoFacturado(
                    id_producto_pedido = id_producto_pedido,
                    id_producto = producto.id,
                    id_pedido = modeloFactura.id_pedido,
                    id_vendedor = DatosPersitidos.datosUsuario.id,
                    vendedor = DatosPersitidos.datosUsuario.nombre,
                    producto = producto.nombre,
                    cantidad = cantidadSeleccionada.toString(),
                    costo = producto.p_compra,
                    venta = producto.p_diamante,
                    precioDescuentos = precioDescuento.toString(),
                    fecha = modeloFactura.fecha,
                    hora = modeloFactura.hora,
                    imagenUrl =producto.url,
                    fechaBusquedas =  modeloFactura.fechaBusquedas,
                    estadoRecaudo = recaudo,
                    listaVariables = producto.listaVariables
                )
                listaProductosFacturados.add(productoFacturado)

                val restarProducto = ModeloTransaccionSumaRestaProducto(
                    idTransaccion = id_producto_pedido,  //la transaccion tiene el mismo id
                    idProducto = producto.id,
                    cantidad = (cantidadSeleccionada).toString(),
                    subido ="false",
                    listaVariables = producto.listaVariables
                )
                listaDescontarInventario.add(restarProducto)
            }
        }

        FirebaseProductoFacturadosOComprados.guardarProductoFacturado("ProductosFacturados",listaProductosFacturados,"venta",context)

        FirebaseProductos.transaccionesCambiarCantidad(context, listaDescontarInventario)


        }




}