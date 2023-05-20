package com.example.ventarapida.compras

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.procesos.CrearTono
import com.example.ventarapida.procesos.Preferencias
import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CompraViewModel : ViewModel() {

    lateinit var context: Context // propiedad para almacenar el contexto
    val productosLiveData = MutableLiveData<List<ModeloProducto>>()



    val totalSeleccionLiveData= MutableLiveData<String>()

    var totalCarritoLiveData= MutableLiveData<String>()
    fun restarProductoSeleccionado( producto: ModeloProducto){
        val id_producto = producto.id
        val productoEncontrado = MainActivity.compraProductosSeleccionados.keys.find { it.id == id_producto }
        if (productoEncontrado != null) {
            val nuevaCantidad = MainActivity.compraProductosSeleccionados[productoEncontrado]!! - 1
            MainActivity.compraProductosSeleccionados.remove(productoEncontrado)
            if (nuevaCantidad > 0) {
                MainActivity.compraProductosSeleccionados[producto] = nuevaCantidad
            }
        }
        calcularTotal()
    }
    fun agregarProductoSeleccionado(producto: ModeloProducto) {


        val id_producto=producto.id
        val productoEncontrado = MainActivity.compraProductosSeleccionados.keys.find { it.id == id_producto }
        if (productoEncontrado != null) {

            val nuevaCantidad = MainActivity.compraProductosSeleccionados[productoEncontrado]!! + 1
            MainActivity.compraProductosSeleccionados.remove(productoEncontrado)
            if (nuevaCantidad > 0) {
                MainActivity.compraProductosSeleccionados[producto] = nuevaCantidad
            }

        }else{
            MainActivity.compraProductosSeleccionados[producto] = 1
        }

        val crearTono= CrearTono()
        crearTono.crearTono(context)
        calcularTotal()

    }

    fun calcularTotal(){

        var total = 0.0

        for ((producto, cantidad) in MainActivity.compraProductosSeleccionados) {
            total += producto.p_compra.toDouble() * cantidad.toDouble()
        }

        totalCarritoLiveData.value =  total.toString().formatoMonenda()

        totalSeleccionLiveData.value= MainActivity.compraProductosSeleccionados.size.toString()

        val preferencias= Preferencias()
        preferencias.guardarPreferenciaListaSeleccionada(context,
            MainActivity.compraProductosSeleccionados,"compra_seleccionada"
        )

    }


    fun actualizarCantidadProducto(producto: ModeloProducto, nuevaCantidad: Int) {
        val id_producto=producto.id
        val productoEncontrado = MainActivity.compraProductosSeleccionados.keys.find { it.id == id_producto }
        if (productoEncontrado != null) {
            if (nuevaCantidad > 0) {
                MainActivity.compraProductosSeleccionados[productoEncontrado] = nuevaCantidad
            } else {
                MainActivity.compraProductosSeleccionados.remove(productoEncontrado)
            }

        }else{
            MainActivity.compraProductosSeleccionados[producto] = nuevaCantidad
        }

        val crearTono= CrearTono()
        crearTono.crearTono(context)
        calcularTotal()
    }

    fun eliminarCarrito(){
        MainActivity.compraProductosSeleccionados.clear()
        calcularTotal()
    }

    fun getProductos(): LiveData<List<ModeloProducto>> {

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val productReference = firebaseDatabase.getReference(MainActivity.datosEmpresa.id).child("Productos")

        productReference.addValueEventListener(object : ValueEventListener {
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


}