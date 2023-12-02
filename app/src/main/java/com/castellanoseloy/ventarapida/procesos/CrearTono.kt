package com.castellanoseloy.ventarapida.procesos

import android.content.Context
import android.media.MediaPlayer
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.R

class CrearTono {
    fun crearTono(context: Context) {
        if(DatosPersitidos.tono){
        val mediaPlayer = MediaPlayer.create(context, R.raw.coin)
        mediaPlayer.start()
        }
    }
}