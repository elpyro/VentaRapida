package com.example.ventarapida.ui.clientes

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.Display.Mode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentListaClientesBinding
import com.example.ventarapida.datos.ModeloClientes
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.procesos.FirebaseClientes
import com.example.ventarapida.procesos.Utilidades
import com.example.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.example.ventarapida.ui.ventaPaginaPrincipal.VentaAdaptador
import java.util.ArrayList
import java.util.Locale


class ListaClientes : Fragment() {

    private var binding: FragmentListaClientesBinding? = null
    private lateinit var vista: View
    private lateinit var adaptador: ClientesAdaptador
    private lateinit var viewModel: ListaClientesViewModel
    private var lista: ArrayList<ModeloClientes>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListaClientesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ListaClientesViewModel::class.java]

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding!!.recyclerViewClientes.layoutManager = gridLayoutManager

        val tareaClientes = FirebaseClientes.buscarTodosClientes()

        tareaClientes.addOnSuccessListener { Clientes ->
            lista= Clientes as ArrayList<ModeloClientes>?
            adaptador = ClientesAdaptador(lista!!)
            binding?.recyclerViewClientes?.adapter = adaptador

            adaptador!!.setOnLongClickItem() { item, position ->
                abriDetalle(item,vista, position)
            }
            adaptador!!.setOnClickItem  { item ->
//                val dialogView = inflater.inflate(R.layout.promt_factura, null)
//
//
//                val editTextProducto = dialogView.findViewById<EditText>(R.id.promt_producto)
//                val nombre = findViewById<TextView>(R.id.textView_cliente).text.toString()

            }
        }

        listeners()

        return binding!!.root
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

        binding!!.searchViewBuscarCliente.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
        val adaptador = filtro?.let { ClientesAdaptador(it as MutableList<ModeloClientes>) }
        binding?.recyclerViewClientes?.adapter =adaptador


    }


    private fun abriDetalle(item: ModeloClientes, vista: View, position: Int) {
        val bundle = Bundle()
        bundle.putInt("position", position)
        bundle.putSerializable("modelo", item)
//        bundle.putSerializable("listaProductos", lista)
        Navigation.findNavController(vista).navigate(R.id.detalleProducto,bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view
    }

}