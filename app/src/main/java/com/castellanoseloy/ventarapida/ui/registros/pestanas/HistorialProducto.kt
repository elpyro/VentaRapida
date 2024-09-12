package com.castellanoseloy.ventarapida.ui.registros.pestanas

import android.annotation.SuppressLint
import android.app.ProgressDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.FragmentFacturaVentasBinding
import com.castellanoseloy.ventarapida.datos.ModeloFactura
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.eliminarAcentosTildes

class HistorialProducto : Fragment() {
    private var listaProductosCompleta: List<ModeloProductoFacturado>? = null
    private var idProducto: String? = null
    private var primeraCarga = true
    private var progressDialog: ProgressDialog? = null
    private var searchText: String? = null
    private var binding: FragmentFacturaVentasBinding? = null
    private lateinit var vista:View

    private lateinit var adaptador: HistorialProductoAdaptador

    private lateinit var viewModel: HistorialProductoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentFacturaVentasBinding.inflate(inflater, container, false)

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding?.recyclerViewFacturaVentas?.layoutManager = gridLayoutManager

        viewModel = ViewModelProvider(this)[HistorialProductoViewModel::class.java]

        val bundle = arguments
        idProducto = bundle?.getString("idProducto")

        binding?.cardViewTotales?.visibility=View.VISIBLE

        viewModel.cargarRegistros(idProducto)


        progressDialogo()

        observadores()
        listeners()
//        if(DatosPersitidos.verPublicidad)  initLoadAds()

        return binding!!.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista= view

    }

    fun progressDialogo(){
        if (progressDialog == null) {
            progressDialog?.setIcon(R.drawable.logo2_compra_rapidita)
            progressDialog = ProgressDialog(requireContext())
            progressDialog?.setMessage("Cargando...") // Puedes personalizar el mensaje
            progressDialog?.setCancelable(false)
        }
    }

    private fun observadores() {
        viewModel.historialProductos.observe(viewLifecycleOwner) { listaProductos ->

            listaProductosCompleta = listaProductos
            adaptador = HistorialProductoAdaptador(listaProductos)
            binding?.recyclerViewFacturaVentas?.adapter = adaptador

            listenersRecyclerView()

            // Verificar si hay un texto de búsqueda y filtrar automáticamente
            if (!searchText.isNullOrBlank()) {
                filtrarProductos(searchText!!)
            }

            totales(listaProductos)

            progressDialog?.dismiss()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun totales(listaProductos: List<ModeloProductoFacturado>) {
        // Calcular las cantidades de productos comprados y vendidos
        var totalComprados = 0
        var totalVendidos = 0

        listaProductos.forEach { productoFacturado ->
            val cantidad = productoFacturado.cantidad.toIntOrNull() ?: 0

            // Verificar si el producto es comprado o vendido
            if (productoFacturado.tipoOperacion == "Surtido") {
                totalComprados += cantidad
            } else {
                totalVendidos += cantidad
            }
        }

        // Mostrar los resultados en el log
        Log.d("HistorialProductoViewModel", "Total productos comprados: $totalComprados")
        Log.d("HistorialProductoViewModel", "Total productos vendidos: $totalVendidos")
        val totalProductos = totalComprados - totalVendidos
        binding?.textViewSutidos?.text = "Surtidos: $totalComprados"
        binding?.textViewVendidos?.text = "Vendidos: $totalVendidos"
        if(primeraCarga){
            binding?.textViewInventario?.text = "Inventario: $totalProductos"
            primeraCarga=false
        }
    }


    private fun listeners() {
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            binding?.swipeRefreshLayout?.isRefreshing=false
            viewModel.cargarRegistros(idProducto)
        }
        binding?.recyclerViewFacturaVentas?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    Utilidades.ocultarTeclado(requireContext(), vista)
                }
            }
        })
        binding?.searchViewBuscarFactura?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    searchText=newText
                    filtrarProductos(newText)
                }
                return true
            }
        })
        //desbloquea searchview al seleccionarlo
        binding?.searchViewBuscarFactura?.setOnClickListener {
            binding?.searchViewBuscarFactura?.isIconified=false
        }
    }

    fun filtrarProductos(nombreFiltrado: String) {

        val productosFiltrados = listaProductosCompleta?.filter {
            it.vendedor.eliminarAcentosTildes().contains(nombreFiltrado.eliminarAcentosTildes(), ignoreCase = true)
                    || it.fecha.contains(nombreFiltrado)
                    || it.tipoOperacion.eliminarAcentosTildes().contains(nombreFiltrado)
                    || it.id_pedido.contains(nombreFiltrado)
                    || it.productoEditado.eliminarAcentosTildes().contains(nombreFiltrado.eliminarAcentosTildes(), ignoreCase = true)
        }
        adaptador.updateData(productosFiltrados as MutableList<ModeloProductoFacturado>)

        listenersRecyclerView()
        totales(productosFiltrados)

    }

    private fun listenersRecyclerView() {
        adaptador.setOnClickItem() { item ->
            abriDetalleFactura(item)
        }
        adaptador.setOnClickItemSurtido() { item ->
            abriDetalleSurtido(item)
        }
    }

    override fun onResume() {
        super.onResume()
        progressDialog?.show()
        viewModel.cargarRegistros(idProducto)
    }
    private fun abriDetalleFactura(item: ModeloFactura) {
        val bundle = Bundle()
        bundle.putSerializable("modelo", item)
        try {
            Navigation.findNavController(vista).navigate(R.id.facturaGuardada,bundle)
        }catch (e:Exception){}

    }

    private fun abriDetalleSurtido(item: ModeloFactura) {
        val bundle = Bundle()
        bundle.putSerializable("modelo", item)
        try {
            Navigation.findNavController(vista).navigate(R.id.compraGuardada,bundle)
        }catch (e:Exception){}

    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}