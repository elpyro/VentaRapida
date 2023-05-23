package com.example.ventarapida

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.ventarapida.databinding.ActivityLoginBinding
import com.example.ventarapida.datos.ModeloDatosEmpresa
import com.example.ventarapida.datos.ModeloUsuario
import com.example.ventarapida.procesos.FirebaseDatosEmpresa
import com.example.ventarapida.procesos.FirebaseUsuarios
import com.example.ventarapida.ui.usuarios.CrearNuevaEmpresa
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        iniciarSesionConGoogle(this)
        listeners()
    }



    private fun listeners() {
        binding.buttonRegistrarUsuario.setOnClickListener {
            val intent = Intent(this, CrearNuevaEmpresa::class.java)
            intent.putExtra("correo", binding.textViewCorreo.text.toString())
            intent.putExtra("nombre", binding.textViewNombre.text.toString())
            startActivity(intent)
            finish()
        }
        binding.buttonCerrarSesion.setOnClickListener {
            MainActivity.auth.signOut()
            MainActivity.googleSignInClient.signOut().addOnCompleteListener(this) {
                Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show()

                iniciarSesionConGoogle(this)
            }
        }
    }


    fun iniciarSesionConGoogle(context: Context) {

        MainActivity.auth = FirebaseAuth.getInstance()
        // Configurar las opciones de inicio de sesión de Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Construir el cliente de inicio de sesión de Google
        MainActivity.googleSignInClient = GoogleSignIn.getClient(context, gso)
        // Implementar el inicio de sesión de Google en respuesta a un evento de clic o botón

        val signInIntent = MainActivity.googleSignInClient.signInIntent
        startActivityForResult(signInIntent, MainActivity.RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MainActivity.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Autenticación exitosa, puedes obtener información del usuario a través de la cuenta de Google
            val displayName = account?.displayName
            val email = account?.email

            MainActivity.datosUsuario = ModeloUsuario()
            FirebaseUsuarios.buscarUsuariosPorCorreo(email!!)
                .addOnSuccessListener { usuario ->
                    if (usuario.size > 0) {
                        usuarioRegistrado(usuario)
                    } else {
                        binding.LinearLayoutBienvenido.visibility= View.GONE
                        binding.LinearLayoutUsuarioNoRegistrado.visibility= View.VISIBLE
                        // USUARIO NO REGISTRADO
                        Toast.makeText(this, "${displayName}, No registrado", Toast.LENGTH_LONG).show()
                        binding.textViewCorreo.text = email
                        binding.textViewNombre.text = displayName
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(this, "${displayName}, Error iniciando", Toast.LENGTH_LONG).show()
                }

        } catch (e: ApiException) {
            Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
        }
    }

    private fun usuarioRegistrado(usuario: MutableList<ModeloUsuario>) {
        MainActivity.datosUsuario = usuario[0]

        FirebaseDatosEmpresa.obtenerDatosEmpresa(
            usuario[0].idEmpresa,
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Procesar los datos en el snapshot
                    MainActivity.datosEmpresa = snapshot.getValue(ModeloDatosEmpresa::class.java)!!

                    abrirPantallaPrincipal()
                    }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar el error
                }
            })

    }

    private fun abrirPantallaPrincipal() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        Toast.makeText(
            this,
            "Bienvenido ${MainActivity.datosUsuario.nombre}",
            Toast.LENGTH_LONG
        ).show()
    }
}