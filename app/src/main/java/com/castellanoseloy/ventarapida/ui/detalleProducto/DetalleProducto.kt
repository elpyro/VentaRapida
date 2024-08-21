package com.castellanoseloy.ve

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.castellanoseloy.ventarapida.ui.detalleProducto.DetalleProductoViewModel
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.FragmentDetalleProductoBinding
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.datos.Variable
import com.castellanoseloy.ventarapida.procesos.FirebaseFacturaOCompra
import com.castellanoseloy.ventarapida.procesos.FirebaseProductoFacturadosOComprados
import com.castellanoseloy.ventarapida.procesos.FirebaseProductos
import com.castellanoseloy.ventarapida.procesos.FirebaseProductos.guardarProducto
import com.castellanoseloy.ventarapida.procesos.TomarFotoYGaleria
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.ocultarTeclado
import com.castellanoseloy.ventarapida.procesos.UtilidadesBaseDatos
import com.castellanoseloy.ventarapida.procesos.VerificarInternet
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.ui.detalleProducto.DetalleVariantesAdaptador
import com.castellanoseloy.ventarapida.ui.promts.PromtAgregarVariante
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import kotlin.math.absoluteValue

@Suppress("DEPRECATION")
class DetalleProducto : Fragment() {

    private lateinit var productoDetalle: ModeloProducto
    private var bitmapFoto: Bitmap? = null
    private var changeListenerActived: Boolean = false
    private lateinit var cantidadAntigua: String

    //    private lateinit var toolbar: Toolbar
    private var binding: FragmentDetalleProductoBinding? = null
    private val viewModel: DetalleProductoViewModel by viewModels() // Inicialización de viewModel
    private lateinit var productosViewModel: DetalleProductoViewModel
    private var vista: View? = null
    private lateinit var id_producto: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        // Inflar el layout del fragmento usando el binding
        binding = FragmentDetalleProductoBinding.inflate(inflater, container, false)

        // Recibe los productos de la lista del fragmento anterior
        val bundle = arguments
        id_producto = bundle?.getString("idProducto")!!

        Log.d("ModeloProducto", "Updates: $id_producto")


        // Inicialización de productosViewModel
        productosViewModel = ViewModelProvider(this).get(DetalleProductoViewModel::class.java)

        // Observa los cambios en detalleProducto y actualiza la UI en consecuencia
        productosViewModel.detalleProducto.observe(viewLifecycleOwner) { detalleProducto ->
            binding?.textViewInformacionAgregarCantidades?.visibility = View.GONE
            changeListenerActived = false
            if (detalleProducto.isNotEmpty()) {
                productoDetalle = detalleProducto[0]
                actualizarCampos(productoDetalle)
                cargarVariantes(productoDetalle)

            }
        }
        productosViewModel.mensajeToast.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        binding?.imageViewFoto?.setOnClickListener {
            cargarImagen()
        }


        binding?.buttonHistorial?.setOnClickListener {
            mostrarHistorial()
        }

        binding?.editTextCantidad?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (changeListenerActived) {
                    binding?.textViewInformacionAgregarCantidades?.visibility = View.VISIBLE
                }
                changeListenerActived = true

            }

            override fun afterTextChanged(s: Editable?) {}
        })



        // Carga el producto en la UI

        buscarProducto(id_producto) { producto ->
            //actualizar el modelo por si hay algun cambio
            if (producto != null) {
                cargarProducto(producto)

                cargarVariables(producto)
            }
        }


        return binding!!.root // Retorna la vista inflada
    }

    private fun cargarVariables(producto: ModeloProducto) {
        binding?.buttonAgregarVariantes?.setOnClickListener {
            promtVariantes(producto)
        }
    }

    private fun promtVariantes(producto: ModeloProducto, variable: Variable? = null) {
        PromtAgregarVariante().agregar(
            requireContext(),
            producto.listaVariables,
            variable
        ) { listaActualizada ->
            producto.listaVariables = listaActualizada
            if(producto.listaVariables.isNullOrEmpty()) producto.cantidad="0"
            cargarVariantes(producto)
            actualizarProductoEnViewModel(producto)


        }
    }



    private fun actualizarProductoEnViewModel(producto: ModeloProducto) {
        val listaProductos = viewModel.detalleProducto.value?.toMutableList() ?: mutableListOf()
        val index = listaProductos.indexOfFirst { it.id == producto.id }

        if (index != -1) {
            listaProductos[index] = producto
        } else {
            listaProductos.add(producto)
        }
        viewModel.detalleProducto.postValue(listaProductos)
        Log.d("ModeloProducto", "variables:" + viewModel.detalleProducto.value!![0].listaVariables)
    }


    private fun buscarProducto(idProducto: String?, callback: (ModeloProducto?) -> Unit) {
        FirebaseProductos.buscarProductoPorId(idProducto!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val producto = task.result
                    // Llamar al callback con el producto
                    callback(producto)
                }
            }
    }



    private fun cargarVariantes(productoDetalle: ModeloProducto) {

        if (!productoDetalle.listaVariables.isNullOrEmpty()) {
            binding!!.recyclerVariantes.visibility = View.VISIBLE
            binding!!.editTextCantidad.isEnabled = false

            val gridLayoutManager = GridLayoutManager(requireContext(), 1)
            binding!!.recyclerVariantes.layoutManager = gridLayoutManager
            var adaptador = DetalleVariantesAdaptador(productoDetalle.listaVariables!!)
            binding?.recyclerVariantes?.adapter = adaptador
            adaptador.setOnClickItem { variable ->
                Log.d("ModeloProducto", "variable seleccionada: $variable")
                promtVariantes(productoDetalle, variable)
            }

            binding?.recyclerVariantes?.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        // se está desplazando hacia abajo
                        ocultarTeclado(requireContext(), vista!!)

                    }
                }
            })

        } else {
            //si el producto fue comprado y no tiene variantes se desactiva el boton agregar
            val cantidad = binding!!.editTextCantidad.text.toString().toInt()
            if (cantidad != 0) binding!!.buttonAgregarVariantes.visibility = View.GONE
            binding!!.recyclerVariantes.visibility = View.GONE
        }

    }



    private fun mostrarHistorial() {
        val bundle = Bundle()
        bundle.putString("idProducto", id_producto)
        Navigation.findNavController(vista!!).navigate(R.id.historialProducto, bundle)
    }


    private fun actualizarCampos(producto: ModeloProducto) {
        Log.d("ModeloProducto", "Producto Actualziado: $producto")
        id_producto = producto.id
        binding?.editTextProducto?.setText(producto.nombre)
        binding?.editTextPCompra?.setText(producto.p_compra)
        binding?.editTextPVenta?.setText(producto.p_diamante)

        // Calcular el total de las cantidades en listaVariables
        var totalCantidad = 0
        if (!producto.listaVariables.isNullOrEmpty()) {
            for (variable in producto.listaVariables!!) {
                totalCantidad += variable.cantidad
            }
        } else {
            totalCantidad = producto.cantidad.toInt()
        }


        // Convertir el total a String y asignarlo al campo editTextCantidad
        binding?.editTextCantidad?.setText(totalCantidad.toString())

        cantidadAntigua = producto.cantidad
        binding?.editTextProveedor?.setText(producto.proveedor)
        binding?.editTextComentario?.setText(producto.comentario)
        if (producto.url.isNotEmpty()) {
            Picasso.get().load(producto.url).into(binding?.imageViewFoto)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista = view // Guarda la vista en la variable vista
    }

    private fun verificarPosiciones() {
        //Verifica la posicion del array para mostrar o ocultar los botones de siguiente o anterior
        if (viewModel.posicionActual == 0) {
            binding?.imageViewBotonIzquierda?.visibility = View.INVISIBLE
        } else {
            binding?.imageViewBotonIzquierda?.visibility = View.VISIBLE
        }
        if (viewModel.posicionActual == viewModel.listaProductos.size - 1) {
            binding?.imageViewBotonDerecha?.visibility = View.INVISIBLE
        } else {
            binding?.imageViewBotonDerecha?.visibility = View.VISIBLE
        }
    }



    private fun cargarSiguienteProducto() {
        // Incrementa la posición actual en 1
        viewModel.posicionActual++

        // Si hemos llegado al final de la lista, volvemos al principio
        if (viewModel.posicionActual >= viewModel.listaProductos.size) {
            viewModel.posicionActual = 0
        }

        // Obtenemos el siguiente modelo de producto de la lista
        val siguienteModeloProducto = viewModel.listaProductos[viewModel.posicionActual]

        // Actualizamos el fragmento con los detalles del siguiente producto
        cargarProducto(siguienteModeloProducto)
        verificarPosiciones()
        ocultarTeclado(requireContext(), vista!!)
    }


    private fun cargarProducto(modeloProducto: ModeloProducto?) {
        binding?.imageViewFoto?.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_menu_camera
            )
        )

        viewModel.setIdProducto(modeloProducto!!.id)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menuproducto, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_guardar -> {
                guardar()
                return true
            }

            R.id.action_camara -> {
                cargarImagen()
                return true
            }

            R.id.action_eliminar -> {
                eliminar()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun cargarImagen() {
        val imageHandler = TomarFotoYGaleria(this)
        imageHandler.cargarImagen()
    }


    private fun eliminar() {
        ocultarTeclado(requireContext(), vista!!)

        // Crear el diálogo de confirmación
        val builder = AlertDialog.Builder(requireContext())
        builder.setIcon(R.drawable.logo2_compra_rapidita)
        builder.setTitle("Eliminar producto")
        builder.setMessage("¿Estás seguro de que deseas eliminar este producto?")
        builder.setPositiveButton("Eliminar") { _, _ ->
            if (id_producto.isNotEmpty()) viewModel.eliminarProducto(id_producto) else Toast.makeText(
                requireContext(),
                "No se puede elimnar el producto null",
                Toast.LENGTH_LONG
            ).show()
            findNavController().popBackStack()
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    @SuppressLint("SuspiciousIndentation")
    fun guardar() {
        val verificarConexion = VerificarInternet()

        ocultarTeclado(requireContext(), vista!!)

        if (binding!!.editTextProducto.text.toString().isEmpty()) {
            binding!!.editTextProducto.error = "Obligatorio"
            return
        }
        if (binding!!.editTextPCompra.text.toString().trim().isEmpty()) {
            binding!!.editTextPCompra.error = "Obligatorio"
            return
        }
        if (binding!!.editTextPVenta.text.toString().trim().isEmpty()) {
            binding!!.editTextPVenta.error = "Obligatorio"
            return
        }

        //veficicar si hay imagen cargada
        if (bitmapFoto != null) {
            viewModel.subirImagenFirebase(requireContext(), bitmapFoto)
            bitmapFoto = null
        }

        var cantidadDisponible = "0"
        if (binding!!.editTextCantidad.text.toString().trim().isNotEmpty()) cantidadDisponible =
            binding!!.editTextCantidad.text.toString().trim()
        if (cantidadAntigua != cantidadDisponible) actualizarCantidadTransaccion(cantidadDisponible)


        // Actualizar el producto con los valores del formulario
        val producto = viewModel.detalleProducto.value!![0].copy(
            cantidad = cantidadDisponible,
            nombre = binding!!.editTextProducto.text.toString().trim(),
            p_compra = binding!!.editTextPCompra.text.toString(),
            p_diamante = binding!!.editTextPVenta.text.toString(),
            id = id_producto,
            comentario = binding!!.editTextComentario.text.toString().trim(),
            proveedor = binding!!.editTextProveedor.text.toString(),
            listaVariables = viewModel.detalleProducto.value!![0].listaVariables
        )
        Log.d(
            "ModeloProducto",
            "variables antes de guardar: $viewModel.detalleProducto.value!![0].listaVariables"
        )
        Log.d("ModeloProducto", "Producto Actualziado en firebase: $producto")

        val updates = producto.getUpdates()
        Log.d("ModeloProducto", "Updates: $updates")

        guardarProducto(updates)

        Toast.makeText(requireContext(), "Producto Actualizado", Toast.LENGTH_LONG).show()

        if (!verificarConexion.verificarConexion(requireContext())) {
            Toast.makeText(
                requireContext(),
                getString(R.string.disponbleEnlaNuebe),
                Toast.LENGTH_LONG
            ).show()
        }

        eliminarDeListas(producto)

        findNavController().popBackStack()
    }

    private fun eliminarDeListas(producto: ModeloProducto) {
        if (DatosPersitidos.compraProductosSeleccionados.isNotEmpty()) {
            val itemAEliminar = DatosPersitidos.compraProductosSeleccionados
                .filterKeys { it.id == producto.id }
                .keys
                .firstOrNull()

            if (itemAEliminar != null) {
                DatosPersitidos.compraProductosSeleccionados.remove(itemAEliminar)
            }
        }

        if (DatosPersitidos.ventaProductosSeleccionados.isNotEmpty()) {
            val itemAEliminar = DatosPersitidos.ventaProductosSeleccionados
                .filterKeys { it.id == producto.id }
                .keys
                .firstOrNull()

            if (itemAEliminar != null) {
                DatosPersitidos.ventaProductosSeleccionados.remove(itemAEliminar)
            }
        }
    }

    private fun actualizarCantidadTransaccion(cantidadDisponible: String) {
        val nuevaCantidad = cantidadDisponible.toInt() - cantidadAntigua.toInt()
        cantidadAntigua = cantidadDisponible
        val producto = this.binding!!.editTextProducto.text.toString().trim()
        if (nuevaCantidad > 0) {

            //crea un registro de compra
            FirebaseFacturaOCompra.guardarDetalleFacturaOCompra(
                "Compra",
                viewModel.obtenerDatosPedido()
            )
            FirebaseProductoFacturadosOComprados.guardarProductoFacturado(
                "ProductosComprados",
                viewModel.productoEditado(productoDetalle, nuevaCantidad),
                "compra",
                requireContext()
            )

            val rootView: View = requireView()
            Utilidades.crearSnackBarr("Se sumaran $nuevaCantidad $producto al inventario", rootView)

        } else if (nuevaCantidad < 0) {

            //crea un registro de venta
            FirebaseFacturaOCompra.guardarDetalleFacturaOCompra(
                "Factura",
                viewModel.obtenerDatosPedido()
            )
            FirebaseProductoFacturadosOComprados.guardarProductoFacturado(
                "ProductosFacturados",
                viewModel.productoEditado(productoDetalle, nuevaCantidad.absoluteValue),
                "venta",
                requireContext()
            )
            val rootView = requireView()
            Utilidades.crearSnackBarr(
                "Se restaran $nuevaCantidad $producto al inventario",
                rootView
            )
        }
        val transaccionesPendientes =
            UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos(requireContext())
        FirebaseProductos.transaccionesCambiarCantidad(requireContext(), transaccionesPendientes)


    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Si la acción fue tomar una foto con la cámara
        if (requestCode == TomarFotoYGaleria.CAMARA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            // Recortar la imagen usando la biblioteca CropImage
            CropImage.activity(TomarFotoYGaleria.imagenUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                //.setAspectRatio(1, 1)
                .start(requireContext(), this)
        }

        // Si la acción fue elegir una imagen de la galería
        if (requestCode == TomarFotoYGaleria.GALERIA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Obtener la URI de la imagen seleccionada de la galería
            val uri = data?.data
            // Recortar la imagen usando la biblioteca CropImage
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                //.setAspectRatio(1, 1)

                .start(requireContext(), this)

        }
        // Si la acción fue recortar la imagen usando CropImage
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                // Obtener el archivo de la imagen recortada
                val file = File(result.uri.path!!)
                if (file.exists()) {
                    // Cargar la imagen recortada en el ImageView
                    bitmapFoto = BitmapFactory.decodeFile(file.absolutePath)
                    binding?.imageViewFoto?.setImageBitmap(bitmapFoto)
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                // Mostrar un mensaje de error si la recortada no fue exitosa
                val error = result.error
                Toast.makeText(
                    requireContext(),
                    "Error al recortar la imagen: $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}