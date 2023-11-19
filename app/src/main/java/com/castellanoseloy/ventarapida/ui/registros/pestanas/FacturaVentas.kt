@file:Suppress("DEPRECATION")

package com.castellanoseloy.ventarapida.ui.registros.pestanas

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isNotEmpty
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.MainActivity
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.FragmentFacturaVentasBinding
import com.castellanoseloy.ventarapida.datos.ModeloFactura
import com.castellanoseloy.ventarapida.procesos.FirebaseFacturaOCompra
import com.castellanoseloy.ventarapida.procesos.FirebaseFacturaOCompra.buscarFacturasOCompra
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.castellanoseloy.ventarapida.procesos.Utilidades.ocultarTeclado
import com.castellanoseloy.ventarapida.ui.nuevoProducto.NuevoProductoViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FacturaVentas : Fragment() {

    private var primeraCarga: Boolean = true
    private var searchText: String? = null
    private var binding: FragmentFacturaVentasBinding? = null
    private lateinit var vista:View
    private var progressDialog: ProgressDialog? = null
    private lateinit var adaptador: FacturaVentasAdaptador
    private lateinit var listaFacturas : MutableList<ModeloFactura>
    private lateinit var registrosViewModel: RegistrosViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding= FragmentFacturaVentasBinding.inflate(inflater, container, false)

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding?.recyclerViewFacturaVentas?.layoutManager = gridLayoutManager

        registrosViewModel = ViewModelProvider(this).get(RegistrosViewModel::class.java)

        listeners()
        observadores()

        if(MainActivity.verPublicidad)  initLoadAds()

        return binding!!.root
    }

    private fun observadores() {
        registrosViewModel.facturasLiveData.observe(viewLifecycleOwner) {facturas->
            if(facturas.isNotEmpty()) {

                binding?.linearLayoutRegistros?.visibility = View.VISIBLE
                binding?.linearLayoutExplicacionRegistros?.visibility = View.GONE
                listaFacturas = facturas
                Log.d("Registro ventas", "Lista ${listaFacturas.size}")
                if(primeraCarga) {
                    adaptador = FacturaVentasAdaptador(listaFacturas)
                    binding?.recyclerViewFacturaVentas?.adapter = adaptador
                    primeraCarga = false
                } else {
                    adaptador!!.updateData(listaFacturas)
                }

                adaptador.setOnClickItem() { item ->
                    abriDetalle(item)
                }

                if (binding?.searchViewBuscarFactura?.isNotEmpty() == true) {
                    binding?.searchViewBuscarFactura?.setQuery(searchText, false)
                    filtrarProductos(searchText!!)
                }
                binding?.swipeRefreshLayout?.isRefreshing = false
            }else{
                binding?.linearLayoutRegistros?.visibility=View.GONE
                binding?.linearLayoutExplicacionRegistros?.visibility=View.VISIBLE
            }
            progressDialog?.dismiss()
        }
    }

    fun processDialogo() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Cargando...") // Mensaje que se mostrará
        progressDialog?.setCancelable(false) // Para evitar que se cierre al tocar fuera de él
        progressDialog?.show()
    }
    private fun initLoadAds() {
        binding?.banner?.visibility=View.VISIBLE
        val adRequest = AdRequest.Builder().build()
        binding?.banner?.loadAd(adRequest)
    }


    private fun cargarLista() {
        processDialogo()
        registrosViewModel.iniciarEscucha("Factura")

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista= view

    }

    override fun onResume() {
        super.onResume()
        cargarLista()
    }
    private fun listeners() {
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            binding?.swipeRefreshLayout?.isRefreshing=false
        }
        binding?.recyclerViewFacturaVentas?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    ocultarTeclado(requireContext(),vista)
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

        val productosFiltrados = listaFacturas.filter {
            it.nombre.eliminarAcentosTildes().contains(nombreFiltrado.eliminarAcentosTildes(), ignoreCase = true)
                    || it.fecha.contains(nombreFiltrado)
                    || it.nombre_vendedor.eliminarAcentosTildes().contains(nombreFiltrado)
                    || it.id_pedido.contains(nombreFiltrado)
        }
        adaptador.updateData(productosFiltrados as MutableList<ModeloFactura>)

        adaptador.setOnClickItem() { item ->
          abriDetalle(item)
        }

    }
    private fun abriDetalle(item: ModeloFactura) {
        val bundle = Bundle()

        bundle.putSerializable("modelo", item)

        Navigation.findNavController(vista).navigate(R.id.facturaGuardada,bundle)
    }


    override fun onPause() {
        super.onPause()
        registrosViewModel.detenerEscucha()
        primeraCarga=true
    }



    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}