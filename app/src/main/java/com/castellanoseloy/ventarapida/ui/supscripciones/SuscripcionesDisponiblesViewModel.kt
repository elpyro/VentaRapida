package com.castellanoseloy.ventarapida.ui.supscripciones

import androidx.lifecycle.ViewModel
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.procesos.Utilidades
import java.util.Calendar
import java.util.Date

class SuscripcionesDisponiblesViewModel : ViewModel() {
    fun obtenerFechaVencimientoPlan(): Date {

        var proximoPago= Utilidades.convertirCadenaAFecha(DatosPersitidos.datosEmpresa.proximo_pago)

        val calendar = Calendar.getInstance()
        val fechaActual = calendar.time
        if(proximoPago==null)proximoPago=fechaActual
        if(fechaActual >= proximoPago){
            calendar.time = fechaActual
        }else{
            calendar.time = proximoPago!!
        }

        calendar.add(Calendar.MONTH, 1) // Agregar un mes a la fecha de inicio

        return calendar.time
    }

    fun verificarPlan(nuevoPlan: String, planActual: String, diasRestantes: Pair<String, Long>): Boolean {

        var planAceptado=true
        if(nuevoPlan != planActual){
            if(diasRestantes.second > 1.0){//no se pueden hacer cambio de plan cuando quedan mas de 1 dias disponibles
                planAceptado=false
            }
        }

        return planAceptado
    }


}