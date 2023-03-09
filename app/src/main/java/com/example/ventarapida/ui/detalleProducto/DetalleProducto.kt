package com.example.ventarapida.ui.detalleProducto

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentDetalleProductoBinding
import com.example.ventarapida.databinding.FragmentHomeBinding
import com.example.ventarapida.ui.data.ModeloProducto
import com.example.ventarapida.ui.home.HomeViewModel
import com.example.ventarapida.ui.process.HideKeyboard
import com.squareup.picasso.Picasso

class DetalleProducto : Fragment() {

    companion object {
        fun newInstance() = DetalleProducto()
    }
    private var binding: FragmentDetalleProductoBinding? = null
    private val viewModel: DetalleProductoViewModel by viewModels()
    private var vista:View?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDetalleProductoBinding.inflate(inflater, container, false)

        val bundle = arguments
        val modeloProducto = bundle?.getSerializable("modelo") as? ModeloProducto
        val listaDeProductos=bundle?.getSerializable("listaProductos") as? ArrayList<ModeloProducto>
        val posicionProducto=bundle?.getInt("position")

        // Llamar a actualizarListaProductos para indicar la lista de productos
        viewModel.actualizarListaProductos(listaDeProductos!!)
        viewModel.actualizarPosiscion(posicionProducto!!)

        // Define el botón "Siguiente" y configura su OnClickListener
        binding?.imageViewBotonDerecha?.setOnClickListener {
        cargarSiguienteProducto()
    }
        binding?.imageViewBotonIzquierda?.setOnClickListener {
            cargarAnteriorProducto()
        }

        verificarPosiciones()

        cargarProducto(modeloProducto)



        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista= view

    }

    private fun verificarPosiciones() {
        if ( viewModel.posicionActual == 0) {
            binding?.imageViewBotonIzquierda?.visibility = View.INVISIBLE
        }else{
            binding?.imageViewBotonIzquierda?.visibility = View.VISIBLE
        }
        if ( viewModel.posicionActual == viewModel.listaProductos.size-1) {
            binding?.imageViewBotonDerecha?.visibility = View.INVISIBLE
        }else{
            binding?.imageViewBotonDerecha?.visibility = View.VISIBLE
        }
    }

    private fun cargarAnteriorProducto() {
        // Descremente la posición actual en 1
        viewModel.posicionActual--

        // Si hemos llegado al final de la lista, volvemos al principio
        if (viewModel.posicionActual >= viewModel.listaProductos.size) {
            viewModel.posicionActual = 0
        }

        // Obtenemos el siguiente modelo de producto de la lista
        val siguienteModeloProducto = viewModel.listaProductos[viewModel.posicionActual]

        // Actualizamos el fragmento con los detalles del siguiente producto
        cargarProducto(siguienteModeloProducto)
        verificarPosiciones()
        HideKeyboard(requireContext()).hideKeyboard(vista!!)
    }

    private fun cargarSiguienteProducto() {
        // Incrementa la posición actual en 1
        viewModel.posicionActual++

        // Si hemos llegado al final de la lista, volvemos al principio
        if (viewModel.posicionActual >= viewModel.listaProductos.size) {
            viewModel.posicionActual = 0
        }

        // Obtenemos el siguiente modelo de producto de la lista
        val siguienteModeloProducto = viewModel.listaProductos[viewModel.posicionActual]

        // Actualizamos el fragmento con los detalles del siguiente producto
        cargarProducto(siguienteModeloProducto)
        verificarPosiciones()
        HideKeyboard(requireContext()).hideKeyboard(vista!!)
    }

    private fun cargarProducto(modeloProducto: ModeloProducto?) {
        binding?.editTextProducto?.setText(modeloProducto!!.nombre)
        binding!!.editTextPCompra.setText(modeloProducto!!.p_compra)
        binding!!.editTextPVenta.setText(modeloProducto.p_diamante)
        binding?.editTextCantidad?.setText(modeloProducto.cantidad)
        Picasso.get().load( modeloProducto.url)
            .into(binding?.imageViewFoto)
    }





    }