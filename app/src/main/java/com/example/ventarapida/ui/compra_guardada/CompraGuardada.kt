package com.example.ventarapida.ui.compra_guardada

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.SearchView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentCompraGuardadaBinding
import com.example.ventarapida.databinding.FragmentFacturaGuardadaBinding
import com.example.ventarapida.datos.ModeloFactura
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.FirebaseFactura
import com.example.ventarapida.procesos.FirebaseFactura.eliminarFactura
import com.example.ventarapida.procesos.FirebaseProductoFacturados.eliminarProductoFacturado
import com.example.ventarapida.procesos.FirebaseProductos
import com.example.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.example.ventarapida.procesos.Utilidades.ocultarTeclado
import com.example.ventarapida.procesos.UtilidadesBaseDatos
import com.example.ventarapida.ui.promts.PromtFacturaGuardada

class CompraGuardada : Fragment() {

    private lateinit var viewModel: CompraGuardadaViewModel

    private var binding: FragmentCompraGuardadaBinding? = null
    private lateinit var vista:View
    private lateinit var adaptador: CompraGuardadaAdaptador
    private lateinit var  modeloFactura: ModeloFactura
    private  var banderaElimandoFactura :Boolean=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentCompraGuardadaBinding.inflate(inflater, container, false)

        //activar  menu para este fragment
        setHasOptionsMenu(true)
        
        // Recibe los productos de la lista del fragmento anterior
        val bundle = arguments
        modeloFactura= (bundle?.getSerializable("modelo") as? ModeloFactura)!!

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding?.recyclerViewProductosSeleccionados?.layoutManager = gridLayoutManager

        viewModel = ViewModelProvider(this).get(CompraGuardadaViewModel::class.java)

        viewModel.cargarDatosFactura(modeloFactura)

        observadores()
        listeners()
        return binding!!.root
    }



    private fun observadores() {
        viewModel.datosFactura.observe(viewLifecycleOwner) { detalleFactura ->
            //actualiza el contenido del modelo actual
            if(detalleFactura!=null){
                modeloFactura=detalleFactura
                //actualizamos los datos del fragmento
                binding?.textViewCliente?.text = "Tienda: " + detalleFactura.nombre

                binding?.textViewVendedor?.text= detalleFactura.nombre_vendedor
                binding?.textViewFecha?.text=detalleFactura.fecha
                binding?.textViewHora?.text=detalleFactura.hora
                binding?.textViewId?.text=detalleFactura.id_pedido.substring(0, 5)
            }

        }

        viewModel.totalFactura.observe(viewLifecycleOwner){

            if (banderaElimandoFactura==true) return@observe

            binding?.textViewTotal?.text="Total: $it"
            val updates = hashMapOf<String, Any>(
                "id_pedido" to modeloFactura.id_pedido,
                "total" to it.eliminarAcentosTildes(),
            )
            FirebaseFactura.guardarFactura("Compra",updates)
        }

        viewModel.datosProductosComprados.observe(viewLifecycleOwner) { productosComprados ->
            adaptador = CompraGuardadaAdaptador(productosComprados as MutableList<ModeloProductoFacturado>)
            binding?.recyclerViewProductosSeleccionados?.adapter = adaptador
            adaptador!!.setOnClickItem() { item ->

                val promtEditarItem=PromtFacturaGuardada()
                promtEditarItem.editarProducto("compra",item,requireActivity())

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

        binding?.tienda?.setOnClickListener {
            val promtEditarDatos=PromtFacturaGuardada()
            promtEditarDatos.promtEditarDatosCompra(modeloFactura,requireActivity())
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

        val productosFiltrados = viewModel.datosProductosComprados.value?.filter { it.producto.eliminarAcentosTildes().contains(nombreFiltrado.eliminarAcentosTildes(), ignoreCase = true) }
        adaptador = CompraGuardadaAdaptador(productosFiltrados as MutableList<ModeloProductoFacturado>)
        binding?.recyclerViewProductosSeleccionados?.adapter = adaptador


        adaptador!!.setOnClickItem() { item ->
            val promtEditarItem=PromtFacturaGuardada()
            promtEditarItem.editarProducto("compra",item,requireActivity())
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_factura_guardada, menu)
        //Todo pendiente agregar el boton ation_agregar_producto
        var menuItem: MenuItem  = menu.findItem(R.id.action_agregar_producto).setVisible(false)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_agregar_producto->{
                abrirAgregarProducto()
                return true
            }

            R.id.action_eliminar -> {

                ocultarTeclado(requireContext(),vista)

                // Crear el diálogo de confirmación
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Eliminar selección")
                builder.setMessage("¿Estás seguro de que deseas eliminar esta compra y DESCONTAR los productos?")
                builder.setPositiveButton("Eliminar") { dialog, which ->

                    banderaElimandoFactura=true

                    val arrayListProductosFacturados = ArrayList(viewModel.datosProductosComprados.value ?: emptyList())
                    eliminarProductoFacturado("ProductosComprados", arrayListProductosFacturados)

                    //Restar cantidades de la factura
                    val productosSeleccionados = mutableMapOf<ModeloProducto, Int>()

                    viewModel.datosProductosComprados.value?.forEach { productoFacturado ->
                        val producto = ModeloProducto(
                            id = productoFacturado.id_producto
                        )
                        val cantidad = -1 * ( productoFacturado.cantidad.toInt())
                        productosSeleccionados[producto] = cantidad
                    }

                    //crear cola de transacciones para restar
                    UtilidadesBaseDatos.guardarTransaccionesBd("compra",context, productosSeleccionados)
                    val transaccionesPendientes =
                        UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos(context)
                    FirebaseProductos.transaccionesCambiarCantidad(context, transaccionesPendientes)

                    eliminarFactura("Compra",modeloFactura.id_pedido)

                    Toast.makeText(requireContext(),modeloFactura.nombre+"\nFactura Eliminada",Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()


                }
                builder.setNegativeButton("Cancelar", null)
                builder.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun abrirAgregarProducto() {
        val bundle = Bundle()

        bundle.putSerializable("modelo", modeloFactura)

        Navigation.findNavController(vista).navigate(R.id.agregarProductoFactura,bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
    }

}