package com.example.ventarapida.ui.usuarios

import android.app.AlertDialog
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
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.ventarapida.MainActivity
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentRegistroUsuarioBinding
import com.example.ventarapida.datos.ModeloConfiguracionUsuario
import com.example.ventarapida.datos.ModeloUsuario
import com.example.ventarapida.procesos.FirebaseUsuarios.eliminarUsuarioPorId
import com.example.ventarapida.procesos.FirebaseUsuarios.guardarUsuario
import java.util.UUID


class RegistroUsuario : Fragment() {

    private var nombreUsuario: String? =null
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
                nombreUsuario= modeloUsuario.nombre
                cargarDatos(modeloUsuario)
                idUsuario= modeloUsuario.id
                binding?.TextviewTitulo?.text = modeloUsuario.perfil
                clienteNuevo=false
            }else{
                idUsuario= UUID.randomUUID().toString()
                binding?.TextviewTitulo?.setText("Crea un nuevo usuario")
            }

            listeners()
            return binding!!.root
        }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(!clienteNuevo) inflater.inflate(R.menu.menu_eliminar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_eliminar ->{
                eliminarCuenta()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun eliminarCuenta() {
       var usuarioPrincipal=verificarUsuarioPrincipal()
        if(usuarioPrincipal){
            Toast.makeText(requireContext(),"No se puede eliminar la cuenta del usuario principal",Toast.LENGTH_LONG).show()
        }else{
            // Crear un cuadro de diálogo con botones "Sí" y "No"
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle("Eliminar")
            alertDialogBuilder.setMessage("¿Estás seguro de que quieres eliminar la cuenta de $nombreUsuario?")
            alertDialogBuilder.setPositiveButton("Sí") { dialogInterface, _ ->
                eliminarUsuarioPorId(idUsuario).addOnCompleteListener{
                    Toast.makeText(requireContext(),"Usuario eliminado",Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
                dialogInterface.dismiss() // Cerrar el cuadro de diálogo
            }
            alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
                // Acciones a realizar cuando se hace clic en "No"
                dialogInterface.dismiss() // Cerrar el cuadro de diálogo
            }

            // Mostrar el cuadro de diálogo
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }


    private fun cargarDatos(modeloUsuario: ModeloUsuario) {
        var nombre=binding?.editTextUsuario
        var editTextCorreo=binding?.editTextCorreo
        var textViewCorreo=binding?.hintCorreo

        nombre?.setText(modeloUsuario.nombre)
        editTextCorreo?.setText(modeloUsuario.correo.replace("@gmail.com", ""))
        when (modeloUsuario.perfil) {
            "Administrador" -> {
                binding?.radioButtonAdministrador?.isChecked = true
                binding?.linearLayoutOpcionesVendedor?.isVisible=false
            }
            "Vendedor" -> {
                binding?.radioButtonVendedor?.isChecked = true
                binding?.linearLayoutOpcionesVendedor?.isVisible=true
            }
            "Inactivo" -> {
                binding?.radioButtonInactivo?.isChecked = true
                binding?.linearLayoutOpcionesAdministrador?.isVisible=false
                binding?.linearLayoutOpcionesVendedor?.isVisible=false
            }
        }
        if(modeloUsuario.configuracion!=null){
            binding?.switchPreciosCompra?.isChecked=modeloUsuario.configuracion.mostrarPreciosCompra
            binding?.switchReporteGanancia?.isChecked=modeloUsuario.configuracion.mostrarReporteGanancia
            binding?.switchEditarFacturas?.isChecked=modeloUsuario.configuracion.editarFacturas
            binding?.switchAgregarInformacion?.isChecked=modeloUsuario.configuracion.agregarInformacionAdicional
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

        binding?.radioButtonAdministrador?.setOnClickListener{
            val seleccion= binding?.radioButtonAdministrador?.isChecked
            if(seleccion!!){
                binding?.linearLayoutOpcionesVendedor?.isVisible=false
                binding?.linearLayoutOpcionesAdministrador?.isVisible=true
            }
        }
        binding?.radioButtonVendedor?.setOnClickListener{
            val seleccion= binding?.radioButtonVendedor?.isChecked
            if(seleccion!!){
                binding?.linearLayoutOpcionesVendedor?.isVisible=true
                binding?.linearLayoutOpcionesAdministrador?.isVisible=true
            }
        }
        binding?.radioButtonInactivo?.setOnClickListener{
            binding?.linearLayoutOpcionesAdministrador?.isVisible=false
            binding?.linearLayoutOpcionesVendedor?.isVisible=false
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

        var perfil= ""
        if(binding?.radioButtonVendedor?.isChecked==true) perfil="Vendedor"
        if(binding?.radioButtonAdministrador?.isChecked==true) perfil="Administrador"
        if(binding?.radioButtonInactivo?.isChecked==true) perfil="Inactivo"

        
        if(!perfil.equals("Administrador")){
            val usaurioPrincipal=verificarUsuarioPrincipal()
            if (usaurioPrincipal) {
                Toast.makeText(requireContext(),"No se puede degradar el usuario principal",Toast.LENGTH_LONG).show()
            return
            }
        }

        var correo= binding?.editTextCorreo?.text.toString() +"@gmail.com"

        var permisosUsuario=otorgarPermisos(perfil)
        val updates = hashMapOf<String, Any>(
            "id" to idUsuario,
            "nombre" to  binding?.editTextUsuario?.text.toString(),
            "correo" to  correo.toLowerCase(),
            "idEmpresa" to  MainActivity.datosEmpresa.id,
            "empresa" to  MainActivity.datosEmpresa.nombre,
            "perfil" to perfil,
            "configuracion" to permisosUsuario
        )

        guardarUsuario(updates)

        Toast.makeText(requireContext(),"Usuario Guardado",Toast.LENGTH_LONG).show()
        findNavController().popBackStack()

    }

    private fun otorgarPermisos(perfil: String): ModeloConfiguracionUsuario {
       var mostrarPercios=true
       var editarFacturar=true
       var mostrarReporte=true
       var agregerInformacion=true

       if(perfil.equals("Administrador")) mostrarPercios= binding?.switchPreciosCompra?.isChecked!!

        if(perfil.equals("Vendedor")){
             mostrarPercios= binding?.switchPreciosCompra?.isChecked!!
             editarFacturar= binding?.switchEditarFacturas?.isChecked!!
             mostrarReporte= binding?.switchReporteGanancia?.isChecked!!
             agregerInformacion= binding?.switchAgregarInformacion?.isChecked!!
        }

        if(perfil.equals("Inactivo")){
             mostrarPercios=false
             editarFacturar=false
             mostrarReporte=false
             agregerInformacion=false

        }

        val configuracionUsuario = ModeloConfiguracionUsuario(
            mostrarPreciosCompra = mostrarPercios,
            editarFacturas = editarFacturar,
            mostrarReporteGanancia = mostrarReporte,
            agregarInformacionAdicional = agregerInformacion
        )

        return configuracionUsuario

    }

    private fun verificarUsuarioPrincipal(): Boolean {
        if(MainActivity.datosEmpresa.idDuenoCuenta!=null){
            return idUsuario == MainActivity.datosEmpresa.idDuenoCuenta
        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            vista=view
        }

    }