package com.example.ve

import com.example.ventarapida.ui.detalleProducto.DetalleProductoViewModel



import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.ventarapida.databinding.FragmentDetalleProductoBinding

import com.example.ventarapida.ui.data.ModeloProducto
import com.example.ventarapida.ui.process.HideKeyboard
import com.google.firebase.FirebaseApp
import com.squareup.picasso.Picasso

class DetalleProducto : Fragment() {
    companion object {
        // Creación de una instancia de DetalleProducto
        fun newInstance() = DetalleProducto()
    }

    private var binding: FragmentDetalleProductoBinding? = null
    private val viewModel: DetalleProductoViewModel by viewModels() // Inicialización de viewModel
    private lateinit var productosViewModel: DetalleProductoViewModel
    private var vista: View? = null
    private lateinit var id_producto:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento usando el binding
        binding = FragmentDetalleProductoBinding.inflate(inflater, container, false)

        // Inicialización de Firebase
        FirebaseApp.initializeApp(requireContext())

        // Recibe los productos de la lista del fragmento anterior
        val bundle = arguments
        val modeloProducto = bundle?.getSerializable("modelo") as? ModeloProducto
        val listaDeProductos = bundle?.getSerializable("listaProductos") as? ArrayList<ModeloProducto>
        val posicionProducto = bundle?.getInt("position")

        // Llama al método actualizarListaProductos para indicar la lista de productos
        viewModel.actualizarListaProductos(listaDeProductos!!)

        // Inicialización de productosViewModel
        productosViewModel = ViewModelProvider(this).get(DetalleProductoViewModel::class.java)

        // Observa los cambios en detalleProducto y actualiza la UI en consecuencia
        productosViewModel.detalleProducto.observe(viewLifecycleOwner, Observer { detalleProducto ->
            if (detalleProducto.isNotEmpty()) {
                val producto = detalleProducto[0]
                actualizarCampos(producto)
            }else{
                cargarSiguienteProducto()
            }
        })
        productosViewModel.mensajeToast.observe(viewLifecycleOwner) { it
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        binding?.imageViewElimniarProducto?.setOnClickListener {


            viewModel.eliminarProducto(id_producto)

        }

        // Define el botón "Siguiente" y configura su OnClickListener
        binding?.imageViewBotonDerecha?.setOnClickListener {
            cargarSiguienteProducto()
        }

        // Define el botón "Anterior" y configura su OnClickListener
        binding?.imageViewBotonIzquierda?.setOnClickListener {
            cargarAnteriorProducto()
        }

        //Define el botón "Guardar"
        binding?.imageViewGuardar?.setOnClickListener {

            val updates = hashMapOf<String, Any>(
                "id" to id_producto.toString().trim(),
                "nombre" to binding!!.editTextProducto.text.toString().trim(),
                "cantidad" to binding!!.editTextCantidad.text.toString().trim(),
                "p_compra" to binding!!.editTextPCompra.text.toString().trim(),
                "p_diamante" to binding!!.editTextPVenta.text.toString().trim()
            )
            viewModel.guardarProducto(updates)

        }
        // Indica la posición del producto para abrir el producto seleccionado
        viewModel.actualizarPosiscion(posicionProducto!!)
        verificarPosiciones()

        // Carga el producto en la UI
        cargarProducto(modeloProducto)

        return binding!!.root // Retorna la vista inflada
    }

    private fun actualizarCampos(producto: ModeloProducto) {
        id_producto=producto.id
        binding?.editTextProducto?.setText(producto.nombre)
        binding?.editTextPCompra?.setText(producto.p_compra)
        binding?.editTextPVenta?.setText(producto.p_diamante)
        binding?.editTextCantidad?.setText(producto.cantidad)
       if (!producto.url.isEmpty()) Picasso.get().load(producto.url)
            .into(binding?.imageViewFoto)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista = view // Guarda la vista en la variable vista
    }

    private fun verificarPosiciones() {
        //Verifica la posicion del array para mostrar o ocultar los botones de siguiente o anterior
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
        viewModel.setIdProducto(modeloProducto!!.id)
    }


    }