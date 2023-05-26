package com.example.ventarapida.ui.configuracion

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.ventarapida.R
import com.example.ventarapida.procesos.Preferencias


class Configuracion : PreferenceFragmentCompat() {

    private lateinit var vista: View
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)


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