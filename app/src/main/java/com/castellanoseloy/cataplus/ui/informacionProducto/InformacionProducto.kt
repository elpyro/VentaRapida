package com.castellanoseloy.cataplus.ui.informacionProducto

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.castellanoseloy.cataplus.R
import com.castellanoseloy.cataplus.databinding.FragmentInformacionProductoBinding
import com.castellanoseloy.cataplus.datos.ModeloProducto
import com.castellanoseloy.cataplus.procesos.FirebaseProductos
import com.castellanoseloy.cataplus.procesos.Utilidades
import com.castellanoseloy.cataplus.procesos.Utilidades.formatoMonenda
import com.castellanoseloy.cataplus.servicios.DatosPersitidos.Companion.datosUsuario


@Suppress("DEPRECATION")
class InformacionProducto : Fragment() {

    private var modeloProducto: ModeloProducto? = null
    private lateinit var binding: FragmentInformacionProductoBinding
    private lateinit var vista: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInformacionProductoBinding.inflate(inflater, container, false)

        val bundle = arguments
        modeloProducto = bundle?.getSerializable("modelo") as? ModeloProducto


        cargarProducto(modeloProducto){ producto ->
           //actualizar el modelo por si hay algun cambio
            if (producto != null) {
                modeloProducto=producto
            }
        }


        setHasOptionsMenu(true)

        listeners()
        return binding.root
    }

    private fun listeners() {
        if(datosUsuario.perfil=="Administrador"){
            binding?.buttonEditar?.setOnClickListener { irEdicionProducto() }
        }else{
            binding?.buttonEditar?.visibility=View.GONE
        }

    }

    private fun irEdicionProducto() {
        val bundle = Bundle()
        bundle.putString("idProducto", modeloProducto!!.id)
        Navigation.findNavController(vista).navigate(R.id.detalleProducto,bundle)
    }

    private fun cargarProducto(modeloProducto: ModeloProducto?, callback: (ModeloProducto?) -> Unit) {
        FirebaseProductos.buscarProductoPorId(modeloProducto!!.id)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val producto = task.result
                    binding.textViewNombre.text = producto?.nombre
                    if (producto?.comentario?.isEmpty() == false) {
                        binding.textViewComentario.visibility = View.VISIBLE
                        binding.textViewComentario.text = producto.comentario
                    }

                    binding.textViewPrecio.text = producto?.p_diamante?.formatoMonenda()
                    producto?.url?.let { Utilidades.cargarImagen(it, binding.photoView) }

                    // Llamar al callback con el producto
                    callback(producto)
                }
            }
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vistas_pdf, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_compartir -> {
                compartir()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun obtenerUriImagen(imageView: ImageView?, context: Context): Uri? {
        if (imageView?.isVisible == true) {
            val drawable = imageView.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                return Utilidades.obtenerImageUriParaCompartir(bitmap, context)
            }
        }
        return null
    }

    private fun compartir(){
        // Obtener la Uri de la imagen desde la URL del producto

        val imageUrl = obtenerUriImagen(binding.photoView,requireContext())

        // Obtener el nombre del producto
        val productName = modeloProducto?.nombre
        val precio = modeloProducto?.p_diamante?.formatoMonenda()
        val descripcion = modeloProducto?.comentario

// Crear el mensaje que contiene el nombre del producto
        val stringBuilder = StringBuilder()
        stringBuilder.append("$productName \n*Precio: $precio* \n$descripcion")

// Agregar las variantes si existen y si la cantidad es mayor o igual a 1
        modeloProducto?.listaVariables?.let { lista ->
            val variablesFiltradas = lista.filter { it.cantidad >= 1 } // Filtrar las variables con cantidad mayor o igual a 1
            if (variablesFiltradas.isNotEmpty()) {
                stringBuilder.append("\n -Variantes: \n")
                val variablesString = variablesFiltradas.joinToString("\n") { variable ->
                    "${variable.nombreVariable}: ${variable.cantidad}"
                }
                stringBuilder.append(variablesString)
            }
        }

        val message = stringBuilder.toString()



        // Llamar a la función para compartir
        compatirInformacionPrincipal(listOf(imageUrl) as List<Uri>, message, requireContext())
    }


    private fun compatirInformacionPrincipal(imageUris: List<Uri>, message: String, context: Context) {
        // Verifica si hay al menos una imagen y si el mensaje no está vacío
        if (imageUris.isNotEmpty() && message.isNotBlank()) {
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_TEXT, message)

            // Asegúrate de que imageUris sea una lista válida de Uri
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // Verifica si hay aplicaciones disponibles para manejar la intención
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "Choose bar"))
            } else {
                // Manejar el caso en el que no hay aplicaciones disponibles
                Toast.makeText(context, "No apps available to share.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Manejar el caso en el que no hay imágenes o mensaje vacío
            Toast.makeText(context, "No images or empty message to share.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view
    }

}