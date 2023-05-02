package com.example.ventarapida.ui.factura_guardada

import android.app.AlertDialog
import android.content.Intent
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
import com.example.ventarapida.VistaPDF
import com.example.ventarapida.databinding.FragmentFacturaGuardadaBinding
import com.example.ventarapida.datos.ModeloFactura
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.FirebaseFacturaOCompra
import com.example.ventarapida.procesos.Utilidades
import com.example.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import com.example.ventarapida.procesos.Utilidades.ocultarTeclado
import com.example.ventarapida.ui.promts.PromtFacturaGuardada

class FacturaGuardada : Fragment() {

    private lateinit var viewModel: FacturaGuardadaViewModel

    private var binding: FragmentFacturaGuardadaBinding? = null
    private lateinit var vista:View
    private lateinit var adaptador: FacturaGuardadaAdaptador
    private lateinit var  modeloFactura: ModeloFactura
    private  var banderaElimandoFactura :Boolean=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentFacturaGuardadaBinding.inflate(inflater, container, false)

        //activar  menu para este fragment
        setHasOptionsMenu(true)
        
        // Recibe los productos de la lista del fragmento anterior
        val bundle = arguments
        modeloFactura= (bundle?.getSerializable("modelo") as? ModeloFactura)!!

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding?.recyclerViewProductosFacturados?.layoutManager = gridLayoutManager

        viewModel = ViewModelProvider(this).get(FacturaGuardadaViewModel::class.java)

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

        }

        viewModel.totalFactura.observe(viewLifecycleOwner){

            if (banderaElimandoFactura==true) return@observe

            binding?.textViewTotal?.text="Total: $it"
            val updates = hashMapOf<String, Any>(
                "id_pedido" to modeloFactura.id_pedido,
                "total" to it.eliminarAcentosTildes(),
            )
            FirebaseFacturaOCompra.guardarFacturaOCompra("Factura",updates)
        }
        viewModel.subTotal.observe(viewLifecycleOwner){
            binding?.textViewSubtotal?.text="Sub-Total: $it"
        }
        viewModel.datosProductosFacturados.observe(viewLifecycleOwner) { productosFacturados ->
            adaptador = FacturaGuardadaAdaptador(productosFacturados as MutableList<ModeloProductoFacturado>)
            binding?.recyclerViewProductosFacturados?.adapter = adaptador
            adaptador!!.setOnClickItem() { item ->

                val promtEditarItem=PromtFacturaGuardada()
                promtEditarItem.editarProducto("venta",item,requireActivity())

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
            promtEditarDatos.promtEditarDatosCliente(modeloFactura,requireActivity())
        }
        binding?.cardViewTotales?.setOnClickListener {
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


        adaptador!!.setOnClickItem() { item ->
            val promtEditarItem=PromtFacturaGuardada()
            promtEditarItem.editarProducto("venta",item,requireActivity())
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_factura_guardada, menu)
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
                dialogoEliminar()

                return true
            }
            R.id.action_crear_pdf -> {
                val intent = Intent(activity, VistaPDF::class.java)
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
        builder.setMessage("¿Estás seguro de que deseas eliminar esta factura y DEVOLVER los productos?")
        builder.setPositiveButton("Eliminar") { dialog, which ->

            banderaElimandoFactura=true

            viewModel.eliminarFactura(requireContext(),modeloFactura)

            Utilidades.esperarUnSegundo()

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
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
    }

}