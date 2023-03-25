package com.example.ventarapida.ui.home

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SearchView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.R
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
    private lateinit var vista:View
    private lateinit var menuItem: MenuItem
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

        productViewModel.totalSeleccionLiveData.observe(viewLifecycleOwner) { productosSeleccionados ->
            binding?.textViewListaSeleccion?.text=productosSeleccionados.toString()
        }


        productViewModel.getProductos().observe(viewLifecycleOwner) { productos ->

            adapter = ProductAdapter(productos, productViewModel)

            adapter!!.setOnLongClickItem { item, position ->
                abriDetalle(item,vista, position)
            }

            //Todo moviemiento pendiente
//            adapter?.setOnClickItem {  posicion , itemView->
//
//                val cartLocation = IntArray(2)
//                binding?.imageViewEliminarCarrito?.getLocationOnScreen(cartLocation)
//                // create bitmap screen capture
//
//                // create bitmap screen capture
//                val viewCopy = itemView
//
//
//                // Obtener la posición inicial de la vista original
//                val originalLocation = IntArray(2)
//                itemView.getLocationOnScreen(originalLocation)
//
//                // Establecer la posición inicial de la vista de copia
//                viewCopy.translationX = originalLocation[0].toFloat()
//                viewCopy.translationY = originalLocation[1].toFloat()
//
//                val itemLocation = IntArray(2)
//                viewCopy.getLocationOnScreen(itemLocation)
//
//                val animator = ValueAnimator.ofFloat(0f, 1f)
//
//                animator.duration = 600
//                animator.interpolator = AccelerateDecelerateInterpolator()
//
//                animator.addUpdateListener { animation ->
//                    val value = animation.animatedValue as Float
//
//                    val x = itemLocation[0] + (cartLocation[0] - itemLocation[0]) * value
//                    val y = itemLocation[1] + (cartLocation[1] - itemLocation[1]) * value
//                    viewCopy.translationX = x - itemLocation[0]
//                    viewCopy.translationY = y - itemLocation[1]
//
//                    // Añadir propiedad de escala para reducir el tamaño del elemento a medida que se mueve
//                    val scale = 1 - (1 - value) * 0.5f // Ajusta 0.5f para cambiar la cantidad de reducción
//                    viewCopy.scaleX = scale
//                    viewCopy.scaleY = scale
//                }
//                animator.doOnEnd {
//                    viewCopy.visibility=View.GONE
//                }
//
//                animator.start()
//
//            }



            lista = productos as ArrayList<ModeloProducto>?
            binding!!.recyclerViewProductosVenta.adapter = adapter
        }



        binding?.imageViewEliminarCarrito?.setOnClickListener {

            mensajeEliminar()
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
            adapter?.notifyDataSetChanged()
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


    private fun filtro(valor: String) {

        val filtro = lista?.filter { objeto: ModeloProducto ->
            objeto.nombre.lowercase(Locale.getDefault()).contains(valor.lowercase(Locale.getDefault()))
        }
        val adaptador = filtro?.let { ProductAdapter(it,productViewModel) }
        binding?.recyclerViewProductosVenta?.adapter =adaptador

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