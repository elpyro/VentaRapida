@file:Suppress("DEPRECATION")

package com.castellanoseloy.ventarapida.ui.reportes


import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.castellanoseloy.ventarapida.MainActivity
import com.castellanoseloy.ventarapida.databinding.FragmentReportesBinding
import com.castellanoseloy.ventarapida.datos.ModeloUsuario
import com.castellanoseloy.ventarapida.procesos.FirebaseUsuarios.buscarTodosUsuariosPorEmpresa
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.google.android.gms.ads.AdRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar


class Reportes : Fragment() {

    private var binding: FragmentReportesBinding? = null
    var posicionSpinnerVendedor=0
    lateinit var listaIdUsuarios:List<String>
    private lateinit var viewModel: ReportesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ReportesViewModel::class.java]

        listener()
        escuchadores()
        binding?.textViewDesde?.text = Utilidades.obtenerFechaActual()
        buscarTodosUsuariosPorEmpresa().addOnSuccessListener { listaUsuarios->
                if (listaUsuarios.isNotEmpty()) crearSpinner(listaUsuarios)
        }
        if(MainActivity.verPublicidad)  initLoadAds()

        return binding!!.root
    }
    private fun initLoadAds() {
        binding?.banner?.visibility=View.VISIBLE
        val adRequest = AdRequest.Builder().build()
        binding?.banner?.loadAd(adRequest)
    }

    private fun escuchadores() {
        viewModel.reporteCompletado.observe(viewLifecycleOwner) {
            progressDialog.dismiss()
        }
    }

    private fun crearSpinner(listaUsuarios: MutableList<ModeloUsuario>?) {
        listaIdUsuarios = listaUsuarios?.map { it.id }!!
        val listaNombresUsuarios = listaUsuarios.map { it.nombre }
        val lista: MutableList<String> = listaNombresUsuarios.toMutableList()

        // Crear un adaptador utilizando la lista de nombres de usuarios
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lista)

        // Especificar el diseño para los elementos desplegables
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Asignar el adaptador al Spinner
        binding?.spinnerVendedor?.adapter = adapter
    }


    private fun listener() {
        binding?.spinnerVendedor?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Aquí puedes obtener la posición seleccionada
                posicionSpinnerVendedor = position
                // Puedes realizar cualquier acción adicional que necesites con la posición seleccionada
                // ...
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Implementación opcional en caso de que no se seleccione ningún elemento
            }
        }

        binding?.spinnerTipoReporte?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Aquí puedes obtener la posición seleccionada
                    if(position==2 || position==4){
                        binding?.spinnerVendedor?.visibility=View.VISIBLE
                    }else{
                        binding?.spinnerVendedor?.visibility=View.GONE
                    }
                // Puedes realizar cualquier acción adicional que necesites con la posición seleccionada
                // ...
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Implementación opcional en caso de que no se seleccione ningún elemento
            }
        }

        binding?.buttonInventario?.setOnClickListener {
            progressDialog.show()
            // Ejecutar la creación del PDF en un hilo secundario usando coroutines
            lifecycleScope.launch(Dispatchers.IO) {
                var mayorCero = true
                if (binding?.radioButtonTodos!!.isChecked) mayorCero = false

                viewModel.crearInventarioPdf(requireContext(), mayorCero)
            }
        }

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

            if(binding?.spinnerTipoReporte?.selectedItemPosition==0){
                progressDialog.show()
                // Ejecutar la creación del PDF en un hilo secundario usando coroutines
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.ReporteGanancia(requireContext(),fechaInicio,fechaFin)

                }

            }

            if(binding?.spinnerTipoReporte?.selectedItemPosition==1){
                progressDialog.show()
                // Ejecutar la creación del PDF en un hilo secundario usando coroutines
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.ReporteMasVendidos(requireContext(), fechaInicio, fechaFin)
                }
            }

            if(binding?.spinnerTipoReporte?.selectedItemPosition==2){
                progressDialog.show()
                // Ejecutar la creación del PDF en un hilo secundario usando coroutines
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.ReportePorVendedor(
                        requireContext(),
                        fechaInicio,
                        fechaFin,
                        listaIdUsuarios[posicionSpinnerVendedor],
                        binding!!
                    )
                }
            }

            if(binding?.spinnerTipoReporte?.selectedItemPosition==3){
                progressDialog.show()
                // Ejecutar la creación del PDF en un hilo secundario usando coroutines
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.ReporteMayorGanancia(requireContext(), fechaInicio, fechaFin)
                }
            }

            if(binding?.spinnerTipoReporte?.selectedItemPosition==4){
                progressDialog.show()
                // Ejecutar la creación del PDF en un hilo secundario usando coroutines
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.ReporteGananciaPorVendedor(requireContext(),fechaInicio,fechaFin,listaIdUsuarios[posicionSpinnerVendedor],binding!!)
                }
            }

            if(binding?.spinnerTipoReporte?.selectedItemPosition==5){
                progressDialog.show()
                // Ejecutar la creación del PDF en un hilo secundario usando coroutines
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.ReporteSurtido(requireContext(),fechaInicio,fechaFin)
                }
            }

            if(binding?.spinnerTipoReporte?.selectedItemPosition==6){
                progressDialog.show()
                // Ejecutar la creación del PDF en un hilo secundario usando coroutines
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.ReporteSurtidoPorProducto(requireContext(), fechaInicio, fechaFin)
                }
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

