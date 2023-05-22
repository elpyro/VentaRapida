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

    private fun cargarDatosUsario() {
        binding?.TextviewNombreUsuario?.text=MainActivity.datosUsuario.nombre
        binding?.textViewCorreo?.text="Correo: "+MainActivity.datosUsuario.correo
        binding?.textViewEmpresa?.text="Empresa: "+MainActivity.datosEmpresa.nombre
        binding?.textViewPerfil?.text="Perfil: "+MainActivity.datosUsuario.perfil
    }



}