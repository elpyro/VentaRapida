package com.example.ventarapida.ui.usuarios

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.ventarapida.MainActivity
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentRegistroUsuarioBinding
import com.example.ventarapida.datos.ModeloUsuario
import com.example.ventarapida.procesos.FirebaseUsuarios.guardarUsuario
import java.util.UUID


class RegistroUsuario : Fragment() {

        private var binding: FragmentRegistroUsuarioBinding? = null
        private lateinit var vista: View

        private lateinit var    viewModel: RegistroUsuarioViewModel
        private var idUsuario=""
        private var clienteNuevo=true

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            binding = FragmentRegistroUsuarioBinding.inflate(inflater, container, false)
            viewModel = ViewModelProvider(this)[RegistroUsuarioViewModel::class.java]


            setHasOptionsMenu(true)

            val bundle = arguments
            val modeloUsuario = bundle?.getSerializable("modelo") as? ModeloUsuario

            if (modeloUsuario!=null){
                cargarDatos(modeloUsuario)
                idUsuario= modeloUsuario.id
                clienteNuevo=false
            }else{
                idUsuario= UUID.randomUUID().toString()
            }

            listeners()
            return binding!!.root
        }


    private fun cargarDatos(modeloUsuario: ModeloUsuario) {
        var nombre=binding?.editTextUsuario
        var editTextCorreo=binding?.editTextCorreo
        var textViewCorreo=binding?.hintCorreo

        nombre?.setText(modeloUsuario.nombre)
        editTextCorreo?.setText(modeloUsuario.correo.replace("@gmail.com", ""))
        when (modeloUsuario.perfil) {
            "Administrador" -> binding?.radioButtonAdministrador?.isChecked = true
            "Vendedor" -> binding?.radioButtonVendedor?.isChecked = true
            "Inactivo" -> binding?.radioButtonInactivo?.isChecked = true
        }
    }

    private fun listeners() {


        binding?.editTextCorreo?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se requiere implementación
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Verificar si el texto actual  contiene un "@gmail."
                val currentText = s.toString()
                val newText = currentText.replace("@gmail.com", "")
                if (currentText != newText) {
                    binding?.editTextCorreo?.setText(newText)
                    binding?.editTextCorreo?.setSelection(newText.length)
                }
            }
            override fun afterTextChanged(s: Editable?) {
                // No se requiere implementación
            }
        })

        binding?.buttonRegister?.setOnClickListener {
            registrarUsuario()

        }



    }

    private fun registrarUsuario() {
        if (binding?.editTextUsuario?.text.toString()==""){
            binding?.editTextUsuario?.error = "Obligatorio"
            return
        }
        if (binding?.editTextCorreo?.text.toString()==""){
            binding?.editTextCorreo?.error = "Obligatorio"
            return
        }
        val id= UUID.randomUUID().toString()
        var perfil= ""
        if(binding?.radioButtonVendedor?.isChecked==true) perfil="Vendedor"
        if(binding?.radioButtonAdministrador?.isChecked==true) perfil="Administrador"
        if(binding?.radioButtonInactivo?.isChecked==true) perfil="Inactivo"

        var correo= binding?.editTextCorreo?.text.toString() +"@gmail.com"

        val updates = hashMapOf<String, Any>(
            "id" to idUsuario,
            "nombre" to  binding?.editTextUsuario?.text.toString(),
            "correo" to  correo.toLowerCase(),
            "idEmpresa" to  MainActivity.datosEmpresa.id,
            "empresa" to  MainActivity.datosEmpresa.nombre,
            "perfil" to perfil
        )

        guardarUsuario(updates)

        Toast.makeText(requireContext(),"Usuario Guardado",Toast.LENGTH_LONG).show()
        findNavController().popBackStack()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            vista=view
        }

    }