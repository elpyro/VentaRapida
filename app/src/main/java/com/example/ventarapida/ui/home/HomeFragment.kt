package com.example.ventarapida.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.databinding.FragmentHomeBinding
import com.example.ventarapida.ui.adapter.ProductAdapter
import com.example.ventarapida.ui.data.ModeloProducto
import com.example.ventarapida.ui.process.HideKeyboard
import java.util.*


class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private lateinit var productViewModel: HomeViewModel
    private var lista: ArrayList<ModeloProducto>? = null
    private var adapter: ProductAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding!!.recyclerView.layoutManager = gridLayoutManager

        productViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        productViewModel.getProductos().observe(viewLifecycleOwner,) { productos ->
            Toast.makeText(context, "Productos ${productos.size}", Toast.LENGTH_SHORT).show()
            adapter = ProductAdapter(productos)
            lista = productos as ArrayList<ModeloProducto>?
            binding!!.recyclerView.adapter = adapter
        }

        binding?.recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    HideKeyboard(requireContext()).hideKeyboard(view)
                }
            }
        })

        if (binding!!.searchView != null) {
            binding!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
        }
    }

    private fun filtro(valor: String) {
        val filtro = lista?.filter { objeto ->
            objeto.nombre.toLowerCase().contains(valor.lowercase(Locale.getDefault()))
        }
        val adaptador = filtro?.let { ProductAdapter(it) }
        binding?.recyclerView?.adapter = adaptador
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}