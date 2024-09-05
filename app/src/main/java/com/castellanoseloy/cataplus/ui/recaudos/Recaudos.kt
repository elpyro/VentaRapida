package com.castellanoseloy.cataplus.ui.recaudos

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.castellanoseloy.cataplus.databinding.FragmentRecaudosBinding
import com.castellanoseloy.cataplus.datos.ModeloUsuario
import com.castellanoseloy.cataplus.procesos.FirebaseUsuarios.buscarTodosUsuariosPorEmpresa
import com.castellanoseloy.cataplus.procesos.Utilidades

class Recaudos : Fragment() {

    private var binding: FragmentRecaudosBinding? = null
    var posicionSpinnerVendedor=0
    lateinit var listaIdUsuarios:List<String>
    private lateinit var viewModel: RecaudosViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecaudosBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[RecaudosViewModel::class.java]


        binding?.textViewHasta?.text = Utilidades.obtenerFechaActual()

        buscarTodosUsuariosPorEmpresa("Vendedor").addOnSuccessListener { listaUsuarios->
             crearSpinner(listaUsuarios)
        }

        return binding!!.root
    }

    private fun crearSpinner(listaUsuarios: MutableList<ModeloUsuario>?) {
        val lista: MutableList<String> = mutableListOf()

        if (listaUsuarios.isNullOrEmpty()) {
            // La lista de vendedores está vacía o nula
            // Agregar un mensaje indicando que no hay vendedores registrados
            lista.add("No hay vendedores registrados")
            binding?.buttonInforme?.visibility=View.GONE
        } else {
            listaIdUsuarios = listaUsuarios.map { it.id }!!
            lista.addAll(listaUsuarios.map { it.nombre })
        }

        // Crear un adaptador utilizando la lista
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lista)

        // Especificar el diseño para los elementos desplegables
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Asignar el adaptador al Spinner
        binding?.spinnerVendedor?.adapter = adapter
        adapter.notifyDataSetChanged()
    }


}