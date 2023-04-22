package com.example.ventarapida.ui.factura_guardada

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentFacturaGuardadaBinding
import com.example.ventarapida.datos.ModeloFactura
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.FirebaseProductoFacturados.eliminarProductoFacturado
import com.example.ventarapida.procesos.FirebaseProductoFacturados.guardarProductoFacturado
import com.example.ventarapida.procesos.FirebaseProductos
import com.example.ventarapida.procesos.OcultarTeclado
import com.example.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.example.ventarapida.procesos.Utilidades.escribirFormatoMoneda
import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import com.example.ventarapida.procesos.UtilidadesBaseDatos

class FacturaGuardada : Fragment() {

    private lateinit var viewModel: FacturaGuardadaViewModel

    private var binding: FragmentFacturaGuardadaBinding? = null
    private lateinit var vista:View
    private lateinit var adaptador: FacturaGuardadaAdaptador
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentFacturaGuardadaBinding.inflate(inflater, container, false)

        // Recibe los productos de la lista del fragmento anterior
        val bundle = arguments
        val modeloFactura = bundle?.getSerializable("modelo") as? ModeloFactura

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding?.recyclerViewProductosFacturados?.layoutManager = gridLayoutManager

        viewModel = ViewModelProvider(this).get(FacturaGuardadaViewModel::class.java)

        viewModel.cargarDatosFactura(modeloFactura)

        observadores()
        listeners()
        return binding!!.root
    }

    override fun onResume() {
        super.onResume()


    }

    private fun observadores() {
        viewModel.datosFactura.observe(viewLifecycleOwner) { detalleFactura ->
            binding?.textViewCliente?.text = "Cliente: " + detalleFactura.nombre
            binding?.textViewTelefono?.text = "Tel: "+ detalleFactura.telefono
            binding?.textViewDocumento?.text = "C.I: "+detalleFactura.documento
            binding?.textViewDireccion?.text = "Dirección: " + detalleFactura.direccion
            binding?.textViewDescuento?.text = "Descuento: %"+detalleFactura.descuento.formatoMonenda()
            binding?.textViewEnvio?.text = "Envio: "+detalleFactura.envio.formatoMonenda()
            binding?.textViewVendedor?.text= detalleFactura.nombre_vendedor
            binding?.textViewFecha?.text=detalleFactura.fecha
            binding?.textViewHora?.text=detalleFactura.hora
            binding?.textViewId?.text=detalleFactura.id_pedido.substring(0, 5)
        }

        viewModel.totalFactura.observe(viewLifecycleOwner){
            binding?.textViewTotal?.text="Total: $it"
        }
        viewModel.subTotal.observe(viewLifecycleOwner){
            binding?.textViewSubtotal?.text="Sub-Total: $it"
        }
        viewModel.datosProductosFacturados.observe(viewLifecycleOwner) { productosFacturados ->
            adaptador = FacturaGuardadaAdaptador(productosFacturados as MutableList<ModeloProductoFacturado>)
            binding?.recyclerViewProductosFacturados?.adapter = adaptador
            adaptador!!.setOnClickItem() { item ->
                editarItem(item)
            }
        }
        viewModel.referencias.observe(viewLifecycleOwner){
            binding?.textViewReferencias?.text="Referencias: $it"
        }
        viewModel.items.observe(viewLifecycleOwner){
            binding?.textViewItems?.text="Items: $it"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view
    }

    private fun listeners() {

        binding?.cardViewCliente?.setOnClickListener {

        }

        binding?.recyclerViewProductosFacturados?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    OcultarTeclado(requireContext()).hideKeyboard(vista)
                }
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
    }

    fun filtrarProductos(nombreFiltrado: String) {

        val productosFiltrados = viewModel.datosProductosFacturados.value?.filter { it.producto.eliminarAcentosTildes().contains(nombreFiltrado.eliminarAcentosTildes(), ignoreCase = true) }
        adaptador = FacturaGuardadaAdaptador(productosFiltrados as MutableList<ModeloProductoFacturado>)
        binding?.recyclerViewProductosFacturados?.adapter = adaptador


        adaptador!!.setOnClickItem() { item ->
            editarItem(item)
        }

    }

    fun editarItem(item: ModeloProductoFacturado) {

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

        editTextProducto.setText( item.producto)
        editTextCantidad.setText(item.cantidad)
        editTextPrecio.setText(item.venta.formatoMonenda())

        editTextPrecio.escribirFormatoMoneda()



// Configurar el botón "Aceptar"
        dialogBuilder.setPositiveButton("Cambiar") { dialogInterface, i ->

            val nuevoNombre=editTextProducto.text.toString()
            val nuevaCantidad = editTextCantidad.text.toString()
            val nuevoPrecio = editTextPrecio.text.toString().replace(".", "")

            val cantidadAnterior = item.cantidad

            item.producto=nuevoNombre
            item.cantidad=nuevaCantidad
            item.venta=nuevoPrecio


            val diferenciaCantidad = nuevaCantidad.toInt() - cantidadAnterior.toInt()

            if(diferenciaCantidad!=0){
                //hacer una cola para restar o sumar las cantidades del inventario
                val productosSeleccionados: MutableMap<ModeloProducto, Int> = mutableMapOf()
                val nuevoProducto = ModeloProducto(id = item.id_producto)
                productosSeleccionados[nuevoProducto] = diferenciaCantidad

                UtilidadesBaseDatos.guardarTransaccionesBd(context, productosSeleccionados)
                val transaccionesPendientes =
                    UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos(context)
                FirebaseProductos.transaccionesCambiarCantidad(context, transaccionesPendientes)

            }
            val listaProductosFacturados = arrayListOf<ModeloProductoFacturado>()
            listaProductosFacturados.add(item)
            if(nuevaCantidad.toInt()!=0){
                guardarProductoFacturado(listaProductosFacturados)
            }else{
                eliminarProductoFacturado(listaProductosFacturados)
                Toast.makeText(context, cantidadAnterior.toString()+"x "+item.producto+" Eliminados", Toast.LENGTH_LONG).show()
            }


        }

// Configurar el botón "Cancelar"
        dialogBuilder.setNegativeButton("Cancelar") { dialogInterface, i ->
            // No hacer nada
        }

// Mostrar el diálogo
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}