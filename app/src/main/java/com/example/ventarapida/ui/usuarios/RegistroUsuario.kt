package com.example.ventarapida.ui.usuarios

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.ventarapida.MainActivity
import com.example.ventarapida.databinding.FragmentRegistroUsuarioBinding
import com.example.ventarapida.procesos.FirebaseUsuarios.guardarUsuario
import java.util.UUID


class RegistroUsuario : Fragment() {

        private var binding: FragmentRegistroUsuarioBinding? = null
        private lateinit var vista: View

        private lateinit var viewModel: RegistroUsuarioViewModel

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            binding = FragmentRegistroUsuarioBinding.inflate(inflater, container, false)
            viewModel = ViewModelProvider(this)[RegistroUsuarioViewModel::class.java]

            listeners()
            return binding!!.root
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
            val id= UUID.randomUUID().toString()
            var perfil= ""
            if(binding?.radioButtonVendedor?.isChecked==true) perfil="Vendedor"
            if(binding?.radioButtonAdministrador?.isChecked==true) perfil="Administrador"
            if(binding?.radioButtonInactivo?.isChecked==true) perfil="Inactivo"

            var correo= binding?.editTextCorreo?.text.toString() +"@gmail.com"

            val updates = hashMapOf<String, Any>(
                "id" to id,
                "nombre" to  binding?.editTextUsuario?.text.toString(),
                "correo" to  correo,
                "idEmpresa" to  MainActivity.datosEmpresa.id,
                "perfil" to perfil
            )

            guardarUsuario(updates)


        }



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            vista=view
        }

    }