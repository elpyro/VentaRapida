package com.example.ventarapida.ui.usuarios


import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.ventarapida.MainActivity
import com.firebase.ui.auth.AuthUI
import com.example.ventarapida.databinding.FragmentDetalleUsuarioBinding
import com.example.ventarapida.Login
import com.google.android.gms.tasks.Task



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

                AuthUI.getInstance().signOut(requireContext())
                    .addOnCompleteListener { task: Task<Void?>? ->
                        Toast.makeText(context, "Sesion Cerrada", Toast.LENGTH_LONG).show()
                        val intent = Intent(requireActivity(), Login::class.java)
                        startActivity(intent)
                        requireActivity().finish()

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