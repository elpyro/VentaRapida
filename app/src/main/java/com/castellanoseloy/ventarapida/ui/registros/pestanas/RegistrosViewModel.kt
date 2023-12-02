package com.castellanoseloy.ventarapida.ui.registros.pestanas

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.datos.ModeloFactura
import com.castellanoseloy.ventarapida.procesos.FirebaseFacturaOCompra
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat

class RegistrosViewModel : ViewModel() {

    private lateinit var tablaRef: DatabaseReference
    private val _facturasLiveData = MutableLiveData<MutableList<ModeloFactura>>()
    val facturasLiveData: LiveData<MutableList<ModeloFactura>> get() = _facturasLiveData

    private var escuchador: ValueEventListener? = null

    fun iniciarEscucha(tablaReferencia: String) {
        val database = FirebaseDatabase.getInstance()
        tablaRef = database.getReference(DatosPersitidos.datosEmpresa.id).child(tablaReferencia)

        if(escuchador==null) {
            escuchador = tablaRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val facturas = mutableListOf<ModeloFactura>()
                    Log.d("Registro ventas", "Se han escuchado cambios ")
                    for (facturaSnapshot in snapshot.children) {
                        val factura = facturaSnapshot.getValue(ModeloFactura::class.java)
                        factura?.let {
                            if (!factura.fecha.isNullOrEmpty()) {
                                if (Utilidades.verificarPermisosAdministrador() || factura.id_vendedor == DatosPersitidos.datosUsuario.id) {
                                    facturas.add(it)
                                }
                            }
                        }
                    }
                    val formatoFecha = SimpleDateFormat("dd/MM/yyyy")

                    var sortedFacturas = facturas.sortedWith(
                        compareByDescending<ModeloFactura> { formatoFecha.parse(it.fecha) }
                            .thenByDescending { it.hora }
                    )


                    val mutableFacturas = mutableListOf<ModeloFactura>()
                    mutableFacturas.addAll(sortedFacturas)

                    _facturasLiveData.value = mutableFacturas
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("MiApp", "Error al buscar facturas: ${error.message}")
                }
            })
        }
    }

    fun detenerEscucha() {
        escuchador?.let {
            tablaRef.removeEventListener(it)
            escuchador = null
        }
    }
}
