package com.castellanoseloy.cataplus.procesos

import android.content.Context
import android.media.MediaPlayer
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.R

class CrearTono {
    fun crearTono(context: Context) {
        if(DatosPersitidos.tono){
        val mediaPlayer = MediaPlayer.create(context, R.raw.coin)
        mediaPlayer.start()
        }
    }
}