package com.example.ventarapida.ui.configuracion

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentDetalleVentaBinding
import com.example.ventarapida.procesos.Preferencias
import com.example.ventarapida.procesos.Utilidades
import com.example.ventarapida.ui.detalleVenta.DetalleVentaViewModel


class Configuracion : PreferenceFragmentCompat() {
    private var informacionAdicional: PreferenceCategory? = null
    private var infSuperior: EditTextPreference? =null
    private var inf_inferior: EditTextPreference? =null
    private lateinit var vista: View
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val permisoAdministrador= Utilidades.verificarPermisosAdministrador()
        if (!permisoAdministrador){
            informacionAdicional= findPreference("informacion_adicional")
            informacionAdicional?.isVisible =false
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