package com.example.ventarapida.ui.configuracion

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.ventarapida.R
import com.example.ventarapida.procesos.Preferencias


class Configuracion : PreferenceFragmentCompat() {

    private lateinit var vista: View
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)


        val customButtonPreference = findPreference<Preference>("datos_empresa")
        customButtonPreference!!.setOnPreferenceClickListener {
            Navigation.findNavController(vista).navigate(R.id.datosEmpresa)
            true
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