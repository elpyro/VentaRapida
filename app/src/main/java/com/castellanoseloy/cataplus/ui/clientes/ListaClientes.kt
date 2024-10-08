package com.castellanoseloy.cataplus.ui.clientes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.cataplus.R
import com.castellanoseloy.cataplus.databinding.FragmentListaClientesBinding
import com.castellanoseloy.cataplus.datos.ModeloClientes
import com.castellanoseloy.cataplus.datos.ModeloFactura
import com.castellanoseloy.cataplus.procesos.FirebaseClientes
import com.castellanoseloy.cataplus.procesos.Utilidades
import com.castellanoseloy.cataplus.procesos.Utilidades.eliminarAcentosTildes
import com.castellanoseloy.cataplus.ui.detalleVenta.DetalleVentaViewModel
import com.castellanoseloy.cataplus.ui.promts.PromtFacturaGuardada

import java.util.ArrayList


@Suppress("DEPRECATION")
class ListaClientes : Fragment() {

    private var binding: FragmentListaClientesBinding? = null
    private lateinit var vista: View
    private lateinit var adaptador: ClientesAdaptador

    private var lista: ArrayList<ModeloClientes>? = null
    var compartirModeloFactura: ModeloFactura? = null
    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListaClientesBinding.inflate(inflater, container, false)

        val modeloFactura = arguments?.getSerializable("modeloFactura") as? ModeloFactura
        if (modeloFactura != null) {
            compartirModeloFactura=modeloFactura
        }


        setHasOptionsMenu(true)

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding!!.recyclerViewClientes.layoutManager = gridLayoutManager

        val tareaClientes = FirebaseClientes.buscarTodosClientes()

        tareaClientes.addOnSuccessListener { Clientes ->
            if(Clientes.isNotEmpty()){
                binding?.LinearLayoutClientes?.visibility=View.VISIBLE
                binding?.buttonPrimerCliente?.visibility=View.GONE
                lista= Clientes as ArrayList<ModeloClientes>?
                adaptador = ClientesAdaptador(lista!!)
                binding?.recyclerViewClientes?.adapter = adaptador

                listenerAdaptador()

                val busqueda = binding?.searchViewBuscarCliente?.query.toString()
                if(busqueda!=""){
                    filtro(busqueda)
                }
                listeners()
            }else{
                binding?.LinearLayoutClientes?.visibility=View.GONE
                binding?.buttonPrimerCliente?.visibility=View.VISIBLE
                binding?.buttonPrimerCliente?.setOnClickListener {  Navigation.findNavController(vista).navigate(R.id.clienteAgregarModificar) }
            }

        }



        return binding!!.root
    }


    private fun listenerAdaptador() {
        adaptador.setOnLongClickItem { item ->
            abriDetalle(item,vista)
        }
        adaptador.setOnClickItem  { item ->

            compartirCliente(item)
        }
    }

    private fun compartirCliente(item: ModeloClientes) {
        if (compartirModeloFactura != null) {
            //si se compartie a el PromtFacturaGuardada
            compartirModeloFactura!!.nombre=item.nombre
            compartirModeloFactura!!.documento=item.documento
            compartirModeloFactura!!.telefono=item.telefono
            compartirModeloFactura!!.direccion=item.direccion

            val promtEditarDatos=PromtFacturaGuardada()
            promtEditarDatos.promtEditarDatosCliente(compartirModeloFactura!!,requireActivity(),vista)

        } else {
            //si se comparte a el DetalleFactura
            DetalleVentaViewModel.datosCliente.value=item
        }

        findNavController().popBackStack()

    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_nuevo_cliente, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_nuevo_cliente ->{
                Navigation.findNavController(vista).navigate(R.id.clienteAgregarModificar)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
    private fun listeners() {
        binding?.recyclerViewClientes?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    Utilidades.ocultarTeclado(requireContext(), vista)

                }
            }
        })

        binding?.searchViewBuscarCliente?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filtro(newText)
                }
                return true
            }
        })
        //desbloquea searchview al seleccionarlo
        binding?.searchViewBuscarCliente?.setOnClickListener {
            binding?.searchViewBuscarCliente?.isIconified=false
        }

    }

    private fun filtro(textoParaFiltrar: String) {

        val filtro = lista?.filter { objeto: ModeloClientes ->
            objeto.nombre.eliminarAcentosTildes().contains(textoParaFiltrar.eliminarAcentosTildes(), ignoreCase = true)
                    || objeto.direccion.eliminarAcentosTildes().contains(textoParaFiltrar.eliminarAcentosTildes(), ignoreCase = true)
        }
        adaptador = filtro?.let { ClientesAdaptador(it as MutableList<ModeloClientes>) }!!
        binding?.recyclerViewClientes?.adapter =adaptador

        listenerAdaptador()


    }


    private fun abriDetalle(item: ModeloClientes, vista: View) {
        val bundle = Bundle()
        bundle.putSerializable("modelo", item)
        Navigation.findNavController(vista).navigate(R.id.clienteAgregarModificar,bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view
    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }

}