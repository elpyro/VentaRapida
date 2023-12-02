@file:Suppress("DEPRECATION")

package com.castellanoseloy.ventarapida.ui.registros.pestanas

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.FragmentFacturaVentasBinding
import com.castellanoseloy.ventarapida.datos.ModeloFactura
import com.castellanoseloy.ventarapida.procesos.FirebaseFacturaOCompra
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.google.android.gms.ads.AdRequest

class ListaCompras : Fragment() {
    private var progressDialog: ProgressDialog? = null
    private var binding: FragmentFacturaVentasBinding? = null
    private lateinit var vista:View
    var productosFiltrados:List<ModeloFactura> = emptyList()
    private lateinit var adaptador: FacturaVentasAdaptador
    private lateinit var listaFacturas : MutableList<ModeloFactura>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding= FragmentFacturaVentasBinding.inflate(inflater, container, false)


        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding!!.recyclerViewFacturaVentas.layoutManager = gridLayoutManager


        listeners()


        if(DatosPersitidos.verPublicidad)  initLoadAds()
        return binding!!.root
    }

    override fun onResume() {
        super.onResume()
        cargarLista()
    }
    private fun initLoadAds() {
        binding?.banner?.visibility=View.VISIBLE
        val adRequest = AdRequest.Builder().build()
        binding?.banner?.loadAd(adRequest)
    }
    fun processDialogo() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Cargando...") // Mensaje que se mostrará
        progressDialog?.setCancelable(false) // Para evitar que se cierre al tocar fuera de él
        progressDialog?.show()
    }
    private fun cargarLista() {
        processDialogo()
        val tareaFacturas = FirebaseFacturaOCompra.buscarFacturasOCompra("Compra")
        tareaFacturas.addOnSuccessListener { facturas ->
            if(facturas.isNotEmpty()){
                binding?.linearLayoutRegistros?.visibility=View.VISIBLE
                binding?.linearLayoutExplicacionRegistros?.visibility=View.GONE
                listaFacturas=facturas
                productosFiltrados = listaFacturas
                adaptador = FacturaVentasAdaptador(listaFacturas)
                binding?.recyclerViewFacturaVentas?.adapter = adaptador

                adaptador.setOnClickItem() { item ->
                    abriDetalle(item)
                }
                binding?.swipeRefreshLayout?.isRefreshing=false
            }else{
                binding?.linearLayoutRegistros?.visibility=View.GONE
                binding?.linearLayoutExplicacionRegistros?.visibility=View.VISIBLE
            }
            progressDialog?.dismiss()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista= view
    }
    private fun listeners() {
        binding?.swipeRefreshLayout?.setOnRefreshListener {
           cargarLista()
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


        productosFiltrados = listaFacturas.filter {
            it.nombre.eliminarAcentosTildes().contains(nombreFiltrado.eliminarAcentosTildes(), ignoreCase = true)
                    || it.fecha.contains(nombreFiltrado)
                    || it.nombre_vendedor.eliminarAcentosTildes().contains(nombreFiltrado)
                    || it.id_pedido.contains(nombreFiltrado)
        }
        adaptador = FacturaVentasAdaptador(productosFiltrados as MutableList<ModeloFactura>)
        binding?.recyclerViewFacturaVentas?.adapter = adaptador


        adaptador.setOnClickItem() { item ->
            abriDetalle(item)
        }

    }
    private fun abriDetalle(item: ModeloFactura) {
        val bundle = Bundle()

        bundle.putSerializable("modelo", item)
        bundle.putSerializable("lista",  ArrayList(productosFiltrados))
        Navigation.findNavController(vista).navigate(R.id.compraGuardada,bundle)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}