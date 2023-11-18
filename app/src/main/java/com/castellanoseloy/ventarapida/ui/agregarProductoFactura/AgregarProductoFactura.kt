@file:Suppress("DEPRECATION")

package com.castellanoseloy.ventarapida.ui.agregarProductoFactura

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.MainActivity
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.datos.ModeloFactura
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.castellanoseloy.ventarapida.databinding.FragmentAgregarProductoFacturaBinding

import java.util.*

class AgregarProductoFactura : Fragment() {

    private var binding: FragmentAgregarProductoFacturaBinding? = null
    private lateinit var viewModel: AgregarProductoFacturaViewModel
    private lateinit var menuItem: MenuItem
    private var lista: ArrayList<ModeloProducto>? = null
    private var adapter: AgregarProductoFacturaAdaptador? = null
    var modeloFactura: ModeloFactura? = null
    val REQUEST_CODE_VOICE_SEARCH = 1001

    companion object {
        var productosSeleccionadosAgregar = mutableMapOf<ModeloProducto, Int>()
    }

    private var vista: View? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentAgregarProductoFacturaBinding.inflate(inflater, container, false)

        val bundle = arguments
        modeloFactura = bundle?.getSerializable("modelo") as? ModeloFactura

        return binding!!.root
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_total, menu)
        menuItem = menu.findItem(R.id.action_total)


        viewModel.totalCarritoLiveData.observe(viewLifecycleOwner) {
            val title = SpannableString("Total: $it")
            title.setSpan(
                AbsoluteSizeSpan(20, true), // Tamaño de texto en sp
                0,
                title.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            menuItem.title = title

            super.onCreateOptionsMenu(menu, inflater)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_total -> {
                agregarFactura()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun agregarFactura() {

        Utilidades.ocultarTeclado(requireContext(), vista!!)

        // Crear el diálogo de confirmación
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(modeloFactura!!.nombre)
        builder.setMessage("¿Estás seguro de agregar ${productosSeleccionadosAgregar.size} productos a la factura?")
        builder.setPositiveButton("Agregar") { _, _ ->

            MainActivity.progressDialog?.show()

            viewModel.subirDatos(requireContext(), modeloFactura!!)

            MainActivity.progressDialog?.dismiss()
            Toast.makeText(
                requireContext(),
                "${productosSeleccionadosAgregar.size} Productos Agregados",
                Toast.LENGTH_LONG
            ).show()
            findNavController().popBackStack()
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista = view
        viewModel = ViewModelProvider(this).get(AgregarProductoFacturaViewModel::class.java)

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding!!.recyclerViewProductosVenta.layoutManager = gridLayoutManager

        viewModel = ViewModelProvider(this).get(AgregarProductoFacturaViewModel::class.java)
        viewModel.context = requireContext()

        observadores()

        listeners()
        viewModel.calcularTotal()
    }

    private fun listeners() {

        binding?.imageViewEliminarCarrito?.setOnClickListener {
            mensajeEliminar()
        }

        binding?.imageViewMicrofono?.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                startActivityForResult(intent, REQUEST_CODE_VOICE_SEARCH)
            } else {
                // Si el permiso no ha sido concedido, solicitarlo al usuario
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            }

        }


        binding?.recyclerViewProductosVenta?.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    Utilidades.ocultarTeclado(requireContext(), vista!!)

                }
            }
        })

        binding?.searchViewProductosVenta?.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
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
        binding?.searchViewProductosVenta?.setOnClickListener {
            binding?.searchViewProductosVenta?.isIconified = false
        }
    }

    private fun observadores() {
        viewModel.totalSeleccionLiveData.observe(viewLifecycleOwner) { productosSeleccionados ->
            binding?.textViewListaSeleccion?.text = productosSeleccionados.toString()
        }

        viewModel.getProductos().observe(viewLifecycleOwner) { productos ->

            val productosOrdenados = productos.sortedBy { it.nombre }
            adapter = AgregarProductoFacturaAdaptador(productosOrdenados, viewModel)

            lista = productos as ArrayList<ModeloProducto>?
            binding!!.recyclerViewProductosVenta.adapter = adapter
        }
    }

    private fun mensajeEliminar() {

        Utilidades.ocultarTeclado(requireContext(), vista!!)

        // Crear el diálogo de confirmación
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar selección")
        builder.setMessage("¿Estás seguro de que deseas eliminar los productos seleccionados?")
        builder.setPositiveButton("Eliminar") { _, _ ->
            viewModel.eliminarCarrito()
            binding?.recyclerViewProductosVenta?.adapter = adapter
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_VOICE_SEARCH && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = results?.get(0)
            if (query != null) {
                binding?.searchViewProductosVenta?.isIconified = false
                binding?.searchViewProductosVenta?.setQuery(query, true)
            }

        }
    }


    private fun filtro(textoParaFiltrar: String) {

        val filtro = lista?.filter { objeto: ModeloProducto ->
            objeto.nombre.eliminarAcentosTildes().lowercase(Locale.getDefault()).contains(
                textoParaFiltrar.eliminarAcentosTildes().lowercase(
                    Locale.getDefault()
                )
            )
        }
        val productosOrdenados = filtro?.sortedBy { it.nombre }
        val adaptador = productosOrdenados?.let { AgregarProductoFacturaAdaptador(it, viewModel) }
        binding?.recyclerViewProductosVenta?.adapter = adaptador

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        productosSeleccionadosAgregar.clear()
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
    }
}