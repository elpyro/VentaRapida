package com.example.ventarapida.ui.factura

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils.replace
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.MainActivity
import com.example.ventarapida.MainActivity.Companion.productosSeleccionados
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentFacturaBinding
import com.example.ventarapida.ui.datos.ModeloProducto
import com.example.ventarapida.ui.procesos.OcultarTeclado
import com.example.ventarapida.ui.procesos.Utilidades.eliminarAcentosTildes
import com.example.ventarapida.ui.procesos.Utilidades.formatoMonenda

class Factura : Fragment() {



    private lateinit var viewModel: FacturaViewModel
    var binding: FragmentFacturaBinding? = null
    private lateinit var vista:View
    private lateinit var adapter:FacturaAdaptador
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFacturaBinding.inflate(inflater, container, false)
        return binding!!.root // Retorna la vista inflada
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista= view

        viewModel = ViewModelProvider(this).get(FacturaViewModel::class.java)

        setHasOptionsMenu(true)

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding!!.recyclerViewProductosSeleccionados.layoutManager = gridLayoutManager
        adapter = FacturaAdaptador(MainActivity.productosSeleccionados,viewModel )


        adapter!!.setOnClickItem() { item, cantidad, position ->
            editarItem(item, cantidad, position)
        }

        binding?.recyclerViewProductosSeleccionados?.adapter = adapter

        viewModel.context = requireContext()
        viewModel.calcular_SubTotal()


        observadores()

        listeners()


    }

     fun editarItem(item: ModeloProducto, cantidad: Int,position:Int) {
         val dialogBuilder = AlertDialog.Builder(context)

// Inflar el layout para el diálogo
         val inflater = requireActivity().layoutInflater
         val dialogView = inflater.inflate(R.layout.promt_factura, null)
         dialogBuilder.setView(dialogView)

         val editTextProducto = dialogView.findViewById<EditText>(R.id.promt_producto)
         val editTextCantidad = dialogView.findViewById<EditText>(R.id.promt_cantidad)
         val editTextPrecio = dialogView.findViewById<EditText>(R.id.promt_precio)

         editTextProducto.setText( item.nombre)
         editTextCantidad.setText(cantidad.toString())
         editTextPrecio.setText(item.p_diamante.formatoMonenda())

         editTextPrecio.addTextChangedListener(object : TextWatcher {
                // Este método se llama antes de que el texto cambie
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                // Este método se llama cuando el texto cambia
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Si el texto no está vacío

                }

                // Este método se llama después de que el texto cambia
                override fun afterTextChanged(s: Editable?) {

                }
            })
// Configurar el botón "Aceptar"
         dialogBuilder.setPositiveButton("Cambiar") { dialogInterface, i ->
             val nuevoNombre=editTextProducto.text.toString()
             val nuevaCantidad = editTextCantidad.text.toString()
             val nuevoPrecio = editTextPrecio.text.toString().replace(".", "")

             viewModel.actualizarPrecio(item, nuevoPrecio.toInt(),nuevaCantidad.toInt(), nuevoNombre)
             adapter.notifyDataSetChanged()
         }

// Configurar el botón "Cancelar"
         dialogBuilder.setNegativeButton("Cancelar") { dialogInterface, i ->
             // No hacer nada
         }

// Mostrar el diálogo
         val alertDialog = dialogBuilder.create()
         alertDialog.show()
     }
    private fun listeners() {

        binding!!.editTextEnvio.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            override fun afterTextChanged(p0: Editable?) {
                val envio=binding!!.editTextEnvio.text.toString()

            }

        })

        binding!!.searchViewBuscarSeleccionados.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filtrarProductos(newText)
                }
                return true
            }
        })
        //desbloquea searchview al seleccionarlo
        binding?.searchViewBuscarSeleccionados?.setOnClickListener {
            binding?.searchViewBuscarSeleccionados?.isIconified=false
        }

        binding?.recyclerViewProductosSeleccionados?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    OcultarTeclado(requireContext()).hideKeyboard(vista)
                }
            }
        })
    }

    private fun observadores() {
        viewModel.subTotal.observe(viewLifecycleOwner) {
            binding?.textViewSubTotal?.text=it.toString()
        }

        viewModel.totalFactura.observe(viewLifecycleOwner) {
            binding?.textViewTotal?.text=it.toString()
        }

        viewModel.referencias.observe(viewLifecycleOwner) {
            binding?.textViewReferencias?.text="Referencias: "+it
        }
        viewModel.itemsSeleccionados.observe(viewLifecycleOwner) {
            binding?.textViewItems?.text="Items: "+ it
        }

        viewModel.mensajeToast.observe(viewLifecycleOwner){
            Toast.makeText(context,it,Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detalle_factura, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_confirmar_venta ->{

                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun filtrarProductos(nombreFiltrado: String) {

        val productosFiltrados = productosSeleccionados.filter { it.key.nombre.eliminarAcentosTildes().contains(nombreFiltrado.eliminarAcentosTildes(), ignoreCase = true) }.toMutableMap()
        val adapter = FacturaAdaptador(MainActivity.productosSeleccionados,viewModel)
        binding?.recyclerViewProductosSeleccionados?.adapter = adapter
//        binding?.recyclerViewProductosSeleccionados?.adapter?.setHasStableIds(true)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
    }


}