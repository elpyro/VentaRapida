package com.example.ventarapida.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ventarapida.MainActivity
import com.example.ventarapida.VistaPDFReporte
import com.example.ventarapida.databinding.FragmentReportesBinding
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.CrearPdfGanancias
import com.example.ventarapida.procesos.CrearPdfInventario
import com.example.ventarapida.procesos.FirebaseProductoFacturadosOComprados.buscarProductosPorFecha
import com.example.ventarapida.procesos.Utilidades.convertirFechaAUnix
import java.util.Calendar


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

        listener()

        return binding!!.root
    }

    private fun listener() {
        binding?.buttonInventario?.setOnClickListener {
            MainActivity.progressDialog?.show()
            var mayorCero=true
            if (binding?.radioButtonTodos!!.isChecked) mayorCero=false

            viewModel.crearInventarioPdf(requireContext(), mayorCero)
        }

        binding?.textViewDesde?.setOnClickListener{
                val c = Calendar.getInstance()
                val dia = c[Calendar.DAY_OF_MONTH]
                val mes = c[Calendar.MONTH]
                val ano = c[Calendar.YEAR]

                val datepickerDialogo = DatePickerDialog(
                    requireContext(),
                    { view, year, month, dayOfMonth ->
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
                    { view, year, month, dayOfMonth ->
                        val formattedMonth = month + 1
                        val formattedDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                        val formattedDate = "$formattedDayOfMonth/$formattedMonth/$year"
                        binding?.textViewHasta?.text = formattedDate
                    }, ano, mes, dia
                )
                datepickerDialogo.show()
        }

        binding?.buttonInforme?.setOnClickListener {

            if(binding?.spinnerTipoReporte?.selectedItemPosition==0){
                ReporteGanancia()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view
    }

    fun ReporteGanancia (){
        var fechaInicio="01/01/2000"
        var fechaFin="01/01/2050"

        if(binding?.textViewDesde?.text.toString()!="Desde") fechaInicio=binding?.textViewDesde?.text.toString()
        if(binding?.textViewHasta?.text.toString()!="Hasta") fechaFin=binding?.textViewHasta?.text.toString()

        buscarProductosPorFecha(convertirFechaAUnix(fechaInicio), convertirFechaAUnix(fechaFin) )
        .addOnSuccessListener { productos ->

            val crearPdf= CrearPdfGanancias()
            crearPdf.ganacias(requireContext(), fechaInicio, fechaFin, productos as ArrayList<ModeloProductoFacturado>)

            val intent = Intent(requireContext(), VistaPDFReporte::class.java)
            requireContext().startActivity(intent)
        }

    }


}

