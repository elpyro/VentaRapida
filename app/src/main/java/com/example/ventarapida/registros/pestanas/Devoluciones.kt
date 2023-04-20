package com.example.ventarapida.registros.pestanas

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ventarapida.R

class Devoluciones : Fragment() {

    companion object {
        fun newInstance() = Devoluciones()
    }

    private lateinit var viewModel: DevolcionesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_devolciones, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DevolcionesViewModel::class.java)
        // TODO: Use the ViewModel
    }

}