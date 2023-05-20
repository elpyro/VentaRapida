package com.example.ventarapida.ui.usuarios

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.ventarapida.MainActivity
import com.example.ventarapida.MainActivity.Companion.RC_SIGN_IN
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentDetalleUsuarioBinding

import androidx.appcompat.app.AppCompatActivity
import com.example.ventarapida.Login
import com.example.ventarapida.datos.ModeloUsuario
import com.example.ventarapida.procesos.FirebaseUsuarios

import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class DetalleSesion : Fragment() {

    private var binding: FragmentDetalleUsuarioBinding? = null
    private lateinit var vista: View

    private lateinit var viewModel: DetalleSesionViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetalleUsuarioBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[DetalleSesionViewModel::class.java]


        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view

        cargarDatosUsario()

        listeners()
    }

    private fun listeners() {

            binding?.buttonRegister?.setOnClickListener {

                MainActivity.auth.signOut()
                MainActivity.googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
                    Toast.makeText(requireContext(), "Sesion cerrada", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                    val intent = Intent(requireContext(), Login::class.java)
                    startActivity(intent)



                }

            }




    }

    fun iniciarSesionConGoogle(context: Context) {

        // Configurar las opciones de inicio de sesión de Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Construir el cliente de inicio de sesión de Google
        googleSignInClient = GoogleSignIn.getClient(context, gso)
        // Implementar el inicio de sesión de Google en respuesta a un evento de clic o botón

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
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
//            val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
//
//            autenticaconGmail.signInWithCredential(credential)
            MainActivity.datosUsuario = ModeloUsuario()

            FirebaseUsuarios.buscarUsuariosPorCorreo(email!!)
                .addOnSuccessListener { usuario ->
                    if(usuario.size>0){
                        MainActivity.datosUsuario =usuario[0]
                        Toast.makeText(requireContext(),"Bienvenido ${MainActivity.datosUsuario.nombre}",Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(requireContext(),"${displayName}, No registrado",Toast.LENGTH_LONG).show()

                    }
                    findNavController().popBackStack()

                }
                .addOnFailureListener {exception ->
                    exception.printStackTrace()
                    Toast.makeText(requireActivity(),"${displayName}, Error iniciando",Toast.LENGTH_LONG).show()
                }

        } catch (e: ApiException) {
            Toast.makeText(requireContext(), "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
        }
    }



    private fun cargarDatosUsario() {
        binding?.TextviewNombreUsuario?.text=MainActivity.datosUsuario.nombre
        binding?.textViewCorreo?.text=MainActivity.datosUsuario.correo
        binding?.textViewEmpresa?.text=MainActivity.datosUsuario.empresa
        binding?.textViewPerfil?.text=MainActivity.datosUsuario.perfil
    }



}