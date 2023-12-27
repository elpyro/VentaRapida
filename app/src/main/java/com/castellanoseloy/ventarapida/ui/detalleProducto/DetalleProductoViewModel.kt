@file:Suppress("DEPRECATION")

package com.castellanoseloy.ventarapida.ui.detalleProducto

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.JobIntentService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.procesos.GuardarImagenEnDispositivo
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.servicios.ServiciosSubirFoto
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.HashMap
import java.util.UUID


class DetalleProductoViewModel : ViewModel() {

    private lateinit var idPedido: String
    private lateinit var fechaActual: String
    private lateinit var horaActual: String

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
    private val productosRef = database.getReference(DatosPersitidos.datosEmpresa.id).child("Productos")

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
        val registroRef = database2.getReference(DatosPersitidos.datosEmpresa.id).child("Productos").child(id)
        listaProductos.removeIf { it.id == id }
        val task = registroRef.removeValue().addOnSuccessListener {
            mensajeToast.value="Producto eliminado"
        }

        return task

    }


    fun obtenerDatosPedido(): HashMap<String, Any> {

        horaActual = Utilidades.obtenerHoraActual()
        fechaActual = Utilidades.obtenerFechaActual()

        idPedido= UUID.randomUUID().toString()

        val total="0"
        val nombre=  "Edición de inventario"
        val totalconEtiqueta = total.replace("Total:", "Nuevo ").trim()
        val datosPedido = hashMapOf<String, Any>(
            "id_pedido" to idPedido,
            "nombre" to nombre,
            "telefono" to "",
            "documento" to "",
            "direccion" to "",
            "descuento" to "0",
            "envio" to "0",
            "fecha" to fechaActual,
            "hora" to horaActual,
            "id_vendedor" to DatosPersitidos.datosUsuario.id,
            "nombre_vendedor" to DatosPersitidos.datosUsuario.nombre,
            "total" to totalconEtiqueta,
            "fechaBusquedas" to Utilidades.obtenerFechaUnix()
        )
        return datosPedido
    }

    fun subirImagenFirebase(context:Context,     bitmap: Bitmap?) {

        val idProducto= detalleProducto.value?.get(0)?.id

        // Obtener la imagen del ImageView como Bitmap
        // Crear una referencia a la ubicación donde se subirá la imagen en Firebase Storage
        val storageRef = Firebase.storage.reference.child(idProducto + ".jpg")


        val guardarImagenEnDispositivo= GuardarImagenEnDispositivo()
        val fileUri = guardarImagenEnDispositivo.guardarImagenEnDispositivo(context , bitmap!!)

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
            DatosPersitidos.JOB_ID,
            intent
        )

    }

    fun productoEditado(producto: ModeloProducto, nuevaCantidad: Int): MutableList<ModeloProductoFacturado> {
        val productoFacturado = ModeloProductoFacturado(
            id_producto_pedido =UUID.randomUUID().toString(),
            id_producto = producto.id,
            id_pedido = idPedido,
            id_vendedor = DatosPersitidos.datosUsuario.id,
            vendedor = DatosPersitidos.datosUsuario.nombre,
            producto = producto.nombre,
            cantidad = nuevaCantidad.toString(),
            costo = producto.p_compra,
            venta = producto.p_compra,
            fecha = fechaActual,
            hora =horaActual,
            imagenUrl =producto.url,
            productoEditado="Inventario Editado",
            fechaBusquedas = Utilidades.obtenerFechaUnix()
        )
        val listaProductosFacturados = arrayListOf<ModeloProductoFacturado>()
        listaProductosFacturados.add(productoFacturado)
        return listaProductosFacturados
    }
}
