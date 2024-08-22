package com.castellanoseloy.ventarapida.ui.factura_guardada

import android.app.AlertDialog
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.VistaPDFFacturaOCompra
import com.castellanoseloy.ventarapida.databinding.FragmentFacturaGuardadaBinding
import com.castellanoseloy.ventarapida.datos.ModeloFactura
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.procesos.FirebaseFacturaOCompra
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.castellanoseloy.ventarapida.procesos.Utilidades.formatoMonenda
import com.castellanoseloy.ventarapida.procesos.Utilidades.ocultarTeclado
import com.castellanoseloy.ventarapida.ui.promts.PromtFacturaGuardada

@Suppress("DEPRECATION")
class FacturaGuardada : Fragment() {

    private var menuAgregar: MenuItem? = null
    private lateinit var viewModel: FacturaGuardadaViewModel
    private var binding: FragmentFacturaGuardadaBinding? = null
    private lateinit var vista:View
    private lateinit var adaptador: FacturaGuardadaAdaptador
    private lateinit var  modeloFactura: ModeloFactura
    private  var banderaElimandoFactura :Boolean=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentFacturaGuardadaBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        // Recibe los productos de la lista del fragmento anterior
        val bundle = arguments
        modeloFactura= (bundle?.getSerializable("modelo") as? ModeloFactura)!!

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding?.recyclerViewProductosFacturados?.layoutManager = gridLayoutManager

        viewModel = ViewModelProvider(this).get(FacturaGuardadaViewModel::class.java)

        viewModel.processDialogo(requireContext())
        viewModel.cargarDatosFactura(modeloFactura)
        viewModel.buscarProductos(modeloFactura.id_pedido)

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

                if(detalleFactura.nombre.equals("Edición de inventario")){
                    binding?.ViewNopermitirEdicion?.visibility=View.VISIBLE
                    menuAgregar?.isVisible=false
                }
                viewModel.calcularTotal()
            }
        }

        viewModel.totalFactura.observe(viewLifecycleOwner){
            if (!banderaElimandoFactura){

                if(binding?.ViewNopermitirEdicion!!.isVisible) return@observe

                binding?.textViewTotal?.text="Total: $it"

                val updates = hashMapOf<String, Any>(
                    "id_pedido" to modeloFactura.id_pedido,
                    "total" to it
                )
                FirebaseFacturaOCompra.guardarDetalleFacturaOCompra("Factura",updates)
            }



        }
        viewModel.subTotal.observe(viewLifecycleOwner){
            binding?.textViewSubtotal?.text="Sub-Total: ${it.formatoMonenda()}"
        }
        viewModel.datosProductosFacturados.observe(viewLifecycleOwner) { productosFacturados ->
            adaptador = FacturaGuardadaAdaptador(productosFacturados as MutableList<ModeloProductoFacturado>)
            binding?.recyclerViewProductosFacturados?.adapter = adaptador
            adaptador.setOnClickItem() { item ->

                if (!DatosPersitidos.datosUsuario.configuracion.editarFacturas){
                    Toast.makeText(requireContext(),"No posee permiso para editar",Toast.LENGTH_LONG).show()
                }else{
                    val promtEditarItem=PromtFacturaGuardada()
                    promtEditarItem.editarProducto("venta",item,requireActivity())
                }
            }
            adaptador.setOnClickImangen { item ->
                val bundle = Bundle()
                bundle.putSerializable("modelo", item)
                Navigation.findNavController(vista).navigate(R.id.informacionProducto,bundle)
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
            val promtEditarDatos=PromtFacturaGuardada()
            promtEditarDatos.promtEditarDatosCliente(modeloFactura,requireActivity(),vista)

        }
        binding?.cardViewTotales?.setOnClickListener {
            if(!DatosPersitidos.datosUsuario.configuracion.editarFacturas){
                Toast.makeText(requireContext(),"No posee permisos para realizar esta acción", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val promtEditarDatos=PromtFacturaGuardada()
            promtEditarDatos.promtEditarModificadoresFactura(modeloFactura,requireActivity())
        }

        binding?.recyclerViewProductosFacturados?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

        val productosFiltrados = viewModel.datosProductosFacturados.value?.filter { it.producto.eliminarAcentosTildes().contains(nombreFiltrado.eliminarAcentosTildes(), ignoreCase = true) }
        adaptador = FacturaGuardadaAdaptador(productosFiltrados as MutableList<ModeloProductoFacturado>)
        binding?.recyclerViewProductosFacturados?.adapter = adaptador


        adaptador.setOnClickItem() { item ->
            val promtEditarItem=PromtFacturaGuardada()
            promtEditarItem.editarProducto("venta",item,requireActivity())
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
        menuAgregar= menu.findItem(R.id.action_agregar_producto)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_agregar_producto->{
                if(DatosPersitidos.datosUsuario.configuracion.editarFacturas){
                abrirAgregarProducto()
                }else{
                    Toast.makeText(requireContext(),"No posee permisos para realizar esta acción", Toast.LENGTH_LONG).show()
                }
                return true
            }

            R.id.action_eliminar -> {
                ocultarTeclado(requireContext(),vista)
                if(DatosPersitidos.datosUsuario.configuracion.editarFacturas){
                    dialogoEliminar()
                }else{
                    Toast.makeText(requireContext(),"No posee permisos para realizar esta acción", Toast.LENGTH_LONG).show()
                }


                return true
            }
            R.id.action_crear_pdf -> {
                val intent = Intent(activity, VistaPDFFacturaOCompra::class.java)
                intent.putExtra("id", modeloFactura.id_pedido)
                intent.putExtra("tablaReferencia", "Factura")
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun dialogoEliminar() {
        // Crear el diálogo de confirmación
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar selección")
        builder.setIcon(R.drawable.logo2_compra_rapidita)
        builder.setMessage("¿Estás seguro de que deseas eliminar esta factura y DEVOLVER los productos?")
        builder.setPositiveButton("Eliminar") { _, _ ->

            banderaElimandoFactura=true



            DatosPersitidos.progressDialog?.show()

            Utilidades.esperarUnSegundo()
            Utilidades.esperarUnSegundo()

                viewModel.eliminarFactura(requireContext())


            Toast.makeText(context,modeloFactura.nombre+"\nFactura Eliminada", Toast.LENGTH_LONG).show()

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
        viewModel.detenerEscuchadores()
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
    }

}