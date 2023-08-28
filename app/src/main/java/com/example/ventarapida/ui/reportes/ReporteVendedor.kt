package com.example.ventarapida.ui.reportes

import android.app.DatePickerDialog
import android.app.ProgressDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.example.ventarapida.MainActivity
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentReporteVendedorBinding
import com.example.ventarapida.databinding.FragmentReportesBinding
import com.example.ventarapida.datos.ModeloUsuario
import com.example.ventarapida.procesos.FirebaseUsuarios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ReporteVendedor : Fragment() {

    private var binding: FragmentReporteVendedorBinding? = null
    var posicionSpinnerVendedor=0
    lateinit var listaIdUsuarios:List<String>
    private lateinit var viewModel: ReporteVendedorViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReporteVendedorBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ReporteVendedorViewModel::class.java]

        binding?.textViewNombre?.setText(MainActivity.datosUsuario.nombre)

        listener()
        escuchadores()
        FirebaseUsuarios.buscarTodosUsuariosPorEmpresa().addOnSuccessListener { listaUsuarios->


        }
        return binding!!.root
    }

    private fun escuchadores() {
        viewModel.reporteCompletado.observe(viewLifecycleOwner) {
            progressDialog.dismiss()
        }
    }




    private fun listener() {


        binding?.buttonCatalogo?.setOnClickListener {

            progressDialog.show()
            // Ejecutar la creación del PDF en un hilo secundario usando coroutines
            lifecycleScope.launch(Dispatchers.IO) {
                var mayorCero = true
                if (binding?.radioButtonCatalogoTodos!!.isChecked) mayorCero = false

                viewModel.crearCatalogo(requireContext(), mayorCero)
            }
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
                     MainActivity.datosUsuario.id,
                     binding!!)
            }
        }
    }


    val progressDialog: ProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage("Creando PDF...")
            setCancelable(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}

