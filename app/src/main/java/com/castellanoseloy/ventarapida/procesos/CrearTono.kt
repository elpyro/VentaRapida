package com.castellanoseloy.ventarapida.procesos

import android.content.Context
import android.media.MediaPlayer
import com.castellanoseloy.ventarapida.MainActivity
import com.castellanoseloy.ventarapida.R

class CrearTono {
    fun crearTono(context: Context) {
        if(MainActivity.tono){
        val mediaPlayer = MediaPlayer.create(context, R.raw.coin)
        mediaPlayer.start()
        }
    }
}