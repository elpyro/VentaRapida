package com.example.ventarapida.registros.pestanas

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.databinding.FragmentFacturaVentasBinding
import com.example.ventarapida.ui.datos.ModeloFactura
import com.example.ventarapida.ui.procesos.FirebaseFactura.buscarFacturas
import com.example.ventarapida.ui.procesos.OcultarTeclado
import com.example.ventarapida.ui.procesos.Utilidades.eliminarAcentosTildes

class FacturaVentas : Fragment() {


    private lateinit var viewModel: FacturaVentasViewModel
    private var binding: FragmentFacturaVentasBinding? = null
    private lateinit var vista:View
    private lateinit var adaptador:FacturaVentasAdaptador
    private lateinit var listaFacturas : MutableList<ModeloFactura>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding= FragmentFacturaVentasBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(FacturaVentasViewModel::class.java)

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding!!.recyclerViewFacturaVentas.layoutManager = gridLayoutManager

        val tareaFacturas = buscarFacturas()

        tareaFacturas.addOnSuccessListener { facturas ->
            listaFacturas=facturas

            adaptador = FacturaVentasAdaptador(listaFacturas)
            binding?.recyclerViewFacturaVentas?.adapter = adaptador
        }

        listeners()




//        adaptador.setOnClickItem() { item, position ->
////                Toast.makeText(requireContext(),"Editar factura ${item.nombre}",Toast.LENGTH_LONG).show()
//        }




        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista= view
    }
    private fun listeners() {

        binding?.recyclerViewFacturaVentas?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    OcultarTeclado(requireContext()).hideKeyboard(vista)
                }
            }
        })

        binding!!.searchViewBuscarFactura.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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


        val productosFiltrados = listaFacturas.filter {
            it.nombre.eliminarAcentosTildes().contains(nombreFiltrado.eliminarAcentosTildes(), ignoreCase = true)
                    || it.fecha.contains(nombreFiltrado)
                    || it.nombre_vendedor.eliminarAcentosTildes().contains(nombreFiltrado)
                    || it.id_pedido.contains(nombreFiltrado)
        }
        adaptador = FacturaVentasAdaptador(productosFiltrados as MutableList<ModeloFactura>)
        binding?.recyclerViewFacturaVentas?.adapter = adaptador


//        adaptador!!.setOnClickItem() { item, cantidad, position ->
//            editarItem(item, cantidad, position)
//        }

    }


}