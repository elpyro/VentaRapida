package com.example.ventarapida.ui.usuarios

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ventarapida.Login
import com.example.ventarapida.databinding.ActivityCrearNuevaEmpresaBinding
import com.example.ventarapida.procesos.FirebaseDatosEmpresa.guardarDatosEmpresa
import com.example.ventarapida.procesos.FirebaseUsuarios
import com.example.ventarapida.procesos.Utilidades.obtenerFechaActual
import java.util.UUID

class CrearNuevaEmpresa : AppCompatActivity() {
    private lateinit var binding: ActivityCrearNuevaEmpresaBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrearNuevaEmpresaBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val correo = intent.getStringExtra("correo")
        val nombre = intent.getStringExtra("nombre")

        binding?.editTextUsuario?.setText(nombre)
        binding?.hintCorreo?.text=correo

        listeners()
    }

    private fun listeners() {
      binding?.buttonRegister?.setOnClickListener {
          if(binding?.editTextEmpresa?.text.toString()==""){
              binding.editTextEmpresa.setError("Obligatorio")
              return@setOnClickListener
          }
          if(binding?.editTextUsuario?.text.toString()==""){
              binding.editTextUsuario.setError("Obligatorio")
              return@setOnClickListener
          }

          val idEmpresa=UUID.randomUUID().toString()
          val updates = hashMapOf(
              "id" to idEmpresa,
              "nombre" to binding?.editTextEmpresa?.text.toString(),
              "premiun" to "true",
              "documento" to "",
              "pagina" to "",
              "telefono1" to "",
              "telefono2" to "",
              "direccion" to "",
              "garantia" to "",
              "correo" to "",
              "url" to "",
              "ultimo_pago" to obtenerFechaActual()
              )

          guardarDatosEmpresa(updates)

          val updatesUsuario = hashMapOf<String, Any>(
              "id" to  UUID.randomUUID().toString(),
              "nombre" to  binding?.editTextUsuario?.text.toString(),
              "correo" to  binding?.hintCorreo?.text.toString().toLowerCase(),
              "idEmpresa" to  idEmpresa,
              "empresa" to   binding?.editTextEmpresa?.text.toString(),
              "perfil" to "Administrador"
          )


          FirebaseUsuarios.guardarUsuario(updatesUsuario)

          finish()

          val intent = Intent(this, Login::class.java)
          startActivity(intent)

      }
    }


}