@file:Suppress("DEPRECATION")

package com.castellanoseloy.ventarapida.ui.detalleVenta

import android.app.AlertDialog
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.Login
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.ventaProductosSeleccionados
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.FragmentDetalleVentaBinding
import com.castellanoseloy.ventarapida.datos.ModeloClientes
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.datos.ModeloTransaccionSumaRestaProducto
import com.castellanoseloy.ventarapida.procesos.FirebaseProductos
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.obtenerFechaActual
import com.castellanoseloy.ventarapida.procesos.Utilidades.obtenerFechaUnix
import com.castellanoseloy.ventarapida.procesos.Utilidades.obtenerHoraActual
import com.castellanoseloy.ventarapida.procesos.Utilidades.ocultarTeclado
import com.castellanoseloy.ventarapida.servicios.ServicioGuadarFactura
import com.castellanoseloy.ventarapida.servicios.ServicioListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DetalleVenta : Fragment(), ServicioListener {

    private lateinit var viewModel: DetalleVentaViewModel
    var binding: FragmentDetalleVentaBinding? = null
    private lateinit var vista:View
    private lateinit var adaptador:DetalleVentaAdaptador
    var idPedido=""
    var limpiar=false
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

        idPedido = UUID.randomUUID().toString()

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding?.recyclerViewProductosSeleccionados?.layoutManager = gridLayoutManager
        actualizarRecycerView()

        viewModel.context = requireContext()
        viewModel.totalFactura()

        //establece el codigo de area por defecto
        binding?.editTextTelefono?.setText(DatosPersitidos.edit_text_preference_codigo_area+" ")

        observadores()

        listeners()


    }

    private fun actualizarRecycerView() {

        adaptador = DetalleVentaAdaptador(ventaProductosSeleccionados )
        binding?.recyclerViewProductosSeleccionados?.adapter = adaptador
        adaptador.setOnClickItem() { item, cantidad, position ->
            editarItem(item, cantidad)
        }

    }


    private fun listeners() {


        binding?.imageButtonBuscarCliente?.setOnClickListener{
            Navigation.findNavController(vista).navigate(R.id.listaClientes)
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

        binding?.editTextEnvio?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Actualiza el valor de envio con el valor del EditText
                if (binding!!.editTextEnvio.text.toString().isNotEmpty()) {
                    viewModel.envio.value = binding!!.editTextEnvio.text.toString()
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

        binding?.editDescuento?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Actualiza el valor de envio con el valor del EditText
                if (binding!!.editDescuento.text.toString().isNotEmpty()) {
                    viewModel.descuento.value = binding!!.editDescuento.text.toString()
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
            binding?.textViewReferencias?.text = "Referencias: "+it
        }
        viewModel.itemsSeleccionados.observe(viewLifecycleOwner) {
            binding?.textViewItems?.text = "Items: "+ it

        }

        viewModel.mensajeToast.observe(viewLifecycleOwner){
            Toast.makeText(context,it,Toast.LENGTH_SHORT).show()
        }

        DetalleVentaViewModel.datosCliente.observe(viewLifecycleOwner){

            binding?.editTextNombre?.setText(it.nombre)
            binding?.editTextTelefono?.setText(it.telefono)
            binding?.editTextDocumento?.setText(it.documento)
            binding?.editTextDireccion?.setText(it.direccion)
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
                ocultarTeclado(requireContext(),vista)
                //validando
                //evalua si la sesion esta activa
                if( DatosPersitidos.datosUsuario.id.isNullOrEmpty()){
                    requireActivity().finish()
                    val intent = Intent(requireContext(), Login::class.java)
                    startActivity(intent)
                    return true
                }

                if(ventaProductosSeleccionados.size<1){
                    Toast.makeText(context,"No hay productos seleccionados",Toast.LENGTH_LONG).show()
                    return true
                }

                crearFacturaVenta()


                return true
            }

            R.id.action_ver_pdf ->{
                abrirPDFConPreferencias()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }



    private fun crearFacturaVenta() {
        DatosPersitidos.progressDialog?.show()


        val datosPedido: HashMap<String, Any> = obtenerDatosPedido()
        // Crear el Intent para iniciar el servicio
        val intent = Intent(context, ServicioGuadarFactura::class.java)
        intent.putExtra("datosPedido", datosPedido)
        intent.putExtra("operacion","Venta")


        ServicioGuadarFactura.setServicioListener(object : ServicioListener {
            override fun onServicioTerminado() {
                abrirPDFConPreferencias()
                cerrarYLimpiar()
            }
        })

        // Iniciar el servicio en segundo plano utilizando JobIntentService
        JobIntentService.enqueueWork(
            requireContext(),
            ServicioGuadarFactura::class.java,
            DatosPersitidos.JOB_IDGUARDARFACTURA,
            intent)

    }

    private fun cerrarYLimpiar() {
        GlobalScope.launch(Dispatchers.Main) {
        viewModel.limpiar(requireContext())
        limpiar=true
        findNavController().popBackStack()
        }
    }


    private fun abrirPDFConPreferencias() {
        GlobalScope.launch(Dispatchers.Main) {
            val datosPedido = obtenerDatosPedido()
            val listaConvertida = convertirLista(ventaProductosSeleccionados, datosPedido)
            viewModel.abrirPDFConPreferencias(listaConvertida.first, datosPedido)
        }
    }


    private fun obtenerDatosPedido(): HashMap<String, Any> {

        val horaActual = obtenerHoraActual()
        val fechaActual = obtenerFechaActual()

        val nombre= binding?.editTextNombre?.text.toString().ifBlank { "Anonimo" }
        val envio= binding?.editTextEnvio?.text.toString().ifBlank { "0" }
        val descuento= binding?.editDescuento?.text.toString().ifBlank { "0" }
        val total=binding?.textViewTotal?.text.toString()
        val totalconEtiqueta = total.replace("Total:", "Nuevo ").trim()

        val datosPedido = hashMapOf<String, Any>(
            "id_pedido" to idPedido,
            "nombre" to nombre,
            "telefono" to binding?.editTextTelefono?.text.toString(),
            "documento" to binding?.editTextDocumento?.text.toString(),
            "direccion" to binding?.editTextDireccion?.text.toString(),
            "descuento" to descuento,
            "envio" to envio,
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
        ventaProductosSeleccionados: MutableMap<ModeloProducto, Int>,
        datosPedido: HashMap<String, Any>
    ): Pair<ArrayList<ModeloProductoFacturado>, ArrayList<ModeloTransaccionSumaRestaProducto>> {

        val listaProductosFacturados = arrayListOf<ModeloProductoFacturado>()
        val listaDescontarInventario = arrayListOf<ModeloTransaccionSumaRestaProducto>()

        val idPedido = datosPedido["id_pedido"].toString()
        val horaActual = datosPedido["hora"].toString()
        val fechaActual = datosPedido["fecha"].toString()
        val descuento = datosPedido["descuento"].toString()
        var recaudo="Pendiente"
        if(DatosPersitidos.datosUsuario.perfil.equals("Administrador")) {
            recaudo = "No aplica"
        }
        ventaProductosSeleccionados.forEach { (producto, cantidadSeleccionada) ->
            if (cantidadSeleccionada != 0) {

                val porcentajeDescuento = descuento.toDouble() / 100
                var precioDescuento: Double = producto.p_diamante.toDouble()
                precioDescuento *= (1 - porcentajeDescuento)

                val id_producto_pedido = UUID.randomUUID().toString()

                val productoFacturado = ModeloProductoFacturado(
                    id_producto_pedido = id_producto_pedido,
                    id_producto = producto.id,
                    id_pedido = idPedido,
                    id_vendedor = DatosPersitidos.datosUsuario.id,
                    vendedor = DatosPersitidos.datosUsuario.nombre,
                    producto = producto.nombre,
                    cantidad = cantidadSeleccionada.toString(),
                    costo = producto.p_compra,
                    venta = producto.p_diamante,
                    precioDescuentos = precioDescuento.toString(),
                    porcentajeDescuento = binding?.editDescuento?.text.toString().trim(),
                    productoEditado = producto.editado,
                    fecha = fechaActual,
                    hora = horaActual,
                    imagenUrl = producto.url,
                    fechaBusquedas = obtenerFechaUnix(),
                    estadoRecaudo = recaudo
                )
                listaProductosFacturados.add(productoFacturado)

                val restarProducto = ModeloTransaccionSumaRestaProducto(
                    idTransaccion = id_producto_pedido,  //la transaccion tiene el mismo id
                    idProducto = producto.id,
                    cantidad = (cantidadSeleccionada).toString(),
                    subido ="false"
                )

                listaDescontarInventario.add(restarProducto)
            }
        }

        return Pair(listaProductosFacturados, listaDescontarInventario)
    }


    override fun onDestroyView() {
        super.onDestroyView()

        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()

    }

    override fun onPause() {
        super.onPause()

        if (limpiar==false){
            val datosCliente = DetalleVentaViewModel.datosCliente.value?: ModeloClientes()
            datosCliente.nombre = binding?.editTextNombre?.text.toString()
            datosCliente.telefono = binding?.editTextTelefono?.text.toString()
            datosCliente.documento = binding?.editTextDocumento?.text.toString()
            datosCliente.direccion = binding?.editTextDireccion?.text.toString()
            DetalleVentaViewModel.datosCliente.postValue(datosCliente)
        }else{
            DetalleVentaViewModel.datosCliente = MutableLiveData<ModeloClientes>()
        }

    }

    fun editarItem(item: ModeloProducto, cantidad: Int) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setIcon(R.drawable.logo2_compra_rapidita)
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
        editTextPrecio.setText(item.p_diamante)


// Configurar el botón "Aceptar"
        dialogBuilder.setPositiveButton("Cambiar") { dialogInterface, i ->
            val nuevoNombre=editTextProducto.text.toString()
            val nuevaCantidad = editTextCantidad.text.toString()
            val nuevoPrecio = editTextPrecio.text.toString()

            viewModel.actualizarProducto(item, nuevoPrecio.toDouble(),nuevaCantidad.toInt(), nuevoNombre)

            actualizarRecycerView()
        }

// Configurar el botón "Cancelar"
        dialogBuilder.setNegativeButton("Cancelar") { dialogInterface, i ->
            // No hacer nada
        }

        dialogBuilder.setNeutralButton("Eliminar") { dialogInterface, i ->
            viewModel.eliminarProducto(  item)
            actualizarRecycerView()
        }

// Mostrar el diálogo
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }



    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }

    override fun onServicioTerminado() {
        TODO("Not yet implemented")
    }

}