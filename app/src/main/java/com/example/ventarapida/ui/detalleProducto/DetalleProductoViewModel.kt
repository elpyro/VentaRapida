package com.example.ventarapida.ui.detalleProducto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.ui.data.ModeloProducto
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DetalleProductoViewModel : ViewModel() {
    // Lista de productos
    val listaProductos = mutableListOf<ModeloProducto>()

    // Posición actual del producto en la lista
    var posicionActual = 0

    fun actualizarListaProductos(nuevaLista: List<ModeloProducto>) {
        listaProductos.clear()
        listaProductos.addAll(nuevaLista)
    }
    fun actualizarPosiscion(posicionRecibida:Int){
        posicionActual=posicionRecibida
    }

    private val database = FirebaseDatabase.getInstance()
    private val productosRef = database.getReference("Productos")

    // LiveData que contiene los detalles de un producto
    val detalleProducto = MutableLiveData<List<ModeloProducto>>()

    // LiveData que contiene el ID del producto actual
    private val idProducto = MutableLiveData<String>()

    fun getProductos(): LiveData<List<ModeloProducto>> {
        val productosList = mutableListOf<ModeloProducto>()

        // Agregamos un listener a la referencia de la base de datos
        productosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Limpiamos la lista de productos antes de llenarla de nuevo
                productosList.clear()

                // Recorremos los hijos de la referencia, que son los productos
                for (productoSnapshot in dataSnapshot.children) {
                    // Convertimos el snapshot en un objeto ModeloProducto
                    val producto = productoSnapshot.getValue(ModeloProducto::class.java)
                    // Si el ID del producto es igual al ID actual, lo agregamos a la lista
                    if (producto?.id == idProducto.value) {
                        productosList.add(producto!!)
                    }
                }

                // Actualizamos el LiveData con la nueva lista de productos
                detalleProducto.value = productosList
            }

            override fun onCancelled(error: DatabaseError) {
                mensajeToast.value="Error cargando producto"
            }
        })

        return detalleProducto
    }

    // Actualiza el ID del producto actual y recupera los detalles del producto
    fun setIdProducto(idProducto: String) {
        this.idProducto.value = idProducto
        getProductos()
    }



    val mensajeToast =MutableLiveData<String>()

    fun guardarProducto(updates: HashMap<String, Any>) {

        val id = updates["id"] as String?
        val database2 = FirebaseDatabase.getInstance()
        val registroRef = database2.getReference("Productos").child(id!!)
        registroRef.updateChildren(updates)

        registroRef.updateChildren(updates)
            .addOnSuccessListener {
                mensajeToast.value="Producto actualizado"
            }
            .addOnFailureListener {
                mensajeToast.value="Error en actualizar datos"
            }
    }

    fun eliminarProducto(id:String ): Task<Void> {

        val database2 = FirebaseDatabase.getInstance()
        val registroRef = database2.getReference("Productos").child(id!!)
        listaProductos.removeIf { it.id == id!! }
        val task = registroRef.removeValue().addOnSuccessListener {
            mensajeToast.value="Producto eliminado"
        }

        return task

    }


}
