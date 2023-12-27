package com.castellanoseloy.ventarapida.ui.usuarios


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.firebase.ui.auth.AuthUI
import com.castellanoseloy.ventarapida.databinding.FragmentDetalleUsuarioBinding
import com.castellanoseloy.ventarapida.Login
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.procesos.FirebaseUsuarios
import com.castellanoseloy.ventarapida.procesos.Preferencias
import com.castellanoseloy.ventarapida.procesos.UtilidadesBaseDatos

class DetalleSesion : Fragment() {

    private var binding: FragmentDetalleUsuarioBinding? = null
    private lateinit var vista: View
    private var idUsuario=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetalleUsuarioBinding.inflate(inflater, container, false)


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
                verificarDatosPendientesPorCargar()
            }

        binding?.buttonEliminarCuenta?.setOnClickListener {

            eliminarCuenta()
        }
            }

    private fun eliminarCuenta() {

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setIcon(R.drawable.logo2_compra_rapidita)
        alertDialogBuilder.setTitle("Eliminar cuenta permanentemente")
        alertDialogBuilder.setMessage("¿Estás seguro de que quieres eliminar su cuenta de ${DatosPersitidos.datosEmpresa.nombre}?")
        alertDialogBuilder.setPositiveButton("Sí") { dialogInterface, _ ->
            if(DatosPersitidos.datosUsuario.id.isNotEmpty()) FirebaseUsuarios.eliminarUsuarioPorId(DatosPersitidos.datosUsuario.id).addOnCompleteListener{
                Toast.makeText(requireContext(),"Usuario eliminado",Toast.LENGTH_LONG).show()
                cerrarSesion()
            }

        }
        alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
            // Acciones a realizar cuando se hace clic en "No"
            dialogInterface.dismiss() // Cerrar el cuadro de diálogo
        }

        // Mostrar el cuadro de diálogo
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun verificarUsuarioPrincipal(): Boolean {
        if(DatosPersitidos.datosEmpresa.idDuenoCuenta!=null){
            return idUsuario == DatosPersitidos.datosEmpresa.idDuenoCuenta
        }
        return false
    }
    private fun verificarDatosPendientesPorCargar() {
        val transaccionesPendientes=
            UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos(context)


        if(transaccionesPendientes.isEmpty()){
                cerrarSesion()
        }else{
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setIcon(R.drawable.logo2_compra_rapidita)
            alertDialogBuilder.setTitle("Sincroniza tu dispositivo")
            alertDialogBuilder.setCancelable(false)
            alertDialogBuilder.setMessage("Para evitar perdida de datos antes de cambiar de usuario asegurate de sincronizar correctamente tu dispositivo")

            alertDialogBuilder.setPositiveButton("Aceptar") { _, _ ->

            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }



    }

    private fun cerrarSesion() {
        DatosPersitidos.ventaProductosSeleccionados.clear()
        DatosPersitidos.compraProductosSeleccionados.clear()

        val preferencias= Preferencias()
        preferencias.guardarPreferenciaListaSeleccionada(requireContext(),
            DatosPersitidos.compraProductosSeleccionados,"compra_seleccionada"
        )

        AuthUI.getInstance().signOut(requireContext())
            .addOnCompleteListener {

                Toast.makeText(context, "Sesion Cerrada", Toast.LENGTH_LONG).show()
                val intent = Intent(requireActivity(), Login::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
    }

    private fun cargarDatosUsario() {
        binding?.TextviewNombreUsuario?.text=DatosPersitidos.datosUsuario.nombre
        binding?.textViewCorreo?.text="Correo: "+DatosPersitidos.datosUsuario.correo
        binding?.textViewEmpresa?.text="Empresa: "+DatosPersitidos.datosEmpresa.nombre
        binding?.textViewPerfil?.text="Perfil: "+DatosPersitidos.datosUsuario.perfil
    }




}