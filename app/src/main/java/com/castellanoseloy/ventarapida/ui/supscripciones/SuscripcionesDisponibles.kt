package com.castellanoseloy.ventarapida.ui.supscripciones

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.castellanoseloy.ventarapida.MainActivity
import com.castellanoseloy.ventarapida.databinding.FragmentSuscripcionesDisponiblesBinding
import com.castellanoseloy.ventarapida.procesos.Utilidades.calcularDiasRestantes
import com.castellanoseloy.ventarapida.procesos.Utilidades.convertirCadenaAFecha
import com.castellanoseloy.ventarapida.procesos.Utilidades.convertirFechaLegible


class SuscripcionesDisponibles : Fragment() {
    private lateinit var binding: FragmentSuscripcionesDisponiblesBinding
    private lateinit var vista: View



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSuscripcionesDisponiblesBinding.inflate(inflater, container, false)

        datosPlan()
        return binding!!.root
    }

    private fun datosPlan() {
        binding.textViewCurrentPlan.setText("Plan Actual: ${MainActivity.datosEmpresa.plan}")


        if(!MainActivity.datosEmpresa.proximo_pago.equals("")){
            binding.textViewExpirationDate.setText("Fecha de Vencimiento: ${convertirFechaLegible(MainActivity.datosEmpresa.proximo_pago)}")

            val diasRestantes= calcularDiasRestantes(convertirCadenaAFecha(MainActivity.datosEmpresa.proximo_pago)!!)

            binding.textViewDiasRestantes.setText(diasRestantes)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view
    }

}