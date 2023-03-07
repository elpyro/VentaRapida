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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ventarapida.databinding.FragmentHomeBinding
import com.example.ventarapida.ui.adapter.ProductAdapter
import com.google.firebase.database.FirebaseDatabase


class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private lateinit var productViewModel: HomeViewModel


    // This property is only valid between onCreateView and
    // onDestroyView.

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



        productViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        productViewModel.getProductos().observe(viewLifecycleOwner, {
            Toast.makeText(context, "Productos "+it.size, Toast.LENGTH_SHORT).show()
            val adapter = ProductAdapter(it)



            val gridLayoutManager = GridLayoutManager(requireContext(), 2)
            binding!!.recyclerView.layoutManager = gridLayoutManager
            binding!!.recyclerView.adapter = adapter

            binding!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter(newText.orEmpty())
                    return true
                }
            })

        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}