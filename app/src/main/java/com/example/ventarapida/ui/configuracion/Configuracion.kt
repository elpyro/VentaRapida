package com.example.ventarapida.ui.configuracion

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.example.ventarapida.MainActivity
import com.example.ventarapida.R
import com.example.ventarapida.procesos.FirebaseDatosEmpresa.guardarDatosEmpresa
import com.example.ventarapida.procesos.Preferencias
import com.example.ventarapida.procesos.Utilidades


class Configuracion : PreferenceFragmentCompat() {
    private var informacionAdicional: PreferenceCategory? = null

    private lateinit var vista: View

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        if (!MainActivity.datosUsuario.configuracion.agregarInformacionAdicional) {
            informacionAdicional = findPreference("informacion_adicional")
            informacionAdicional?.isVisible = false
        }
//        // Obtén la preferencia "mostrar_precio_compra"
//        val mostrarPrecioCompraPreference = findPreference<SwitchPreference>("mostrar_precio_compra_preference")
//
//        // Establece el valor predeterminado en función del valor de verPreciosMayoristas
//        val verPreciosMayoristas = MainActivity.datosEmpresa.mostrarPreciosCompra.toBoolean()
//        mostrarPrecioCompraPreference?.setDefaultValue(verPreciosMayoristas)
//
//        mostrarPrecioCompraPreference?.setOnPreferenceChangeListener { preference, newValue ->
//            val isChecked = newValue as Boolean
//            //Toast.makeText(requireContext(),"Error al obtener la URL de descarga de la imagen subida.",Toast.LENGTH_LONG).show()
//            val updates = hashMapOf(
//                "id" to MainActivity.datosEmpresa.id,
//                "mostrarPreciosCompra" to isChecked.toString()
//            )
//            // Guarda los cambios en Firebase o donde corresponda
//            guardarDatosEmpresa(updates)
//            true
//        }
    }


    class ConfiguracionFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view
    }

    override fun onDetach() {
        super.onDetach()
        val preferenciasConfiguracion= Preferencias()
        preferenciasConfiguracion.preferenciasConfiguracion(requireContext())
    }




}