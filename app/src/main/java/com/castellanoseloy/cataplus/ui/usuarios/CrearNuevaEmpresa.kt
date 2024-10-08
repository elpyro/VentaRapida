package com.castellanoseloy.cataplus.ui.usuarios

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.castellanoseloy.cataplus.Login
import com.castellanoseloy.cataplus.databinding.ActivityCrearNuevaEmpresaBinding
import com.castellanoseloy.cataplus.datos.ModeloConfiguracionUsuario
import com.castellanoseloy.cataplus.procesos.FirebaseDatosEmpresa.guardarDatosEmpresa
import com.castellanoseloy.cataplus.procesos.FirebaseUsuarios
import com.castellanoseloy.cataplus.procesos.Suscripcion
import com.castellanoseloy.cataplus.procesos.Utilidades.obtenerFechaActual
import java.util.Locale
import java.util.UUID

class CrearNuevaEmpresa : AppCompatActivity() {
    private var idGoogle: String? = null
    private lateinit var binding: ActivityCrearNuevaEmpresaBinding
    private var suscripcion= Suscripcion()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrearNuevaEmpresaBinding.inflate(layoutInflater)

        //COLOCAR LA BARRA SUPERIOR TRANSAPENTE
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        setContentView(binding.root)

        idGoogle = intent.getStringExtra("idGoogle")
        val correo = intent.getStringExtra("correo")
        val nombre = intent.getStringExtra("nombre")

        binding.editTextUsuario.setText(nombre)
        binding.hintCorreo.text =correo

        listeners()
    }

    private fun listeners() {
        binding.buttonRegister.setOnClickListener {
            if(binding.editTextEmpresa.text.toString()==""){
                binding.editTextEmpresa.error = "Obligatorio"
                return@setOnClickListener
            }
            if(binding.editTextUsuario.text.toString()==""){
                binding.editTextUsuario.error = "Obligatorio"
                return@setOnClickListener
            }

            val proximo_pago= suscripcion.calcularFechaFinSuscripcion()

            val idEmpresa=UUID.randomUUID().toString()
            val updates = hashMapOf(
                "id" to idEmpresa,
                "nombre" to binding.editTextEmpresa.text.toString(),
                "premiun" to "true",
                "documento" to "",
                "pagina" to "",
                "telefono1" to "",
                "telefono2" to "",
                "direccion" to "",
                "garantia" to "",
                "correo" to "",
                "url" to "",
                "ultimo_pago" to obtenerFechaActual(),
                "plan" to "Gratuito",
                "idDuenoCuenta" to idGoogle.toString(),
                "proximo_pago" to proximo_pago.toString(),
                "mostrarPreciosCompra" to "false"
            )

            guardarDatosEmpresa(updates)

            val updatesUsuario = hashMapOf<String, Any>(
                "id" to  idGoogle.toString(),
                "nombre" to  binding.editTextUsuario.text.toString(),
                "correo" to binding.hintCorreo.text.toString().lowercase(Locale.getDefault()),
                "idEmpresa" to  idEmpresa,
                "empresa" to   binding.editTextEmpresa.text.toString(),
                "configuracion" to ModeloConfiguracionUsuario(),
                "perfil" to "Administrador"
            )


            FirebaseUsuarios.guardarUsuario(updatesUsuario)

            finish()

            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

        }
    }


}