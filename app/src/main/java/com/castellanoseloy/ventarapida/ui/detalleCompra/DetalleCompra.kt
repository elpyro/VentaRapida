package com.castellanoseloy.ventarapida.ui.detalleCompra

import android.app.AlertDialog
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.Login
import com.castellanoseloy.ventarapida.MainActivity
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.FragmentDetalleCompraBinding
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.datos.ModeloTransaccionSumaRestaProducto
import com.castellanoseloy.ventarapida.procesos.FirebaseProductos
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.castellanoseloy.ventarapida.procesos.Utilidades.obtenerFechaActual
import com.castellanoseloy.ventarapida.procesos.Utilidades.obtenerFechaUnix
import com.castellanoseloy.ventarapida.procesos.Utilidades.obtenerHoraActual
import java.util.*

class DetalleCompra : Fragment() {



    private lateinit var viewModel: DetalleCompraViewModel
    var binding: FragmentDetalleCompraBinding? = null
    private lateinit var vista:View
    private lateinit var adaptador: DetalleCompraAdaptador
    var idPedido=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetalleCompraBinding.inflate(inflater, container, false)
        return binding!!.root // Retorna la vista inflada
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista= view

        viewModel = ViewModelProvider(this).get(DetalleCompraViewModel::class.java)

        setHasOptionsMenu(true)

        idPedido = UUID.randomUUID().toString()

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding!!.recyclerViewProductosSeleccionados.layoutManager = gridLayoutManager
        adaptador = DetalleCompraAdaptador(MainActivity.compraProductosSeleccionados )


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
        editTextPrecio.setText(item.p_compra)
        val precioAnterior = editTextPrecio.text.toString()

// Configurar el botón "Aceptar"
        dialogBuilder.setPositiveButton("Cambiar") { dialogInterface, i ->
            val nuevoNombre=editTextProducto.text.toString()
            val nuevaCantidad = editTextCantidad.text.toString()
            val nuevoPrecio = editTextPrecio.text.toString()

            viewModel.actualizarProducto(item, nuevoPrecio.toDouble(),nuevaCantidad.toInt(), nuevoNombre)
            adaptador.notifyDataSetChanged()

            if(precioAnterior!=nuevoPrecio){
                dialogoCambiarPreciosDB(nuevoPrecio, item)
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

    private fun dialogoCambiarPreciosDB(nuevoPrecio: String, item: ModeloProducto) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Mantente actualizado")
        builder.setMessage("Desea actualizar el precio de compra para todos los ${item.nombre}")
        builder.setPositiveButton("Sí") { dialog, which ->

            val updates = hashMapOf<String, Any>(
                "id" to item.id,
                "p_compra" to nuevoPrecio,
            )
            FirebaseProductos.guardarProducto(updates)
            Toast.makeText(requireContext(), "${item.nombre} ha sido actualizado", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("No", null)
        builder.show()
    }

    private fun listeners() {


        binding?.recyclerViewProductosSeleccionados?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    Utilidades.ocultarTeclado(requireContext(), vista)
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

        binding?.recyclerViewProductosSeleccionados?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    Utilidades.ocultarTeclado(requireContext(), vista)
                }
            }
        })
    }

    private fun observadores() {

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
            Toast.makeText(context,it, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detalle_factura_o_compra, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_confirmar_venta ->{
                Utilidades.ocultarTeclado(requireContext(), vista)

                //evalua si la sesion esta activa
                if( MainActivity.datosUsuario.id.isNullOrEmpty()){
                    requireActivity().finish()
                    val intent = Intent(requireContext(), Login::class.java)
                    startActivity(intent)
                    return true
                }

                if(MainActivity.compraProductosSeleccionados.size<1){
                    Toast.makeText(context,"No hay productos seleccionados", Toast.LENGTH_LONG).show()
                    return true
                }


                MainActivity.progressDialog?.show()

                val datosPedido= obtenerDatosPedido()
                var listaConvertida=convertirLista(MainActivity.compraProductosSeleccionados,datosPedido)

                viewModel.subirDatos(datosPedido,listaConvertida.first)


                FirebaseProductos.transaccionesCambiarCantidad(context, listaConvertida.second)


                viewModel.abrirPDFConPreferencias(listaConvertida.first, datosPedido)

                //limpiamos los productos seleccionados
                viewModel.limpiarProductosSelecionados(requireContext())

                Toast.makeText(requireContext(), "Los productos fueron agregados al inventario",Toast.LENGTH_LONG).show()

                findNavController().popBackStack()
                return true
            }

            R.id.action_ver_pdf ->{

                val datosPedido=obtenerDatosPedido()
                var listaConvertida=convertirLista(MainActivity.compraProductosSeleccionados,datosPedido)
                viewModel.abrirPDFConPreferencias(listaConvertida.first,datosPedido)


                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun obtenerDatosPedido(): HashMap<String, Any> {

        val horaActual = obtenerHoraActual()
        val fechaActual = obtenerFechaActual()

        val total=binding?.textViewTotal?.text.toString()
        val nombre= binding?.editTextTienda?.text.toString().ifBlank { "Desconocida" }
        val totalconEtiqueta = total.replace("Total:", "Nuevo ").trim()
        val datosPedido = hashMapOf<String, Any>(
            "id_pedido" to idPedido,
            "nombre" to nombre,
            "telefono" to "",
            "documento" to "",
            "direccion" to "",
            "descuento" to "0",
            "envio" to "0",
            "fecha" to fechaActual,
            "hora" to horaActual,
            "id_vendedor" to MainActivity.datosUsuario.id,
            "nombre_vendedor" to MainActivity.datosUsuario.nombre,
            "total" to totalconEtiqueta,
            "fechaBusquedas" to obtenerFechaUnix()
        )
        return datosPedido
    }

    private fun convertirLista(
        compraProductosSeleccionados: MutableMap<ModeloProducto, Int>,
        datosPedido: HashMap<String, Any>
    ):  Pair<ArrayList<ModeloProductoFacturado>, ArrayList<ModeloTransaccionSumaRestaProducto>> {

        val listaProductosComprados = arrayListOf<ModeloProductoFacturado>()
        val listaDescontarInventario = arrayListOf<ModeloTransaccionSumaRestaProducto>()

        val idPedido = datosPedido["id_pedido"].toString()
        val horaActual = datosPedido["hora"].toString()
        val fechaActual = datosPedido["fecha"].toString()

        compraProductosSeleccionados.forEach{ (producto, cantidadSeleccionada)->
            //calculamos el precio descuento para tener la referencia para los reportes
            if (cantidadSeleccionada!=0){

                val id_producto_pedido = UUID.randomUUID().toString()

                val productoFacturado = ModeloProductoFacturado(
                    id_producto_pedido =id_producto_pedido,
                    id_producto = producto.id,
                    id_pedido = idPedido,
                    id_vendedor = MainActivity.datosUsuario.id,
                    vendedor = MainActivity.datosUsuario.nombre,
                    producto = producto.nombre,
                    cantidad = cantidadSeleccionada.toString(),
                    costo = producto.p_compra,
                    venta = producto.p_diamante,
                    fecha = fechaActual,
                    hora =horaActual,
                    imagenUrl =producto.url,
                    fechaBusquedas = obtenerFechaUnix()
                )
                listaProductosComprados.add(productoFacturado)

                val restarProducto = ModeloTransaccionSumaRestaProducto(
                    idTransaccion = id_producto_pedido,  //la transaccion tiene el mismo id
                    idProducto = producto.id,
                    cantidad = (-1 * cantidadSeleccionada).toString(),
                    subido ="false"
                )

                listaDescontarInventario.add(restarProducto)

            }
        }
        return Pair(listaProductosComprados, listaDescontarInventario)
    }



    fun filtrarProductos(nombreFiltrado: String) {

        val productosFiltrados = MainActivity.compraProductosSeleccionados.filter { it.key.nombre.eliminarAcentosTildes().contains(nombreFiltrado.eliminarAcentosTildes(), ignoreCase = true) }.toMutableMap()
        adaptador = DetalleCompraAdaptador(productosFiltrados)
        binding?.recyclerViewProductosSeleccionados?.adapter = adaptador


        adaptador!!.setOnClickItem() { item, cantidad, position ->
            editarItem(item, cantidad)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding=null
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
    }



}