package com.example.ventarapida.ui.detalleProducto

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.core.app.JobIntentService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity


import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.procesos.GuardarImagenEnDispositivo
import com.example.ventarapida.ui.procesos.ServiciosSubirFoto

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


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
    private val productosRef = database.getReference(MainActivity.datosEmpresa.id).child("Productos")

    // LiveData que contiene los detalles de un producto
    val detalleProducto = MutableLiveData<List<ModeloProducto>>()

    // LiveData que contiene el ID del producto actual
    private val idProducto = MutableLiveData<String>()

    fun getProductos(): LiveData<List<ModeloProducto>> {
        val productosList = mutableListOf<ModeloProducto>()
        productosRef.keepSynced(true)
        // Agregamos un listener a la referencia de la base de datos
        productosRef.addListenerForSingleValueEvent(object : ValueEventListener {
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


    fun eliminarProducto(id:String ): Task<Void> {

        val database2 = FirebaseDatabase.getInstance()
        val registroRef = database2.getReference(MainActivity.datosEmpresa.id).child("Productos").child(id)
        listaProductos.removeIf { it.id == id }
        val task = registroRef.removeValue().addOnSuccessListener {
            mensajeToast.value="Producto eliminado"
        }

        return task

    }

    fun subirImagenFirebase(context:Context,imageViewFoto: ImageView?) {

        val idProducto= detalleProducto.value?.get(0)?.id

        // Obtener la imagen del ImageView como Bitmap
        val bitmap = (imageViewFoto?.drawable as BitmapDrawable).bitmap

        // Crear una referencia a la ubicación donde se subirá la imagen en Firebase Storage
        val storageRef = Firebase.storage.reference.child(idProducto + ".jpg")


        val guardarImagenEnDispositivo= GuardarImagenEnDispositivo()
        val fileUri = guardarImagenEnDispositivo.guardarImagenEnDispositivo(context ,bitmap)

        // Crear el Intent para iniciar el servicio
        val intent = Intent(context, ServiciosSubirFoto::class.java)
        intent.putExtra("fileUri", fileUri)
        intent.putExtra("storageRef", storageRef.toString())
        intent.putExtra("idProducto", idProducto)
        intent.putExtra("tablaReferencia", "Productos")

        // Iniciar el servicio en segundo plano utilizando JobIntentService
        JobIntentService.enqueueWork(
            context,
            ServiciosSubirFoto::class.java,
            MainActivity.JOB_ID,
            intent
        )

    }
}
