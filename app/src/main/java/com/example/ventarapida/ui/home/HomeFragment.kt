package com.example.ventarapida.ui.home

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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.MainActivity
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentHomeBinding
import com.example.ventarapida.ui.data.ModeloProducto
import com.example.ventarapida.ui.process.HideKeyboard
import java.util.*


class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private lateinit var productViewModel: HomeViewModel
    private var lista: ArrayList<ModeloProducto>? = null
    private var adapter: VentaProductosAdapter? = null
    private lateinit var vista:View
    private lateinit var menuItem: MenuItem
    val REQUEST_CODE_VOICE_SEARCH = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_venta, menu)
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vista= view

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding!!.recyclerViewProductosVenta.layoutManager = gridLayoutManager

        productViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        productViewModel.context = requireContext()

        productViewModel.calcularTotal()

        productViewModel.totalSeleccionLiveData.observe(viewLifecycleOwner) { productosSeleccionados ->
            binding?.textViewListaSeleccion?.text=productosSeleccionados.toString()
        }



        productViewModel.getProductos().observe(viewLifecycleOwner) { productos ->

            adapter = VentaProductosAdapter(productos, productViewModel)

            adapter!!.setOnLongClickItem { item, position ->
                abriDetalle(item,vista, position)
            }


            lista = productos as ArrayList<ModeloProducto>?
            binding!!.recyclerViewProductosVenta.adapter = adapter
        }

        binding?.searchViewProductosVenta?.setOnClickListener {
            binding?.searchViewProductosVenta?.isIconified=false
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
                    HideKeyboard(requireContext()).hideKeyboard(view)
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
    }

    private fun mensajeEliminar() {

        HideKeyboard(requireContext()).hideKeyboard(vista)

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
                val numerosSeparados= productViewModel.separarNumerosDelString(query.trim())

                if (numerosSeparados.second!=null){
                    cantidadPorVoz= numerosSeparados.second!!.toInt()
                }
                binding?.searchViewProductosVenta?.setQuery(numerosSeparados.first.trim(), true)
            }

        }
    }

    var cantidadPorVoz=0
    private fun filtro(textoParaFiltrar: String) {

        val filtro = lista?.filter { objeto: ModeloProducto ->
            objeto.nombre.lowercase(Locale.getDefault()).contains(textoParaFiltrar.lowercase(Locale.getDefault()))
        }
        val adaptador = filtro?.let { VentaProductosAdapter(it,productViewModel) }
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