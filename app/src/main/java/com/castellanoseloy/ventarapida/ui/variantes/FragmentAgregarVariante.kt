package com.castellanoseloy.ventarapida.ui.variantes

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.castellanoseloy.ventarapida.R

class FragmentAgregarVariante : Fragment() {

    companion object {
        fun newInstance() = FragmentAgregarVariante()
    }

    private val viewModel: FragmentAgregarVarianteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_fragment_agregar_variante, container, false)
    }
}