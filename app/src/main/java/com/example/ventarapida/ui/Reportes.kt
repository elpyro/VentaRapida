package com.example.ventarapida.ui


import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ventarapida.MainActivity
import com.example.ventarapida.VistaPDFReporte
import com.example.ventarapida.databinding.FragmentReportesBinding
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.datos.ModeloUsuario
import com.example.ventarapida.procesos.CrearPdfGanancias
import com.example.ventarapida.procesos.CrearPdfMasVendidos
import com.example.ventarapida.procesos.CrearPdfMayorGanancia
import com.example.ventarapida.procesos.CrearPdfVentasPorVendedor
import com.example.ventarapida.procesos.FirebaseProductoFacturadosOComprados.buscarProductosPorFecha
import com.example.ventarapida.procesos.FirebaseUsuarios.buscarTodosUsuariosPorEmpresa
import com.example.ventarapida.procesos.Utilidades.convertirFechaAUnix
import java.util.Calendar


class Reportes : Fragment() {

    private var binding: FragmentReportesBinding? = null
    private lateinit var vista: View
    var posicionSpinnerVendedor=0
    lateinit var listaIdUsuarios:List<String>
    private lateinit var viewModel: ReportesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ReportesViewModel::class.java]

        listener()
        buscarTodosUsuariosPorEmpresa().addOnSuccessListener { listaUsuarios->

                if (!listaUsuarios.isEmpty()) crearSpinner(listaUsuarios)
        }
        return binding!!.root
    }
    private fun crearSpinner(listaUsuarios: MutableList<ModeloUsuario>?) {
        listaIdUsuarios = listaUsuarios?.map { it.id }!!
        val listaNombresUsuarios = listaUsuarios?.map { it.nombre }
        val lista: MutableList<String> = listaNombresUsuarios?.toMutableList() ?: mutableListOf()

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
                    if(position==2){
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

            var fechaInicio="01/01/2000"
            var fechaFin="01/01/2050"

            if(binding?.textViewDesde?.text.toString()!="Desde") fechaInicio=binding?.textViewDesde?.text.toString()
            if(binding?.textViewHasta?.text.toString()!="Hasta") fechaFin=binding?.textViewHasta?.text.toString()

            if(binding?.spinnerTipoReporte?.selectedItemPosition==0){
                ReporteGanancia(fechaInicio,fechaFin)
            }

            if(binding?.spinnerTipoReporte?.selectedItemPosition==1){
                ReporteMasVendidos(fechaInicio, fechaFin)
            }

            if(binding?.spinnerTipoReporte?.selectedItemPosition==2){
                ReportePorVendedor(fechaInicio, fechaFin)
            }

            if(binding?.spinnerTipoReporte?.selectedItemPosition==3){
                ReporteMayorGanancia(fechaInicio, fechaFin)
            }
        }
    }

    private fun ReportePorVendedor(fechaInicio: String, fechaFin: String) {
        val idVendedor=listaIdUsuarios[posicionSpinnerVendedor]
        val nombreVendedor=binding?.spinnerVendedor?.selectedItem.toString()
        buscarProductosPorFecha(convertirFechaAUnix(fechaInicio), convertirFechaAUnix(fechaFin), idVendedor )
            .addOnSuccessListener { productos ->
                val crearPdf= CrearPdfVentasPorVendedor()
                crearPdf.ventas(requireContext(), fechaInicio, fechaFin, productos as ArrayList<ModeloProductoFacturado>,nombreVendedor )

                val intent = Intent(requireContext(), VistaPDFReporte::class.java)
                requireContext().startActivity(intent)
            }
    }

    private fun ReporteMasVendidos(fechaInicio: String, fechaFin: String) {
        buscarProductosPorFecha(convertirFechaAUnix(fechaInicio), convertirFechaAUnix(fechaFin),"false" )
            .addOnSuccessListener { productos ->

               var listaMasVendidos= viewModel.crearListaMasVendidos(productos)

                val crearPdf= CrearPdfMasVendidos()
                crearPdf.masVendidos(requireContext(), fechaInicio, fechaFin,listaMasVendidos)

                val intent = Intent(requireContext(), VistaPDFReporte::class.java)
                requireContext().startActivity(intent)
            }
    }

    private fun ReporteMayorGanancia(fechaInicio: String, fechaFin: String) {
        buscarProductosPorFecha(convertirFechaAUnix(fechaInicio), convertirFechaAUnix(fechaFin),"false" )
            .addOnSuccessListener { productos ->

                var listaMasVendidos= viewModel.crearListaMayorGanancia(productos)

                val crearPdf= CrearPdfMayorGanancia()
                crearPdf.mayorGanancia(requireContext(), fechaInicio, fechaFin,listaMasVendidos)

                val intent = Intent(requireContext(), VistaPDFReporte::class.java)
                requireContext().startActivity(intent)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view
    }

    fun ReporteGanancia(fechaInicio: String, fechaFin: String) {


        buscarProductosPorFecha(convertirFechaAUnix(fechaInicio), convertirFechaAUnix(fechaFin), "false" )
        .addOnSuccessListener { productos ->

            val crearPdf= CrearPdfGanancias()
            crearPdf.ganacias(requireContext(), fechaInicio, fechaFin, productos as ArrayList<ModeloProductoFacturado>)

            val intent = Intent(requireContext(), VistaPDFReporte::class.java)
            requireContext().startActivity(intent)
        }

    }


}

