package com.castellanoseloy.cataplus.procesos


import java.util.*

class Suscripcion {

    fun calcularFechaFinSuscripcion(): Date {

        val calendar = Calendar.getInstance()

        // Obtener la fecha actual como fecha de inicio
        val fechaInicioDate = calendar.time

        calendar.time = fechaInicioDate
        calendar.add(Calendar.MONTH, 1) // Agregar un mes a la fecha de inicio

        return calendar.time
    }



    fun verificarFinSuscripcion(fechaFin: Date): Boolean {
        val fechaActual = Date()
        return fechaActual.after(fechaFin) // Verificar si la fecha actual es posterior a la fecha de finalización
    }
}