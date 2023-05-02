package com.example.ventarapida.procesos

import android.content.Context
import android.media.MediaPlayer
import com.example.ventarapida.MainActivity
import com.example.ventarapida.R

class CrearTono {
    fun crearTono(context: Context) {
        if(MainActivity.tono){
        val mediaPlayer = MediaPlayer.create(context, R.raw.coin)
        mediaPlayer.start()
        }
    }
}