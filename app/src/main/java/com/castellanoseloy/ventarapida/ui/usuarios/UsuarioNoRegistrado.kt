package com.castellanoseloy.ventarapida.ui.usuarios

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.castellanoseloy.ventarapida.databinding.FragmentUsuarioNoRegistradoBinding

class UsuarioNoRegistrado : Fragment() {

    private var binding: FragmentUsuarioNoRegistradoBinding? = null
    private lateinit var vista: View

    private lateinit var viewModel: UsuarioNoRegistradoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUsuarioNoRegistradoBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[UsuarioNoRegistradoViewModel::class.java]


        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view
    }

}