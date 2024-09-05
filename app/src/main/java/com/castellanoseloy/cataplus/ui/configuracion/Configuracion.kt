package com.castellanoseloy.cataplus.ui.configuracion

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.R
import com.castellanoseloy.cataplus.procesos.Preferencias


class Configuracion : PreferenceFragmentCompat() {
    private var informacionAdicional: PreferenceCategory? = null

    private lateinit var vista: View

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        if (!DatosPersitidos.datosUsuario.configuracion.agregarInformacionAdicional) {
            informacionAdicional = findPreference("informacion_adicional")
            informacionAdicional?.isVisible = false
        }

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