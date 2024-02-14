package com.castellanoseloy.ventarapida.ui.usuarios


import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
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
import com.castellanoseloy.ventarapida.ui.promts.PromtEliminarCuenta
import com.castellanoseloy.ventarapida.ui.promts.PromtFacturaGuardada
import java.io.File
import java.net.URLEncoder

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

        binding?.imageButtonWhatsApp?.setOnClickListener{
            enviarWhatsappSoporte()
        }

            binding?.buttonRegister?.setOnClickListener {
                verificarDatosPendientesPorCargar()
            }

        binding?.buttonEliminarCuenta?.setOnClickListener {

            eliminarCuenta()
        }
            }

    private fun enviarWhatsappSoporte() {
        val numeroWhatsApp = "+573215866072"
        val mensaje="Hola, necesito ayuda con CATAPLUS, "
        val uri = Uri.parse("whatsapp://send?phone=$numeroWhatsApp&text=${URLEncoder.encode(mensaje, "UTF-8")}")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        // Verificar si hay aplicaciones disponibles para manejar la intención
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            requireActivity().startActivity(intent)
        } else {
            Toast.makeText(requireActivity(), "WhatsApp no están instalado.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun eliminarCuenta() {

        val promtEliminarCuenta= PromtEliminarCuenta()
        promtEliminarCuenta.eliminar(requireContext(), requireActivity())

    }

    fun cerrarSesion() {
        DatosPersitidos.ventaProductosSeleccionados.clear()
        DatosPersitidos.compraProductosSeleccionados.clear()

        val preferencias= Preferencias()
        preferencias.guardarPreferenciaListaSeleccionada(requireContext(),
            DatosPersitidos.compraProductosSeleccionados,"compra_seleccionada"
        )

        preferencias.guardarPreferenciaListaSeleccionada(requireContext(),
            DatosPersitidos.ventaProductosSeleccionados,"venta_seleccionada"
        )


        AuthUI.getInstance().signOut(requireContext())
            .addOnCompleteListener {

                Toast.makeText(context, "Sesion Cerrada", Toast.LENGTH_LONG).show()
                val intent = Intent(requireActivity(), Login::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
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


    private fun cargarDatosUsario() {
        binding?.TextviewNombreUsuario?.text=DatosPersitidos.datosUsuario.nombre
        binding?.textViewCorreo?.text="Correo: "+DatosPersitidos.datosUsuario.correo
        binding?.textViewEmpresa?.text="Empresa: "+DatosPersitidos.datosEmpresa.nombre
        binding?.textViewPerfil?.text="Perfil: "+DatosPersitidos.datosUsuario.perfil
    }




}