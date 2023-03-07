package com.example.ventarapida.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.ui.data.ModeloProducto
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val productReference = firebaseDatabase.getReference("Productos")

    fun getProductos(): LiveData<List<ModeloProducto>> {
        val productosLiveData = MutableLiveData<List<ModeloProducto>>()

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

//
//    private val _text = MutableLiveData<String>().apply {
//        value = "This is home Fragment"
//    }
//    val text: LiveData<String> = _text
    }
}