package com.example.ventarapida.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentReportesBinding

class Reportes : Fragment() {

    private var binding: FragmentReportesBinding? = null
    private lateinit var vista: View

    private lateinit var viewModel: ReportesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ReportesViewModel::class.java]


        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view
    }

}