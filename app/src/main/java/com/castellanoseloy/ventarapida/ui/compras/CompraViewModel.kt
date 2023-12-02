package com.castellanoseloy.ventarapida.ui.compras

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.procesos.CrearTono
import com.castellanoseloy.ventarapida.procesos.Preferencias
import com.castellanoseloy.ventarapida.procesos.Utilidades.formatoMonenda
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CompraViewModel : ViewModel() {

    private lateinit var escuchadorProductos: ValueEventListener
    private lateinit var productReference: DatabaseReference
    lateinit var context: Context // propiedad para almacenar el contexto
    val productosLiveData = MutableLiveData<List<ModeloProducto>>()



    val totalSeleccionLiveData= MutableLiveData<String>()

    var totalCarritoLiveData= MutableLiveData<String>()
    fun restarProductoSeleccionado( producto: ModeloProducto){
        val id_producto = producto.id
        val productoEncontrado = DatosPersitidos.compraProductosSeleccionados.keys.find { it.id == id_producto }
        if (productoEncontrado != null) {
            val nuevaCantidad = DatosPersitidos.compraProductosSeleccionados[productoEncontrado]!! - 1
            DatosPersitidos.compraProductosSeleccionados.remove(productoEncontrado)
            if (nuevaCantidad > 0) {
                DatosPersitidos.compraProductosSeleccionados[producto] = nuevaCantidad
            }
        }
        calcularTotal()
    }
    fun agregarProductoSeleccionado(producto: ModeloProducto) {


        val id_producto=producto.id
        val productoEncontrado = DatosPersitidos.compraProductosSeleccionados.keys.find { it.id == id_producto }
        if (productoEncontrado != null) {

            val nuevaCantidad = DatosPersitidos.compraProductosSeleccionados[productoEncontrado]!! + 1
            DatosPersitidos.compraProductosSeleccionados.remove(productoEncontrado)
            if (nuevaCantidad > 0) {
                DatosPersitidos.compraProductosSeleccionados[producto] = nuevaCantidad
            }

        }else{
            DatosPersitidos.compraProductosSeleccionados[producto] = 1
        }

        val crearTono= CrearTono()
        crearTono.crearTono(context)
        calcularTotal()

    }

    fun calcularTotal(){

        var total = 0.0

        for ((producto, cantidad) in DatosPersitidos.compraProductosSeleccionados) {
            total += producto.p_compra.toDouble() * cantidad.toDouble()
        }

        totalCarritoLiveData.value =  total.toString().formatoMonenda()

        totalSeleccionLiveData.value= DatosPersitidos.compraProductosSeleccionados.size.toString()

        val preferencias= Preferencias()
        preferencias.guardarPreferenciaListaSeleccionada(context,
            DatosPersitidos.compraProductosSeleccionados,"compra_seleccionada"
        )

    }


    fun actualizarCantidadProducto(producto: ModeloProducto, nuevaCantidad: Int) {
        val id_producto=producto.id
        val productoEncontrado = DatosPersitidos.compraProductosSeleccionados.keys.find { it.id == id_producto }
        if (productoEncontrado != null) {
            if (nuevaCantidad > 0) {
                DatosPersitidos.compraProductosSeleccionados[productoEncontrado] = nuevaCantidad
            } else {
                DatosPersitidos.compraProductosSeleccionados.remove(productoEncontrado)
            }

        }else{
            DatosPersitidos.compraProductosSeleccionados[producto] = nuevaCantidad
        }

        val crearTono= CrearTono()
        crearTono.crearTono(context)
        calcularTotal()
    }

    fun eliminarCarrito(){
        DatosPersitidos.compraProductosSeleccionados.clear()
        calcularTotal()
    }

    fun getProductos(): LiveData<List<ModeloProducto>> {
        Log.d("Escuchadores", "Se ha llamado el escuchador de PAGINA SURTIDO")
        val firebaseDatabase = FirebaseDatabase.getInstance()
        productReference = firebaseDatabase.getReference(DatosPersitidos.datosEmpresa.id).child("Productos")

        escuchadorProductos= productReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productos = mutableListOf<ModeloProducto>()

                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(ModeloProducto::class.java)
                    productos.add(producto!!)
                }
                Log.d("Escuchadores", "Ocurrio un envento en la pagina de surtido")
                productosLiveData.value = productos
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductViewModel", "Error al cargar productos", error.toException())
            }
        })

        return productosLiveData
    }
    fun detenerEscuchadores(){
        if (::productReference.isInitialized && ::productReference.isInitialized) {
            productReference.removeEventListener(escuchadorProductos)
        }
    }

}