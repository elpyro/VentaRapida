package com.example.ventarapida.ui.informacionProducto

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
import com.example.ventarapida.R
import com.example.ventarapida.databinding.FragmentInformacionProductoBinding
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.procesos.Utilidades
import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import com.squareup.picasso.Picasso


class InformacionProducto : Fragment() {

    private var modeloProducto: ModeloProducto? = null
    private lateinit var binding: FragmentInformacionProductoBinding
    private lateinit var vista: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInformacionProductoBinding.inflate(inflater, container, false)

        val bundle = arguments
        modeloProducto = bundle?.getSerializable("modelo") as? ModeloProducto

        cargarProducto(modeloProducto)

        setHasOptionsMenu(true)
        return binding!!.root
    }

    private fun cargarProducto(modeloProducto: ModeloProducto?) {

        binding.textViewNombre.setText(modeloProducto?.nombre)
        if(modeloProducto?.comentario?.isEmpty() == false){
            binding.textViewComentario.visibility=View.VISIBLE
            binding.textViewComentario.setText(modeloProducto?.comentario)
        }

        binding.textViewPrecio.setText(modeloProducto?.p_diamante?.formatoMonenda())
        Picasso.get().load(modeloProducto?.url).into(binding?.photoView)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vistas_pdf, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


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
        val precio=modeloProducto?.p_diamante?.formatoMonenda()
        // Crear el mensaje que contiene el nombre del producto
        val message = "$productName Precio: $precio"

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