@file:Suppress("DEPRECATION")

package com.castellanoseloy.ventarapida.ui.detalleCompra

import android.app.AlertDialog
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.Login
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.FragmentDetalleCompraBinding
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.datos.ModeloTransaccionSumaRestaProducto
import com.castellanoseloy.ventarapida.procesos.FirebaseProductos
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.formatoMonenda
import com.castellanoseloy.ventarapida.procesos.Utilidades.obtenerFechaActual
import com.castellanoseloy.ventarapida.procesos.Utilidades.obtenerFechaUnix
import com.castellanoseloy.ventarapida.procesos.Utilidades.obtenerHoraActual
import com.castellanoseloy.ventarapida.servicios.ServicioGuadarFactura
import com.castellanoseloy.ventarapida.servicios.ServicioListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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


        actualizarRecycerView()

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
        val imageView_foto= dialogView.findViewById<ImageView>(R.id.imageView_foto)

        // Seleccionar tode el contenido del EditText al recibir foco
        editTextProducto.setSelectAllOnFocus(true)
        editTextCantidad.setSelectAllOnFocus(true)
        editTextPrecio.setSelectAllOnFocus(true)

        Utilidades.cargarImagen(item.url, imageView_foto)
        editTextProducto.setText( item.nombre)
        editTextCantidad.setText(cantidad.toString())
        editTextPrecio.setText(item.p_compra)
        val precioAnterior = editTextPrecio.text.toString()

        // Configurar el botón "Aceptar"
        dialogBuilder.setPositiveButton("Cambiar") { _, _ ->
            val nuevoNombre=editTextProducto.text.toString()
            val nuevaCantidad = editTextCantidad.text.toString()
            val nuevoPrecio = editTextPrecio.text.toString()

            viewModel.actualizarProducto(item, nuevoPrecio.toDouble(),nuevaCantidad.toInt(), nuevoNombre)
            actualizarRecycerView()

            if(precioAnterior!=nuevoPrecio){
                dialogoCambiarPreciosDB(nuevoPrecio, nuevaCantidad, item)
            }

        }

// Configurar el botón "Cancelar"
        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            // No hacer nada
        }

        dialogBuilder.setNeutralButton("Eliminar") { _, _ ->
            viewModel.eliminarProducto(  item)
            actualizarRecycerView()
        }

// Mostrar el diálogo
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun actualizarRecycerView() {
        adaptador = DetalleCompraAdaptador(DatosPersitidos.compraProductosSeleccionados )
        binding?.recyclerViewProductosSeleccionados?.adapter = adaptador
        adaptador.setOnClickItem() { item, cantidad, _ ->
            editarItem(item, cantidad)
        }
    }

    private fun dialogoCambiarPreciosDB(
        nuevoPrecio: String,
        nuevaCantidad: String,
        itemAnterior: ModeloProducto
    ) {

        FirebaseProductos.buscarProductoPorId(itemAnterior.id)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val itemActualizado = it.result

                    if(!itemActualizado?.p_compra.equals(nuevoPrecio)) {

                        val itemValorActual =
                            itemActualizado?.p_compra!!.toDouble() * itemActualizado.cantidad.toInt()
                        val itemValorCompra = nuevoPrecio.toDouble() * nuevaCantidad.toInt()
                        val valorInventariado = itemValorActual + itemValorCompra
                        val totalProductoInventariado =
                            itemActualizado.cantidad.toInt() + nuevaCantidad.toInt()

                        val promedio = valorInventariado / totalProductoInventariado
                        val promedioFormateado = String.format("%.2f", promedio)

                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("Mantente actualizado")
                        builder.setMessage("Desea actualizar el precio de compra de ${itemActualizado.p_compra.formatoMonenda()} a el nuevo precio ${nuevoPrecio.formatoMonenda()} para todos los ${itemActualizado.nombre}")
                        builder.setPositiveButton("Sí") { _, _ ->
                            actualizarPrecio(nuevoPrecio,itemActualizado)
                        }

                        builder.setNegativeButton("No", null)

                        builder.setNeutralButton("Promediar (${
                            promedioFormateado.formatoMonenda()
                            })"
                        ) { _, _ ->
                            actualizarPrecio(promedioFormateado,itemActualizado)
                        }
                        builder.show()
                    }
                }
            }


    }

    private fun actualizarPrecio(nuevoPrecio: String, item:ModeloProducto) {
        val updates = hashMapOf<String, Any>(
            "id" to item.id,
            "p_compra" to nuevoPrecio,
        )
        FirebaseProductos.guardarProducto(updates)
        Toast.makeText(
            requireContext(),
            "${item.nombre} ha sido actualizado",
            Toast.LENGTH_LONG
        ).show()
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


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detalle_factura_o_compra, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_confirmar_venta ->{
                Utilidades.ocultarTeclado(requireContext(), vista)

                //evalua si la sesion esta activa
                if( DatosPersitidos.datosUsuario.id.isNullOrEmpty()){
                    requireActivity().finish()
                    val intent = Intent(requireContext(), Login::class.java)
                    startActivity(intent)
                    return true
                }

                if(DatosPersitidos.compraProductosSeleccionados.isEmpty()){
                    Toast.makeText(context,"No hay productos seleccionados", Toast.LENGTH_LONG).show()
                    return true
                }

                realizarCompra()

                return true
            }

            R.id.action_ver_pdf ->{

                val datosPedido=obtenerDatosPedido()
                val listaConvertida=convertirLista(DatosPersitidos.compraProductosSeleccionados,datosPedido)
                viewModel.abrirPDFConPreferencias(listaConvertida.first,datosPedido)


                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun realizarCompra() {
        DatosPersitidos.progressDialog?.show()


        val datosPedido: HashMap<String, Any> = obtenerDatosPedido()
        // Crear el Intent para iniciar el servicio
        val intent = Intent(context, ServicioGuadarFactura::class.java)
        intent.putExtra("datosPedido", datosPedido)
        intent.putExtra("operacion","Compra")

        ServicioGuadarFactura.setServicioListener(object : ServicioListener {
            override fun onServicioTerminado() {
                mostrarPdf()
            }
        })

        // Iniciar el servicio en segundo plano utilizando JobIntentService
        JobIntentService.enqueueWork(
            requireContext(),
            ServicioGuadarFactura::class.java,
            DatosPersitidos.JOB_IDGUARDARFACTURA,
            intent)


    }

    private fun mostrarPdf() {
        GlobalScope.launch(Dispatchers.Main) {
            val datosPedido = obtenerDatosPedido()
            val listaConvertida =
                convertirLista(DatosPersitidos.compraProductosSeleccionados, datosPedido)
            viewModel.abrirPDFConPreferencias(listaConvertida.first, datosPedido)

            //limpiamos los productos seleccionados
            viewModel.limpiarProductosSelecionados(requireContext())

            Toast.makeText(
                requireContext(),
                "Los productos fueron agregados al inventario",
                Toast.LENGTH_LONG
            ).show()

            findNavController().popBackStack()
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
            "id_vendedor" to DatosPersitidos.datosUsuario.id,
            "nombre_vendedor" to DatosPersitidos.datosUsuario.nombre,
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
                    id_vendedor = DatosPersitidos.datosUsuario.id,
                    vendedor = DatosPersitidos.datosUsuario.nombre,
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


    override fun onDestroyView() {
        super.onDestroyView()
        binding=null
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
    }



}