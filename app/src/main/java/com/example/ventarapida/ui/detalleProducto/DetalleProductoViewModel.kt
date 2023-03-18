package com.example.ventarapida.ui.detalleProducto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.ui.data.ModeloProducto
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.HashMap


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
        val registroRef = database2.getReference("Productos").child(id)
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
        val storageRef = Firebase.storage.reference.child(idProducto+".jpg")

        // Obtener la URI del archivo temporal
        val fileUri = guardarImagenEnDispositivo(context ,bitmap)

// Subir la imagen a Firebase Storage
        if (fileUri != null) {
            val uploadTask = storageRef.putFile(fileUri)
            uploadTask.addOnSuccessListener {
                // Obtener la URL de descarga de la imagen subida
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Aquí puedes usar la URL para mostrar la imagen en tu app, o para guardarla en tu base de datos
                    val url = uri.toString()

                    val updates = hashMapOf<String, Any>(
                        "url" to url.trim(),
                    )

                    val database = FirebaseDatabase.getInstance()
                    val registroRef = database.getReference("Productos").child(idProducto!!)
                    registroRef.updateChildren(updates)

                }.addOnFailureListener {
                    mensajeToast.value = "Error al obtener la URL de descarga de la imagen subida."
                }
            }.addOnFailureListener {
                mensajeToast.value="Error al subir la imagen."
            }
        }
    }

    private fun guardarImagenEnDispositivo(context: Context,bitmap: Bitmap): Uri? {
        // Crear un archivo temporal en el almacenamiento interno
        val file = File.createTempFile(
            "tempImagen",
            ".jpg",
            context.cacheDir
        )

        // Convertir el bitmap a un archivo jpeg
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
        val byteArray = stream.toByteArray()

        // Escribir el archivo jpeg en el archivo temporal
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(byteArray)
        fileOutputStream.flush()
        fileOutputStream.close()

        // Obtener la URI del archivo temporal
        return Uri.fromFile(file)
    }


}
