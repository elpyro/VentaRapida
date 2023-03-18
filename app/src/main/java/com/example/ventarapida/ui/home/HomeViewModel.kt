package com.example.ventarapida.ui.home

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.ui.data.ModeloProducto
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel() {


    val productosLiveData = MutableLiveData<List<ModeloProducto>>()

    val productosSeleccionados = mutableMapOf<ModeloProducto, Int>()

    val totalSeleccionLiveData=MutableLiveData<String>()

    fun agregarProductoSeleccionado(producto: ModeloProducto) {
        if (productosSeleccionados.containsKey(producto)) {
            productosSeleccionados[producto] = productosSeleccionados[producto]!! + 1
        } else {
            productosSeleccionados[producto] = 1
        }
        totalSeleccionLiveData.value=productosSeleccionados.size.toString()
    }
    fun getProductos(): LiveData<List<ModeloProducto>> {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val productReference = firebaseDatabase.getReference("Productos")

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