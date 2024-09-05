package com.castellanoseloy.cataplus

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.isVisible

import com.castellanoseloy.cataplus.datos.ModeloDatosEmpresa
import com.castellanoseloy.cataplus.datos.ModeloUsuario
import com.castellanoseloy.cataplus.procesos.FirebaseDatosEmpresa
import com.castellanoseloy.cataplus.procesos.FirebaseUsuarios
import com.castellanoseloy.cataplus.procesos.Utilidades.esperarUnSegundo
import com.castellanoseloy.cataplus.ui.usuarios.CrearNuevaEmpresa
import com.castellanoseloy.cataplus.databinding.ActivityLoginBinding
import com.castellanoseloy.cataplus.procesos.Preferencias
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {

    private var nuevoUsuario: Boolean =false
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

        //cargar las preferencias primero para evitar errores de carga
        val preferenciasServicios= Preferencias()
        preferenciasServicios.preferenciasConfiguracion(this)

        // Iniciar el servicio
        val intent = Intent(this, DatosPersitidos::class.java)
        startService(intent)
        
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
                mostrarAlertDialog()
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
                mostrarBotonIncioSesion()
            }
        }
    }

    private fun verificarUsuario(correoUsuario: String?, nombreUsuario: String?) {
        if(nuevoUsuario) showProgressDialog()
        ocultarBotonIncioSesion()


        DatosPersitidos.datosUsuario = ModeloUsuario()
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
                                ocultarBotonIncioSesion()
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
                mostrarBotonIncioSesion()
                Toast.makeText(this, "${nombreUsuario}, Error iniciando", Toast.LENGTH_LONG).show()
            }


    }

    private fun listeners() {
        binding.buttonInicioSesionGoogle.setOnClickListener{
            inicioGoogle()
        }
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


    var bandera=false
    private fun usuarioRegistrado(usuario: MutableList<ModeloUsuario>) {
        DatosPersitidos.datosUsuario = usuario[0]

        FirebaseDatosEmpresa.obtenerDatosEmpresa(
            usuario[0].idEmpresa,
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // verificarDatos nuevamente

                    if(bandera){
                        DatosPersitidos.datosEmpresa = snapshot.getValue(ModeloDatosEmpresa::class.java)!!
                        abrirPantallaPrincipal()
                    }else{
                        esperarUnSegundo()
                        esperarUnSegundo()
                        bandera=true
                        usuarioRegistrado(usuario)
                    }

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
            progressDialog?.setIcon(R.drawable.logo2_compra_rapidita)
            progressDialog?.setMessage("Verificando cuenta...")
            progressDialog?.setCancelable(true)
            progressDialog?.show()
        }catch (e: Exception) {
        }

    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    fun mostrarAlertDialog(){

        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_alert_inicio_sesion, null)

            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setIcon(R.drawable.logo2_compra_rapidita)
            alertDialogBuilder.setTitle("Bienvenido")
            alertDialogBuilder.setCancelable(false)
            alertDialogBuilder.setView(dialogView)
            alertDialogBuilder.setPositiveButton("Aceptar") { _, _ ->
                nuevoUsuario=true
                inicioGoogle()
            }
            alertDialogBuilder.setNegativeButton("No, Gracías") { _, _ ->
                mostrarBotonIncioSesion()
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }catch ( e: Exception){
        }

    }

    private fun mostrarBotonIncioSesion() {
        binding?.buttonInicioSesionGoogle?.isVisible =true
    }
    private fun ocultarBotonIncioSesion() {
        binding?.buttonInicioSesionGoogle?.isVisible =false
    }
    private fun inicioGoogle() {
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