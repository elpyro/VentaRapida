package com.castellanoseloy.cataplus.procesos

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.castellanoseloy.cataplus.MainActivity
import com.castellanoseloy.cataplus.datos.ModeloProducto
import com.castellanoseloy.cataplus.datos.ModeloTransaccionSumaRestaProducto
import com.castellanoseloy.cataplus.procesos.UtilidadesBaseDatos.eliminarColaSubida
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


object FirebaseProductos {

    private const val TABLA_REFERENCIA = "Productos"


    fun guardarProducto(updates: Map<String, Any?>): Task<Void> {
        val database = FirebaseDatabase.getInstance()
        val registroRef =
            database.getReference(DatosPersitidos.datosEmpresa.id).child(TABLA_REFERENCIA)
                .child(updates["id"] as String)
        registroRef.keepSynced(true)
        return registroRef.updateChildren(updates)
    }


    private val transaccionesEjecutadas = HashSet<String>()

    fun transaccionesCambiarCantidad(
        context: Context?,
        solicitudes: List<ModeloTransaccionSumaRestaProducto>
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val ocultarBoton = MainActivity()
            ocultarBoton.mostrarFabBottonTransacciones(context!!)
        }

        val database = FirebaseDatabase.getInstance()
        val productosRef =
            database.getReference(DatosPersitidos.datosEmpresa.id).child(TABLA_REFERENCIA)
        productosRef.keepSynced(true)

        solicitudes.forEach { solicitud ->
            val idTransaccion = solicitud.idTransaccion
            if (transaccionesEjecutadas.contains(idTransaccion)) {
                // La transacción ya se ha ejecutado, no es necesario procesarla nuevamente
                return@forEach
            }

            val idProducto = solicitud.idProducto
            val cantidad = solicitud.cantidad
            Log.d("Firebase", "Cantidad a cambiar: $cantidad")
            // Referencia al producto principal
            val productoRef = productosRef.child(idProducto)

            // Ejecutar la transacción para la cantidad principal del producto
            productoRef.child("cantidad").runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val cantidadActual = mutableData.getValue(String::class.java)?.toInt()
                        ?: return Transaction.success(mutableData)


                    Log.d("Firebase", "el producto guardado en  firebase: ${cantidad} del ${productoRef}")

                    // Restar la cantidad del producto principal
                    mutableData.value = (cantidadActual - cantidad.toInt()).toString()

                    return Transaction.success(mutableData)
                }

                override fun onComplete(
                    databaseError: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (databaseError != null) {
                        Log.e(
                            "Firebase",
                            "Error al actualizar la cantidad del producto: ${databaseError.message}"
                        )
                        Toast.makeText(
                            context,
                            "Error al actualizar la cantidad del producto",
                            Toast.LENGTH_SHORT
                        ).show()
                        eliminarColaSubida(context!!, idTransaccion) // eliminar registro con id
                    } else {
                        // Actualizar las variables solo si la actualización principal fue exitosa


                        if(solicitud.listaVariables.isNullOrEmpty()) {
                            actualizarQuickSell(idProducto)
                        }else{
                            actualizarVariables(solicitud, productoRef)
                        }

                        eliminarColaSubida(context!!, idTransaccion) // eliminar registro con id

                        GlobalScope.launch(Dispatchers.Main) {
                            val ocultarBoton = MainActivity()
                            ocultarBoton.mostrarFabBottonTransacciones(context!!)
                        }
                    }
                }
            })

            // Marcar la transacción como ejecutada
            transaccionesEjecutadas.add(idTransaccion)
        }
    }

    private fun actualizarVariables(
        solicitud: ModeloTransaccionSumaRestaProducto,
        productoRef: DatabaseReference
    ) {


        solicitud.listaVariables?.forEach { variable ->
            val listaVariablesRef = productoRef.child("listaVariables")

            listaVariablesRef.get().addOnSuccessListener { snapshot ->
                for (childSnapshot in snapshot.children) {
                    val id = childSnapshot.child("idVariable").getValue(String::class.java)
                    if (id == variable.idVariable) {
                        val variableCantidadRef = childSnapshot.ref.child("cantidad")

                        variableCantidadRef.runTransaction(object : Transaction.Handler {
                            private var cantidadVariableNueva: Int = 0

                            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                val cantidadVariableActual = mutableData.getValue(Int::class.java)
                                    ?: return Transaction.success(mutableData)

                                cantidadVariableNueva =(cantidadVariableActual - variable.cantidad )
                                // Restar la cantidad de la variable
                                Log.d("Firebase", "se recivio la cantidad en la variable: ${variable.nombreVariable}= ${variable.cantidad}")
                                Log.d("Firebase", "se fija la nueva cantidad en la variable:  $cantidadVariableNueva")

                                mutableData.value =cantidadVariableNueva


                                return Transaction.success(mutableData)
                            }

                            override fun onComplete(
                                databaseError: DatabaseError?,
                                committed: Boolean,
                                currentData: DataSnapshot?
                            ) {
                                if (databaseError != null) {
                                    Log.e(
                                        "Firebase",
                                        "Error al actualizar la cantidad de la variable ${variable.idVariable}: ${databaseError.message}"
                                    )
                                } else {
                                    Log.d(
                                        "Firebase",
                                        "Cantidad de la variable ${variable.idVariable} actualizada correctamente."
                                    )
                                    if(cantidadVariableNueva<-1) cantidadVariableNueva=0

                                        val actualizarQuickSellVariable = ActualizarQuickSell(variable.nombreVariable,cantidadVariableNueva)
                                        actualizarQuickSellVariable.updateInventory()


                                }
                            }
                        })
                        break
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e("Firebase", "Error al obtener lista de variables: ${exception.message}")
            }
        }

    }


    private fun actualizarQuickSell(idProducto: String) {
        // Versión no comercial de Eloy Castellanos lleva la siguiente línea
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val detalleProducto = buscarProductoPorId(idProducto).await()
                detalleProducto?.let {

                    // Actualizar la cantidad principal del producto
                    if(it.cantidad.toInt()<-1) it.cantidad="0"
                    val actualizarQuickSellProducto = ActualizarQuickSell(it.nombre, it.cantidad.toInt())
                    actualizarQuickSellProducto.updateInventory()


                }
                Log.d("QuickSell", "Quick sell fue actualizado correctamente")
            } catch (e: Exception) {
                Log.e("QuickSell", "Error al actualizar QuickSell: ${e.message}", e)
            }
        }
    }


    fun buscarProductoPorId(idProducto: String): Task<ModeloProducto?> {
        val database = FirebaseDatabase.getInstance()
        val tablaRef =
            database.getReference(DatosPersitidos.datosEmpresa.id).child(TABLA_REFERENCIA)
                .child(idProducto)

        val taskCompletionSource = TaskCompletionSource<ModeloProducto?>()

        tablaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val producto = snapshot.getValue(ModeloProducto::class.java)
                taskCompletionSource.setResult(producto)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MiApp", "Error al buscar producto por ID: ${error.message}")
                taskCompletionSource.setException(error.toException())
            }
        })

        return taskCompletionSource.task
    }


    fun buscarProductos(mayorCero: Boolean): Task<MutableList<ModeloProducto>> {
        val database = FirebaseDatabase.getInstance()
        val tablaRef =
            database.getReference(DatosPersitidos.datosEmpresa.id).child(TABLA_REFERENCIA)

        val productos = mutableListOf<ModeloProducto>()
        val taskCompletionSource = TaskCompletionSource<MutableList<ModeloProducto>>()
        tablaRef.keepSynced(true)
        tablaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // esperamos 2 segundo y volvemos a hacer la misma busqueda para estar seguro que si se obtienen los valores actualizados
                Utilidades.esperarUnSegundo()
                Utilidades.esperarUnSegundo()
                tablaRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (facturaSnapshot in snapshot.children) {
                            val factura = facturaSnapshot.getValue(ModeloProducto::class.java)
                            if (mayorCero) {

                                factura?.let {
                                    if (it.cantidad.toInt() > 0) { // Filtrar productos con cantidad mayor a 0
                                        if (!factura.editado.equals("Inventario Editado")) productos.add(
                                            factura
                                        )
                                    }
                                }
                            } else {
                                productos.add(factura!!)
                            }
                        }

                        taskCompletionSource.setResult(productos)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w("MiApp", "Error al buscar facturas: ${error.message}")
                        taskCompletionSource.setException(error.toException())
                    }
                })


            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MiApp", "Error al buscar facturas: ${error.message}")
                taskCompletionSource.setException(error.toException())
            }
        })

        return taskCompletionSource.task
    }


}