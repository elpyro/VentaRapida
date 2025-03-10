@file:Suppress("DEPRECATION")

package com.castellanoseloy.ventarapida.ui.reportes

import android.app.DatePickerDialog
import android.app.ProgressDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.databinding.FragmentReporteVendedorBinding
import com.castellanoseloy.ventarapida.procesos.Utilidades
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ReporteVendedor : Fragment() {

    private var binding: FragmentReporteVendedorBinding? = null

    private lateinit var viewModel: ReporteVendedorViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReporteVendedorBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ReporteVendedorViewModel::class.java]

        //mostrar ganancias si esta permitido
        if(DatosPersitidos.datosUsuario.configuracion.mostrarReporteGanancia) binding?.buttonGanancia?.visibility=View.VISIBLE

        binding?.textViewDesde?.text = Utilidades.obtenerFechaActual()

        listener()
        escuchadores()

        if(DatosPersitidos.datosUsuario.configuracion.mostrarPreciosCompra){
            binding?.checkBoxPersonalizado?.visibility=View.VISIBLE
        }else{
            binding?.checkBoxPersonalizado?.visibility=View.GONE
        }

        return binding!!.root
    }

    private fun escuchadores() {
        viewModel.reporteCompletado.observe(viewLifecycleOwner) {
            progressDialog.dismiss()
        }

        binding?.checkBoxPersonalizado?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                binding?.editTextAumento?.visibility=View.VISIBLE
            }else{
                binding?.editTextAumento?.visibility=View.GONE
            }

        }
    }




    private fun listener() {


        binding?.buttonCatalogo?.setOnClickListener {

            progressDialog.show()
            // Ejecutar la creación del PDF en un hilo secundario usando coroutines
            lifecycleScope.launch(Dispatchers.IO) {
                var mayorCero = true
                var aumento: Double? =null
                if(binding?.checkBoxPersonalizado?.isChecked == true)  {
                    if (binding?.editTextAumento?.text.toString().isEmpty()) {
                        binding?.editTextAumento?.setText("0.0")
                    }

                    aumento= binding?.editTextAumento?.text.toString().toDouble()
                }

                if (binding?.radioButtonCatalogoTodos!!.isChecked) mayorCero = false
                viewModel.crearCatalogo(requireContext(), mayorCero, aumento)

            }

        }

        binding?.textViewDesde?.setOnClickListener{
            val c = Calendar.getInstance()
            val dia = c[Calendar.DAY_OF_MONTH]
            val mes = c[Calendar.MONTH]
            val ano = c[Calendar.YEAR]

            val datepickerDialogo = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val formattedMonth = month + 1
                    val formattedDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                    val formattedDate = "$formattedDayOfMonth/$formattedMonth/$year"
                    binding?.textViewDesde?.text = formattedDate
                }, ano, mes, dia
            )
            datepickerDialogo.show()

        }

        binding?.textViewHasta?.setOnClickListener{

            val c = Calendar.getInstance()
            val dia = c[Calendar.DAY_OF_MONTH]
            val mes = c[Calendar.MONTH]
            val ano = c[Calendar.YEAR]

            val datepickerDialogo = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val formattedMonth = month + 1
                    val formattedDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                    val formattedDate = "$formattedDayOfMonth/$formattedMonth/$year"
                    binding?.textViewHasta?.text = formattedDate
                }, ano, mes, dia
            )
            datepickerDialogo.show()
        }

        binding?.buttonInforme?.setOnClickListener {

            var fechaInicio="01/01/2000"
            var fechaFin="01/01/2050"

            if(binding?.textViewDesde?.text.toString()!="Desde") fechaInicio=binding?.textViewDesde?.text.toString()
            if(binding?.textViewHasta?.text.toString()!="Hasta") fechaFin=binding?.textViewHasta?.text.toString()

             progressDialog.show()
             // Ejecutar la creación del PDF en un hilo secundario usando coroutines
             lifecycleScope.launch(Dispatchers.IO) {
                 viewModel.ReportePorVendedor(
                     requireContext(),
                     fechaInicio,
                     fechaFin,
                     DatosPersitidos.datosUsuario.id)
            }
        }

        binding?.buttonGanancia?.setOnClickListener {

            var fechaInicio="01/01/2000"
            var fechaFin="01/01/2050"

            if(binding?.textViewDesde?.text.toString()!="Desde") fechaInicio=binding?.textViewDesde?.text.toString()
            if(binding?.textViewHasta?.text.toString()!="Hasta") fechaFin=binding?.textViewHasta?.text.toString()

            progressDialog.show()
            // Ejecutar la creación del PDF en un hilo secundario usando coroutines
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.ReporteGananciaPorVendedor(
                    requireContext(),
                    fechaInicio,
                    fechaFin,
                    DatosPersitidos.datosUsuario.id,DatosPersitidos.datosUsuario.nombre)
            }
        }
    }




    val progressDialog: ProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setIcon(R.drawable.logo2_compra_rapidita)
            setMessage("Creando PDF...")
            setCancelable(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}

