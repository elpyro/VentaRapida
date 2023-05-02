package com.example.ventarapida.ui.detalleVenta

import android.app.AlertDialog
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.MainActivity.Companion.ventaProductosSeleccionados
import com.example.ventarapida.R
import com.example.ventarapida.VistaPDF
import com.example.ventarapida.databinding.FragmentDetalleVentaBinding

import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.procesos.PermissionManager
import com.example.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.example.ventarapida.procesos.Utilidades.eliminarPuntosComasLetras
import com.example.ventarapida.procesos.Utilidades.escribirFormatoMoneda
import com.example.ventarapida.procesos.Utilidades.esperarUnSegundo
import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import com.example.ventarapida.procesos.Utilidades.ocultarTeclado

import java.text.SimpleDateFormat
import java.util.*

class DetalleVenta : Fragment() {




    private lateinit var viewModel: DetalleVentaViewModel
    var binding: FragmentDetalleVentaBinding? = null
    private lateinit var vista:View
    private lateinit var adaptador:DetalleVentaAdaptador
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetalleVentaBinding.inflate(inflater, container, false)
        return binding!!.root // Retorna la vista inflada
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista= view

        viewModel = ViewModelProvider(this).get(DetalleVentaViewModel::class.java)

        setHasOptionsMenu(true)

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding!!.recyclerViewProductosSeleccionados.layoutManager = gridLayoutManager
        adaptador = DetalleVentaAdaptador(ventaProductosSeleccionados )


        adaptador.setOnClickItem() { item, cantidad, position ->
            editarItem(item, cantidad)
        }

        binding?.recyclerViewProductosSeleccionados?.adapter = adaptador

        viewModel.context = requireContext()
        viewModel.totalFactura()


        observadores()

        listeners()


    }

     fun editarItem(item: ModeloProducto, cantidad: Int) {
         val dialogBuilder = AlertDialog.Builder(context)

// Inflar el layout para el diálogo
         val inflater = requireActivity().layoutInflater
         val dialogView = inflater.inflate(R.layout.promt_factura, null)
         dialogBuilder.setView(dialogView)

         val editTextProducto = dialogView.findViewById<EditText>(R.id.promt_producto)
         val editTextCantidad = dialogView.findViewById<EditText>(R.id.promt_cantidad)
         val editTextPrecio = dialogView.findViewById<EditText>(R.id.promt_precio)

         // Seleccionar tode el contenido del EditText al recibir foco
         editTextProducto.setSelectAllOnFocus(true)
         editTextCantidad.setSelectAllOnFocus(true)
         editTextPrecio.setSelectAllOnFocus(true)

         editTextProducto.setText( item.nombre)
         editTextCantidad.setText(cantidad.toString())
         editTextPrecio.setText(item.p_diamante.formatoMonenda())

         editTextPrecio.escribirFormatoMoneda()



// Configurar el botón "Aceptar"
         dialogBuilder.setPositiveButton("Cambiar") { dialogInterface, i ->
             val nuevoNombre=editTextProducto.text.toString()
             val nuevaCantidad = editTextCantidad.text.toString()
             val nuevoPrecio = editTextPrecio.text.toString().replace(".", "")

             viewModel.actualizarProducto(item, nuevoPrecio.toInt(),nuevaCantidad.toInt(), nuevoNombre)
             adaptador.notifyDataSetChanged()
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

        binding!!.editTextEnvio.escribirFormatoMoneda()
        binding!!.editDescuento.escribirFormatoMoneda()


        binding?.recyclerViewProductosSeleccionados?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    ocultarTeclado(requireContext(),vista)
                }
            }
        })

        binding!!.editTextEnvio.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Actualiza el valor de envio con el valor del EditText
                if (binding!!.editTextEnvio.text.toString().isNotEmpty()) {
                    viewModel.envio.value = binding!!.editTextEnvio.text.toString().eliminarPuntosComasLetras()
                } else {
                    viewModel.envio.value = "0"
                }
                viewModel.totalFactura()

            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se necesita implementar este método en este caso
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No se necesita implementar este método en este caso
            }
        })

        binding!!.editDescuento.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Actualiza el valor de envio con el valor del EditText
                if (binding!!.editDescuento.text.toString().isNotEmpty()) {
                    viewModel.descuento.value = binding!!.editDescuento.text.toString().eliminarPuntosComasLetras()
                } else {
                    viewModel.descuento.value = "0"
                }
                viewModel.totalFactura()

            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se necesita implementar este método en este caso
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No se necesita implementar este método en este caso
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
                    ocultarTeclado(requireContext(),vista)
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
            binding?.textViewReferencias?.text="Referencias: "+it.formatoMonenda()
        }
        viewModel.itemsSeleccionados.observe(viewLifecycleOwner) {
            binding?.textViewItems?.text="Items: "+ it.formatoMonenda()
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
                ocultarTeclado(requireContext(),vista)

                if(ventaProductosSeleccionados.size<1){
                    Toast.makeText(context,"No hay productos seleccionados",Toast.LENGTH_LONG).show()
                    return true
                }

                val idPedido = UUID.randomUUID().toString()

                val currentTime = Calendar.getInstance().time

                val pattern = "HH:mm:ss"
                val formatoHora = SimpleDateFormat(pattern)
                val horaActual = formatoHora.format(currentTime)

                val formatoFecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val fechaActual = formatoFecha.format(Date())

                val nombre= binding?.editTextNombre?.text.toString().ifBlank { "Anonimo" }
                val envio= binding?.editTextEnvio?.text.toString().ifBlank { "0" }
                val descuento= binding?.editDescuento?.text.toString().ifBlank { "0" }
                val total=binding?.textViewTotal?.text.toString().eliminarPuntosComasLetras()

                val datosPedido = hashMapOf<String, Any>(
                    "id_pedido" to idPedido,
                    "nombre" to nombre,
                    "telefono" to binding?.editTextTelefono?.text.toString(),
                    "documento" to binding?.editTextDocumento?.text.toString(),
                    "direccion" to binding?.editTextDireccion?.text.toString(),
                    "descuento" to descuento.eliminarPuntosComasLetras(),
                    "envio" to envio.eliminarPuntosComasLetras(),
                    "fecha" to fechaActual,
                    "hora" to horaActual,
                    "id_vendedor" to "id_vendedor",
                    "nombre_vendedor" to "nombre_vendedor",
                    "total" to total
                )


                viewModel.subirDatos(datosPedido, ventaProductosSeleccionados)

                //limpiamos los productos seleccionados
                viewModel.limpiarProductosSelecionados(requireContext())

                esperarUnSegundo()

                findNavController().popBackStack()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun filtrarProductos(nombreFiltrado: String) {

        val productosFiltrados = ventaProductosSeleccionados.filter { it.key.nombre.eliminarAcentosTildes().contains(nombreFiltrado.eliminarAcentosTildes(), ignoreCase = true) }.toMutableMap()
        adaptador = DetalleVentaAdaptador(productosFiltrados)
        binding?.recyclerViewProductosSeleccionados?.adapter = adaptador


        adaptador!!.setOnClickItem() { item, cantidad, position ->
            editarItem(item, cantidad)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
    }


}