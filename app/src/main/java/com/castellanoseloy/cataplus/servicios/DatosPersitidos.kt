package com.castellanoseloy.cataplus.servicios

import android.app.ProgressDialog
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.castellanoseloy.cataplus.databinding.ActivityMainBinding
import com.castellanoseloy.cataplus.datos.ModeloDatosEmpresa
import com.castellanoseloy.cataplus.datos.ModeloProducto
import com.castellanoseloy.cataplus.datos.ModeloUsuario
import com.google.android.gms.ads.interstitial.InterstitialAd

class DatosPersitidos : Service() {

    // Datos que deben persistir
    companion object {
        const val JOB_ID = 1200 // Cambia este número por uno que no esté siendo utilizado en tu app
        const val JOB_IDGUARDARFACTURA= 1300
        var interstitial: InterstitialAd? = null
        var ventaProductosSeleccionados = mutableMapOf<ModeloProducto, Int>()
        var compraProductosSeleccionados = mutableMapOf<ModeloProducto, Int>()

        var verPublicidad: Boolean = false

        //Elementos de las preferencias para usarlos en la aplicacion
        var tono = true
        var mostrarAgotadosCatalogo= true
        var datosEmpresa: ModeloDatosEmpresa = ModeloDatosEmpresa()
        var datosUsuario: ModeloUsuario = ModeloUsuario()
        var planVencido:Boolean? =false

        lateinit var logotipo: ImageView
        lateinit var editText_nombreEmpresa: TextView

        lateinit var preferencia_informacion_superior:String
        lateinit var preferencia_informacion_inferior:String
        lateinit var edit_text_preference_codigo_area:String
        var progressDialog: ProgressDialog? = null
        lateinit var binding: ActivityMainBinding

    }

    override fun onBind(intent: Intent?): IBinder? {
        // No necesitamos un enlace para este servicio
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // Inicializar el servicio
        Log.d("Datos persistentes", "Servicio creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Lógica principal del servicio
        Log.d("Datos persistentes", "Servicio iniciado")

        // Acceder o modificar datosPersistidos aquí

        // Puedes realizar operaciones en segundo plano aquí

        // Devuelve START_STICKY para que el servicio se reinicie si se cierra inesperadamente
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Realizar tareas de limpieza o liberación de recursos
        Log.d("MiServicio", "Servicio destruido")
    }
}
