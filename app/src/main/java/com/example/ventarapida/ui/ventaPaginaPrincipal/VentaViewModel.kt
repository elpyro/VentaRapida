package com.example.ventarapida.ui.ventaPaginaPrincipal

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity
import com.example.ventarapida.MainActivity.Companion.ventaProductosSeleccionados
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.procesos.CrearTono

import com.example.ventarapida.procesos.Preferencias
import com.example.ventarapida.procesos.Utilidades
import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class VentaViewModel : ViewModel() {

    lateinit var context: Context // propiedad para almacenar el contexto
    val productosLiveData = MutableLiveData<List<ModeloProducto>>()

   

    val totalSeleccionLiveData=MutableLiveData<String>()

    var totalCarritoLiveData=MutableLiveData<String>()
    fun restarProductoSeleccionado( producto: ModeloProducto){
        val id_producto = producto.id
        val productoEncontrado = ventaProductosSeleccionados.keys.find { it.id == id_producto }
        if (productoEncontrado != null) {
            val nuevaCantidad = ventaProductosSeleccionados[productoEncontrado]!! - 1
            ventaProductosSeleccionados.remove(productoEncontrado)
            if (nuevaCantidad > 0) {
                ventaProductosSeleccionados[producto] = nuevaCantidad
            }
        }
        calcularTotal()
    }
    fun agregarProductoSeleccionado(producto: ModeloProducto) {


        val id_producto=producto.id
        val productoEncontrado = ventaProductosSeleccionados.keys.find { it.id == id_producto }
        if (productoEncontrado != null) {

            val nuevaCantidad = ventaProductosSeleccionados[productoEncontrado]!! + 1
            ventaProductosSeleccionados.remove(productoEncontrado)
            if (nuevaCantidad > 0) {
                ventaProductosSeleccionados[producto] = nuevaCantidad
            }

        }else{
            ventaProductosSeleccionados[producto] = 1
        }

        val crearTono= CrearTono()
        crearTono.crearTono(context)
        calcularTotal()

    }

    fun calcularTotal(){

        var total = 0.0

        for ((producto, cantidad) in ventaProductosSeleccionados) {
            total += producto.p_diamante.toDouble() * cantidad.toDouble()
        }

        totalCarritoLiveData.value =  total.toString().formatoMonenda()

        totalSeleccionLiveData.value=ventaProductosSeleccionados.size.toString()


        val preferencias= Preferencias()
        preferencias.guardarPreferenciaListaSeleccionada(context,
            ventaProductosSeleccionados,"venta_seleccionada")

    }


    fun actualizarCantidadProducto(producto: ModeloProducto, nuevaCantidad: Int) {
        val id_producto=producto.id
        val productoEncontrado = ventaProductosSeleccionados.keys.find { it.id == id_producto }
        if (productoEncontrado != null) {
            if (nuevaCantidad > 0) {
                ventaProductosSeleccionados[productoEncontrado] = nuevaCantidad
            } else {
                ventaProductosSeleccionados.remove(productoEncontrado)
            }

        }else{
            ventaProductosSeleccionados[producto] = nuevaCantidad
        }

        val crearTono= CrearTono()
        crearTono.crearTono(context)
        calcularTotal()
    }

        fun eliminarCarrito(){
            ventaProductosSeleccionados.clear()
            calcularTotal()
        }

    fun obtenerProductos(): LiveData<List<ModeloProducto>> {
        Log.d("Preferencia", "Mostrar productos agotados "+ MainActivity.mostrarAgotadosCatalogo.toString())
            val firebaseDatabase = FirebaseDatabase.getInstance()
            val productReference =
                firebaseDatabase.getReference(MainActivity.datosEmpresa.id).child("Productos")
            productReference.keepSynced(true)
            productReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productos = mutableListOf<ModeloProducto>()

                    for (productoSnapshot in snapshot.children) {
                        val producto = productoSnapshot.getValue(ModeloProducto::class.java)

                        if (MainActivity.mostrarAgotadosCatalogo==false) {
                            if (producto?.cantidad?.toInt()!! > 0) productos.add(producto!!)
                        }

                        if (MainActivity.mostrarAgotadosCatalogo) productos.add(producto!!)
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