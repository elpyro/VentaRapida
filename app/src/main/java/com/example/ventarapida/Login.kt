package com.example.ventarapida

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.example.ventarapida.databinding.ActivityLoginBinding
import com.example.ventarapida.datos.ModeloDatosEmpresa
import com.example.ventarapida.datos.ModeloUsuario
import com.example.ventarapida.procesos.FirebaseDatosEmpresa
import com.example.ventarapida.procesos.FirebaseUsuarios
import com.example.ventarapida.procesos.Utilidades.esperarUnSegundo
import com.example.ventarapida.ui.usuarios.CrearNuevaEmpresa
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
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

    private var idGoogle: String? =null
    private var progressDialog: ProgressDialog? = null
    private lateinit var binding: ActivityLoginBinding
    val RC_SIGN_IN = 123 //inicio sesion gmail

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var authStateListener: FirebaseAuth.AuthStateListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //COLOCAR LA BARRA SUPERIOR TRANSAPENTE
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        listeners()
        autenticacionGoogle()

    }

    private fun autenticacionGoogle() {

        firebaseAuth = FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // El usuario ha iniciado sesión
                val nombreUsuario = user.displayName
                val correoUsuario = user.email
                idGoogle= user.uid
                // Realizar las acciones necesarias con el nombre y correo del usuario
                Log.d("FIREBASEGOOGLE", "el id google es ${idGoogle.toString()}")
                verificarUsuario(correoUsuario,nombreUsuario)
            } else {
                // El usuario no ha iniciado sesión o ha cerrado sesión
                // Iniciar el flujo de inicio de sesión
                val providers = arrayListOf(
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )

                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                    RC_SIGN_IN
                )
            }
        }
        // Registrar el listener
        firebaseAuth.addAuthStateListener(authStateListener)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {

            if (resultCode == RESULT_OK) {
                // Inicio de sesión exitoso
                val user = FirebaseAuth.getInstance().currentUser
                val nombreUsuario = user?.displayName
                val correoUsuario = user?.email

                verificarUsuario( correoUsuario,nombreUsuario)
            } else {
                Toast.makeText(this,"Error en inicio de sesion Google",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun verificarUsuario(correoUsuario: String?, nombreUsuario: String?) {
        showProgressDialog()

        MainActivity.datosUsuario = ModeloUsuario()
        FirebaseUsuarios.buscarUsuariosPorCorreo(correoUsuario!!)
            .addOnSuccessListener { usuario ->
                if (usuario.size > 0) {
                    usuarioRegistrado(usuario)
                } else {
                    //hacemos una segunda consulta para asegurar que los datos sean persistentes
                    esperarUnSegundo()
                    esperarUnSegundo()
                    FirebaseUsuarios.buscarUsuariosPorCorreo(correoUsuario!!)
                        .addOnSuccessListener { usuario ->
                            if (usuario.size > 0) {
                                usuarioRegistrado(usuario)
                            } else {
                                // USUARIO NO REGISTRADO
                                hideProgressDialog()
                                binding.LinearLayoutBienvenido.visibility= View.GONE
                                binding.LinearLayoutUsuarioNoRegistrado.visibility= View.VISIBLE
                                binding.textViewCorreo.text = correoUsuario
                                binding.textViewNombre.text = nombreUsuario
                            }
                        }
                }

            }
            .addOnFailureListener {
                hideProgressDialog()
                Toast.makeText(this, "${nombreUsuario}, Error iniciando", Toast.LENGTH_LONG).show()
            }


    }

    private fun listeners() {
        binding.buttonRegistrarUsuario.setOnClickListener {
            val intent = Intent(this, CrearNuevaEmpresa::class.java)
            intent.putExtra("idGoogle", idGoogle.toString())
            intent.putExtra("correo", binding.textViewCorreo.text.toString())
            intent.putExtra("nombre", binding.textViewNombre.text.toString())
            startActivity(intent)
        }
        binding.buttonCerrarSesion.setOnClickListener {
            AuthUI.getInstance().signOut(this)
                .addOnCompleteListener { task: Task<Void?>? ->
                    Toast.makeText(this, "Sesion Cerrada", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                }
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
        hideProgressDialog()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    private fun showProgressDialog() {
        try{
            progressDialog = ProgressDialog(this@Login)
            progressDialog?.setMessage("Verificando usuario Google...")
            progressDialog?.setCancelable(true)
            progressDialog?.show()
        }catch (e: Exception) {
        }

    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }


}