package com.castellanoseloy.ventarapida.ui.supscripciones

import androidx.lifecycle.ViewModel
import com.castellanoseloy.ventarapida.MainActivity
import com.castellanoseloy.ventarapida.procesos.Utilidades
import java.util.Calendar
import java.util.Date

class SuscripcionesDisponiblesViewModel : ViewModel() {
    fun obtenerFechaVencimientoPlan(): Date {

        var proximoPago= Utilidades.convertirCadenaAFecha(MainActivity.datosEmpresa.proximo_pago)

        val calendar = Calendar.getInstance()
        val fechaActual = calendar.time
        if(proximoPago==null)proximoPago=fechaActual
        if(fechaActual >= proximoPago){
            calendar.time = fechaActual
        }else{
            calendar.time = proximoPago
        }

        calendar.add(Calendar.MONTH, 1) // Agregar un mes a la fecha de inicio

        return calendar.time
    }

    fun verificarPlan(nuevoPlan: String, planActual: String, diasRestantes: Pair<String, Long>): Boolean {

        var planAceptado=true
        if(nuevoPlan != planActual){
            if(diasRestantes.second > 3.0){//no se pueden hacer cambio de plan cuando quedan mas de 5 dias disponibles
                planAceptado=false
            }
        }

        return planAceptado
    }


}