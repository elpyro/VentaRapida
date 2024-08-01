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
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.FragmentDetalleProductoBinding
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.datos.ModeloUsuario
import com.castellanoseloy.ventarapida.procesos.FirebaseFacturaOCompra
import com.castellanoseloy.ventarapida.procesos.FirebaseProductoFacturadosOComprados
import com.castellanoseloy.ventarapida.procesos.FirebaseProductos
import com.castellanoseloy.ventarapida.procesos.FirebaseProductos.guardarProducto
import com.castellanoseloy.ventarapida.procesos.TomarFotoYGaleria
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.ocultarTeclado
import com.castellanoseloy.ventarapida.procesos.UtilidadesBaseDatos
import com.castellanoseloy.ventarapida.procesos.VerificarInternet
import com.google.android.material.snackbar.Snackbar
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
        val modeloProducto = bundle?.getSerializable("modelo") as? ModeloProducto


        val listaDeProductos =
            bundle?.getSerializable("listaProductos") as? ArrayList<ModeloProducto>
        val posicionProducto = bundle?.getInt("position")

        if(!listaDeProductos.isNullOrEmpty()) {
            // Llama al método actualizarListaProductos para indicar la lista de productos si existe
            viewModel.actualizarListaProductos(listaDeProductos!!)
        }

        // Inicialización de productosViewModel
        productosViewModel = ViewModelProvider(this).get(DetalleProductoViewModel::class.java)

        // Observa los cambios en detalleProducto y actualiza la UI en consecuencia
        productosViewModel.detalleProducto.observe(viewLifecycleOwner) { detalleProducto ->
            binding?.textViewInformacionAgregarCantidades?.visibility = View.GONE
            changeListenerActived = false
            if (detalleProducto.isNotEmpty()) {
                productoDetalle = detalleProducto[0]
                actualizarCampos(productoDetalle)
            } else {
                cargarSiguienteProducto()
            }
        }
        productosViewModel.mensajeToast.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        binding?.imageViewFoto?.setOnClickListener {
            cargarImagen()
        }

        // Define el botón "Siguiente" y configura su OnClickListener
        binding?.imageViewBotonDerecha?.setOnClickListener {
            cargarSiguienteProducto()
        }

        // Define el botón "Anterior" y configura su OnClickListener
        binding?.imageViewBotonIzquierda?.setOnClickListener {
            cargarAnteriorProducto()
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


        // Indica la posición del producto para abrir el producto seleccionado
        if(!listaDeProductos.isNullOrEmpty()){
            viewModel.actualizarPosiscion(posicionProducto!!)
            verificarPosiciones()
        }


        // Carga el producto en la UI
        cargarProducto(modeloProducto)

        return binding!!.root // Retorna la vista inflada
    }

    private fun mostrarHistorial() {
        val bundle = Bundle()
        bundle.putString("idProducto", id_producto)
        Navigation.findNavController(vista!!).navigate(R.id.historialProducto,bundle)
    }

    //Tomamos la foto resultante de la camara o la galeria y la colocamos en el imageview
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

    private fun actualizarCampos(producto: ModeloProducto) {
        id_producto = producto.id
        binding?.editTextProducto?.setText(producto.nombre)
        binding?.editTextPCompra?.setText(producto.p_compra)
        binding?.editTextPVenta?.setText(producto.p_diamante)
        binding?.editTextCantidad?.setText(producto.cantidad)
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

    private fun cargarAnteriorProducto() {
        // Descremente la posición actual en 1
        viewModel.posicionActual--

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

        val updates = hashMapOf<String, Any>(
            "id" to id_producto.trim(),
            "nombre" to this.binding!!.editTextProducto.text.toString().trim(),
            "p_compra" to this.binding!!.editTextPCompra.text.toString(),
            "p_diamante" to this.binding!!.editTextPVenta.text.toString(),
            "comentario" to binding!!.editTextComentario.text.toString().trim(),
            "proveedor" to binding!!.editTextProveedor.text.toString()
        )

        guardarProducto(updates)

        Toast.makeText(requireContext(), "Producto Actualizado", Toast.LENGTH_LONG).show()

        if (!verificarConexion.verificarConexion(requireContext())) {
            Toast.makeText(
                requireContext(),
                getString(R.string.disponbleEnlaNuebe),
                Toast.LENGTH_LONG
            ).show()
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
                viewModel.productoEditado(productoDetalle,nuevaCantidad),
                "compra",
                requireContext()
            )

            val rootView: View = requireView()
            Utilidades.crearSnackBarr("Se sumaran $nuevaCantidad $producto al inventario",rootView)

        } else if (nuevaCantidad < 0) {

            //crea un registro de venta
            FirebaseFacturaOCompra.guardarDetalleFacturaOCompra(
                "Factura",
                viewModel.obtenerDatosPedido()
            )
            FirebaseProductoFacturadosOComprados.guardarProductoFacturado(
                "ProductosFacturados",
                viewModel.productoEditado(productoDetalle,nuevaCantidad.absoluteValue),
                "venta",
                requireContext()
            )
            val rootView = requireView()
            Utilidades.crearSnackBarr("Se restaran $nuevaCantidad $producto al inventario",rootView)
        }
        val transaccionesPendientes=
            UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos(requireContext())
        FirebaseProductos.transaccionesCambiarCantidad(requireContext(), transaccionesPendientes)


    }




    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        // Invalidar el menú al salir del fragmento para que la barra de menú desaparezca
        requireActivity().invalidateOptionsMenu()
    }

}