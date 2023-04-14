package com.example.ventarapida.ui.procesos

import android.content.Context
import android.media.MediaPlayer
import com.example.ventarapida.R

class CrearTono {
    fun crearTono(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.coin)
        mediaPlayer.start()
    }
}