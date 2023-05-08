package com.example.ventarapida.ui.ventaPaginaPrincipal

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.*
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.R
import com.example.ventarapida.databinding.VentaBinding
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.example.ventarapida.procesos.Utilidades.ocultarTeclado
import com.example.ventarapida.procesos.Utilidades.separarNumerosDelString
import java.util.*


class Venta : Fragment() {

    private var binding: VentaBinding? = null
    private lateinit var productViewModel: VentaViewModel
    private lateinit var vista:View
    private lateinit var menuItem: MenuItem
    private var lista: ArrayList<ModeloProducto>? = null
    private var adapter: VentaAdaptador? = null
    val REQUEST_CODE_VOICE_SEARCH = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = VentaBinding.inflate(inflater, container, false)
        return binding!!.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_total, menu)
        menuItem  = menu.findItem(R.id.action_total)


        productViewModel.totalCarritoLiveData.observe(viewLifecycleOwner){it->
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_total ->{
                abrirFactura()
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

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding!!.recyclerViewProductosVenta.layoutManager = gridLayoutManager

        productViewModel = ViewModelProvider(this).get(VentaViewModel::class.java)
        productViewModel.context = requireContext()

        observadores()

        listeners()
        productViewModel.calcularTotal()



    }

    private fun listeners() {



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

        binding!!.searchViewProductosVenta.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    private fun observadores() {
        productViewModel.totalSeleccionLiveData.observe(viewLifecycleOwner) { productosSeleccionados ->
            binding?.textViewListaSeleccion?.text=productosSeleccionados.toString()
        }

        productViewModel.getProductos().observe(viewLifecycleOwner) { productos ->

            adapter = VentaAdaptador(productos, productViewModel)

            adapter!!.setOnLongClickItem { item, position ->
                abriDetalle(item,vista, position)
            }


            lista = productos as ArrayList<ModeloProducto>?
            binding!!.recyclerViewProductosVenta.adapter = adapter

            //si el valor esta filtrado buscarlo
            val busqueda = binding?.searchViewProductosVenta?.getQuery().toString()
            if(busqueda!=""){
                filtro(busqueda)
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

    private fun abriDetalle(modeloProducto: ModeloProducto, view:View, position:Int) {
        val bundle = Bundle()
        bundle.putInt("position", position)
        bundle.putSerializable("modelo", modeloProducto)
        bundle.putSerializable("listaProductos", lista)
        Navigation.findNavController(view).navigate(R.id.detalleProducto,bundle)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_VOICE_SEARCH && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = results?.get(0)
            if (query != null) {
                //Separamos los ultimos numeros de el string obtenido por voz
                // para saber si hay un numero y agregar el numero a la seleccion del producto
                val numerosSeparados= separarNumerosDelString(query.trim())

                if (numerosSeparados.second!=null){
                    cantidadPorVoz= numerosSeparados.second!!.toInt()
                }
                binding?.searchViewProductosVenta?.isIconified=false
                binding?.searchViewProductosVenta?.setQuery(numerosSeparados.first.trim(), true)
            }

        }
    }

    var cantidadPorVoz=0
    private fun filtro(textoParaFiltrar: String) {

        val filtro = lista?.filter { objeto: ModeloProducto ->
            objeto.nombre.eliminarAcentosTildes().lowercase(Locale.getDefault()).contains(textoParaFiltrar.eliminarAcentosTildes().lowercase(Locale.getDefault()))
        }
        val adaptador = filtro?.let { VentaAdaptador(it,productViewModel) }
        binding?.recyclerViewProductosVenta?.adapter =adaptador

        if (filtro?.size==1 && cantidadPorVoz!=0){
            productViewModel.actualizarCantidadProducto(filtro[0], cantidadPorVoz)
            cantidadPorVoz=0
        }

            adaptador!!.setOnLongClickItem { item, position ->
            val bundle = Bundle()
            bundle.putSerializable("modelo", item)
            bundle.putInt("position", position)
            val arrayList: ArrayList<ModeloProducto> = filtro.toCollection(ArrayList())
            bundle.putSerializable("listaProductos", arrayList)
            Navigation.findNavController(vista).navigate(R.id.detalleProducto,bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}