package com.castellanoseloy.ventarapida.ui.ventaPaginaPrincipal

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.datosEmpresa
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.datosUsuario
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.VistaPDFReporte
import com.castellanoseloy.ventarapida.databinding.VentaBinding
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.castellanoseloy.ventarapida.procesos.Utilidades.ocultarTeclado
import com.castellanoseloy.ventarapida.procesos.crearPdf.CrearPdfCatalogo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*


class Venta : Fragment() {

    private var primeraCarga: Boolean=true
    private var binding: VentaBinding? = null
    private lateinit var productViewModel: VentaViewModel
    private lateinit var vista:View
    private lateinit var menuItem: MenuItem
    private lateinit var menuPremium: MenuItem
    private var lista: ArrayList<ModeloProducto>? = null
    private var filtro: ArrayList<ModeloProducto>? = null
    private var adapter: VentaAdaptador? = null
    val REQUEST_CODE_VOICE_SEARCH = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = VentaBinding.inflate(inflater, container, false)

        return binding!!.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_total, menu)

        menuItem  = menu.findItem(R.id.action_total)
        menuPremium  = menu.findItem(R.id.action_premium)

        if(DatosPersitidos.planVencido!!){//bloque el boton si el usuario no es el dueño de la cuenta
            if(datosUsuario.id != datosEmpresa.idDuenoCuenta) {
                menuItem.isVisible = false
                menuPremium.isVisible = true
            }
        }



        productViewModel.totalCarritoLiveData.observe(viewLifecycleOwner){it->

            val title = SpannableString("Carrito: $it")
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_total ->{
                abrirFactura()
                return true
            }
            R.id.action_premium ->{
                Navigation.findNavController(vista).navigate(R.id.suscripcionesDisponibles)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun abrirFactura() {

        Navigation.findNavController(vista).navigate(R.id.factura)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vista= view


        setHasOptionsMenu(true)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding?.recyclerViewProductosVenta?.layoutManager = gridLayoutManager

        productViewModel = ViewModelProvider(this).get(VentaViewModel::class.java)
        productViewModel.context = requireContext()

        observadores()
        actualizarLista()
        Log.d("pruebas","id empresa ${DatosPersitidos.datosEmpresa.id}")
        productViewModel.calcularTotal()
        listeners()
    }

    private fun listeners() {
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            actualizarLista()
        }

        binding?.imageViewMostrarPDF?.setOnClickListener{
            varidarDatosPDf()
        }

        binding?.imageViewEliminarCarrito?.setOnClickListener {
            mensajeEliminar()
        }

        binding?.imageViewMicrofono?.setOnClickListener {

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                startActivityForResult(intent, REQUEST_CODE_VOICE_SEARCH)
            } else {
                // Si el permiso no ha sido concedido, solicitarlo al usuario
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            }

        }


        binding?.recyclerViewProductosVenta?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    ocultarTeclado(requireContext(),vista)
                    
                }
            }
        })

        binding?.searchViewProductosVenta?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
            binding?.searchViewProductosVenta?.isIconified=false
        }

    }

    private fun varidarDatosPDf() {
        ocultarTeclado(requireContext(),vista)
        if(filtro ==null){
            Toast.makeText(context, "No hay registros disponibles", Toast.LENGTH_LONG)
                .show()
        }else{
            Log.d("Reportes","tamaño del filtro:${filtro!!.size}" )
            if(filtro!!.size>0){
                progressDialog.show()
                lifecycleScope.launch(Dispatchers.IO) {

                    crearCatalogo()
                }
            }else{
                Toast.makeText(context, "No hay registros disponibles", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun crearCatalogo( ) {
                    runBlocking {
                        val crearPdf = CrearPdfCatalogo()
                        crearPdf.catalogo(
                            requireContext(),
                            filtro as ArrayList<ModeloProducto>
                        )

                        progressDialog.dismiss()
                        val intent = Intent(requireContext(), VistaPDFReporte::class.java)
                        startActivity(intent)
                    }

    }

    private fun observadores() {
        productViewModel.totalSeleccionLiveData.observe(viewLifecycleOwner) { productosSeleccionados ->
            binding?.textViewListaSeleccion?.text=productosSeleccionados.toString()
        }
    }

    fun actualizarLista(){

        productViewModel.obtenerProductos().observe(viewLifecycleOwner) { productos ->
                if (productos.isNotEmpty()) {
                    binding?.LinearLayoutPantallaPrincipal?.visibility=View.VISIBLE
                    binding?.LinearLayoutPantallaBienvenida?.visibility=View.GONE
                        val productosOrdenados = productos?.sortedBy { it.nombre }
                        lista = productos as ArrayList<ModeloProducto>?
                        filtro = lista
                        if (primeraCarga) {
                            adapter = VentaAdaptador(productosOrdenados!!, productViewModel)
                            binding?.recyclerViewProductosVenta?.adapter = adapter
                            primeraCarga = false
                        } else {
                            adapter!!.updateData(productosOrdenados ?: emptyList())
                        }
                        adapter!!.setOnLongClickItem { item, position ->
                            abriDetalle(item, vista)
                        }

                        //si el valor esta filtrado buscarlo
                        val busqueda = binding?.searchViewProductosVenta?.getQuery().toString()
                        if (busqueda != "") {
                            filtro(busqueda)
                        }
                        binding?.swipeRefreshLayout?.isRefreshing = false

                } else {
                    binding?.LinearLayoutPantallaBienvenida?.visibility=View.VISIBLE
                    binding?.LinearLayoutPantallaPrincipal?.visibility=View.GONE
                }
            }


    }

    private fun mensajeEliminar() {

        ocultarTeclado(requireContext(),vista)

        // Crear el diálogo de confirmación
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar selección")
        builder.setMessage("¿Estás seguro de que deseas eliminar los productos seleccionados?")
        builder.setPositiveButton("Eliminar") { dialog, which ->
            productViewModel.eliminarCarrito()
            binding?.recyclerViewProductosVenta?.adapter=adapter
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun abriDetalle(modeloProducto: ModeloProducto, view:View) {
        val bundle = Bundle()
        bundle.putSerializable("modelo", modeloProducto)
        Navigation.findNavController(view).navigate(R.id.informacionProducto,bundle)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_VOICE_SEARCH && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = results?.get(0)
            if (query != null) {
                binding?.searchViewProductosVenta?.isIconified=false
                binding?.searchViewProductosVenta?.setQuery(query, true)
            }

        }
    }



    private fun filtro(textoParaFiltrar: String) {

        filtro = lista?.filter { objeto: ModeloProducto ->
            objeto.nombre.eliminarAcentosTildes().lowercase(Locale.getDefault()).contains(textoParaFiltrar.eliminarAcentosTildes().lowercase(Locale.getDefault()))
        } as ArrayList<ModeloProducto>?
        val filtroOrdenado = filtro?.sortedBy { it.nombre }
        adapter = filtroOrdenado?.let { VentaAdaptador(it,productViewModel) }
        binding?.recyclerViewProductosVenta?.adapter =adapter

        adapter?.setOnLongClickItem { item, position ->
                abriDetalle(item,vista)
        }
    }

    val progressDialog: ProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage("Creando PDF de productos filtrados...")
            setCancelable(true)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}