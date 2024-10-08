package com.castellanoseloy.cataplus.ui.compra_guardada

import android.app.AlertDialog
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.R
import com.castellanoseloy.cataplus.VistaPDFFacturaOCompra
import com.castellanoseloy.cataplus.databinding.FragmentCompraGuardadaBinding
import com.castellanoseloy.cataplus.datos.ModeloFactura
import com.castellanoseloy.cataplus.datos.ModeloProductoFacturado
import com.castellanoseloy.cataplus.procesos.FirebaseFacturaOCompra
import com.castellanoseloy.cataplus.procesos.Utilidades
import com.castellanoseloy.cataplus.procesos.Utilidades.eliminarAcentosTildes
import com.castellanoseloy.cataplus.procesos.Utilidades.formatoMonenda
import com.castellanoseloy.cataplus.procesos.Utilidades.ocultarTeclado
import com.castellanoseloy.cataplus.ui.promts.PromtFacturaGuardada
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class CompraGuardada : Fragment() {

    private lateinit var viewModel: CompraGuardadaViewModel

    private var binding: FragmentCompraGuardadaBinding? = null
    private lateinit var vista: View
    private lateinit var adaptador: CompraGuardadaAdaptador
    private lateinit var modeloFactura: ModeloFactura
//    var listaDeProductos: List<ModeloFactura> = emptyList<ModeloFactura>()
    private var banderaElimandoFactura: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompraGuardadaBinding.inflate(inflater, container, false)

        // Recibe los productos de la lista del fragmento anterior
        val bundle = arguments
        modeloFactura = (bundle?.getSerializable("modelo") as? ModeloFactura)!!
//        listaDeProductos = (bundle.getSerializable("lista") as? ArrayList<ModeloFactura>)!!

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding?.recyclerViewProductosSeleccionados?.layoutManager = gridLayoutManager

        viewModel = ViewModelProvider(this).get(CompraGuardadaViewModel::class.java)

        viewModel.processDialogo(requireContext())
        viewModel.cargarDatosFactura(modeloFactura)
        viewModel.buscarProductos(modeloFactura.id_pedido)

        observadores()
        listeners()

        //activar  menu para este fragment
        setHasOptionsMenu(true)

        return binding!!.root
    }

    private fun observadores() {
        viewModel.datosFactura.observe(viewLifecycleOwner) { detalleFactura ->
            //actualiza el contenido del modelo actual
            if (detalleFactura != null) {

                modeloFactura = detalleFactura
                //actualizamos los datos del fragmento
                binding?.textViewCliente?.text = "Tienda: " + detalleFactura.nombre

                binding?.textViewVendedor?.text = detalleFactura.nombre_vendedor
                binding?.textViewFecha?.text = detalleFactura.fecha
                binding?.textViewHora?.text = detalleFactura.hora
                binding?.textViewId?.text = detalleFactura.id_pedido.substring(0, 5)

                if(detalleFactura.nombre.equals("Edición de inventario")) binding?.ViewNopermitirEdicion?.visibility=View.VISIBLE
            }

        }

        viewModel.totalFactura.observe(viewLifecycleOwner) {

            if (!banderaElimandoFactura){

                if(binding?.ViewNopermitirEdicion!!.isVisible) return@observe

                binding?.textViewTotal?.text = "Total: ${it.formatoMonenda()}"

                val updates = hashMapOf<String, Any>(
                    "id_pedido" to modeloFactura.id_pedido,
                    "total" to it.formatoMonenda(),
                )
                FirebaseFacturaOCompra.guardarDetalleFacturaOCompra("Compra", updates)
            }


        }

        viewModel.datosProductosComprados.observe(viewLifecycleOwner) { productosComprados ->
            adaptador =
                CompraGuardadaAdaptador(productosComprados as MutableList<ModeloProductoFacturado>)
            binding?.recyclerViewProductosSeleccionados?.adapter = adaptador
            adaptador.setOnClickItem() { item ->

                val promtEditarItem = PromtFacturaGuardada()
                promtEditarItem.editarProducto("compra", item, requireActivity())

            }
            adaptador.setOnClickImangen { item ->
                val bundle = Bundle()
                bundle.putSerializable("modelo", item)
                Navigation.findNavController(vista).navigate(R.id.informacionProducto,bundle)
            }
        }
        viewModel.referencias.observe(viewLifecycleOwner) {
            binding?.textViewReferencias?.text = "Referencias: ${it}"
        }
        viewModel.items.observe(viewLifecycleOwner) {
            binding?.textViewItems?.text = "Items: ${it}"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista = view
    }

    private fun listeners() {

        binding?.tienda?.setOnClickListener {
            val promtEditarDatos = PromtFacturaGuardada()
            promtEditarDatos.promtEditarDatosCompra(modeloFactura, requireActivity())
        }

        binding?.recyclerViewProductosSeleccionados?.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    ocultarTeclado(requireContext(), vista)
                }
            }
        })

        binding!!.searchViewBuscarSeleccionados.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
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
            binding?.searchViewBuscarSeleccionados?.isIconified = false
        }
    }

    fun filtrarProductos(nombreFiltrado: String) {

        val productosFiltrados = viewModel.datosProductosComprados.value?.filter {
            it.producto.eliminarAcentosTildes()
                .contains(nombreFiltrado.eliminarAcentosTildes(), ignoreCase = true)
        }
        adaptador =
            CompraGuardadaAdaptador(productosFiltrados as MutableList<ModeloProductoFacturado>)
        binding?.recyclerViewProductosSeleccionados?.adapter = adaptador


        adaptador.setOnClickItem() { item ->
            val promtEditarItem = PromtFacturaGuardada()
            promtEditarItem.editarProducto("compra", item, requireActivity())
        }
        adaptador.setOnClickImangen { item ->
            val bundle = Bundle()
            bundle.putSerializable("modelo", item)
            Navigation.findNavController(vista).navigate(R.id.informacionProducto,bundle)
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_factura_o_compra_guardada, menu)
        //Todo pendiente agregar el boton ation_agregar_producto
        menu.findItem(R.id.action_agregar_producto).isVisible = false
        menu.findItem(R.id.action_recibo_ingreso_pdf).isVisible = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_agregar_producto -> {
                abrirAgregarProducto()
                return true
            }

            R.id.action_eliminar -> {
                dialogoEliminar()
                return true
            }

            R.id.action_recibo_ingreso_pdf->{
                val intent = Intent(activity, VistaPDFFacturaOCompra::class.java)
                intent.putExtra("id", modeloFactura.id_pedido)
                intent.putExtra("tablaReferencia", "Compra")
                intent.putExtra("reciboIngreso", true)
                startActivity(intent)
                return true
            }

            R.id.action_crear_pdf -> {
                val intent = Intent(activity, VistaPDFFacturaOCompra::class.java)
                intent.putExtra("id", modeloFactura.id_pedido)
                intent.putExtra("tablaReferencia", "Compra")
                startActivity(intent)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun dialogoEliminar() {
        ocultarTeclado(requireContext(),vista)

        // Crear el diálogo de confirmación
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar selección")
        builder.setIcon(R.drawable.logo2_compra_rapidita)
        builder.setMessage("¿Estás seguro de que deseas eliminar esta compra y DESCONTAR los productos?")
        builder.setPositiveButton("Eliminar") { dialog, which ->

            banderaElimandoFactura=true
            DatosPersitidos.progressDialog?.show()


            Utilidades.esperarUnSegundo()
            Utilidades.esperarUnSegundo()

            lifecycleScope.launch {
                viewModel.eliminarCompra(requireContext())
            }

            Toast.makeText(requireContext(),modeloFactura.nombre+"\nFactura Eliminada",Toast.LENGTH_LONG).show()
            findNavController().popBackStack()


        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
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
        viewModel.detenerEscuchadores()
    }

}