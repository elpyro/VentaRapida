import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.R
import com.castellanoseloy.cataplus.databinding.FragmentClienteAgregarModificarBinding

import com.castellanoseloy.cataplus.datos.ModeloClientes
import com.castellanoseloy.cataplus.procesos.FirebaseClientes.eliminarCliente
import com.castellanoseloy.cataplus.procesos.FirebaseClientes.guardarCliente
import com.castellanoseloy.cataplus.procesos.Utilidades.ocultarTeclado
import java.util.UUID

@Suppress("DEPRECATION")
class ClienteAgregarModificar : Fragment() {

    private var binding: FragmentClienteAgregarModificarBinding? = null
    private lateinit var vista: View
    private var idCliente = ""
    private var clienteNuevo = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClienteAgregarModificarBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        val bundle = arguments
        val modeloCliente = bundle?.getSerializable("modelo") as? ModeloClientes

        //agregar codigo de area por defecto
        binding?.editTextTelefono?.setText(DatosPersitidos.edit_text_preference_codigo_area + " ")

        if (modeloCliente != null) {
            cargarDatos(modeloCliente)
            idCliente = modeloCliente.id
            clienteNuevo = false
        } else {
            idCliente = UUID.randomUUID().toString()
        }


        return binding!!.root
    }

    private fun cargarDatos(modeloCliente: ModeloClientes) {
        val nombre = binding?.editTextCliente
        val telefono = binding?.editTextTelefono
        val documento = binding?.editTextDocumento
        val direccion = binding?.editTextDireccion

        nombre?.setText(modeloCliente.nombre)
        telefono?.setText(modeloCliente.telefono)
        documento?.setText(modeloCliente.documento)
        direccion?.setText(modeloCliente.direccion)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista = view
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menuproducto, menu)
        val item = menu.findItem(R.id.action_camara)
        val item_eliminar = menu.findItem(R.id.action_eliminar)
        item.isVisible = false
        if (clienteNuevo) item_eliminar.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {


            R.id.action_guardar -> {
                ocultarTeclado(requireContext(), vista)

                if (binding!!.editTextCliente.text.toString().isEmpty()) {
                    binding!!.editTextCliente.error = "Obligatorio"
                    return true
                }
                DatosPersitidos.progressDialog?.show()
                val updates = hashMapOf<String, Any>(
                    "id" to idCliente,
                    "nombre" to binding?.editTextCliente?.text.toString(),
                    "documento" to binding?.editTextDocumento?.text.toString(),
                    "telefono" to binding?.editTextTelefono?.text.toString(),
                    "direccion" to binding?.editTextDireccion?.text.toString()
                )

                guardarCliente(updates)

                DatosPersitidos.progressDialog?.dismiss()
                findNavController().popBackStack()

                return true
            }

            R.id.action_eliminar -> {

                ocultarTeclado(requireContext(), vista)

                // Crear el diálogo de confirmación
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Eliminar producto")
                builder.setIcon(R.drawable.logo2_compra_rapidita)
                builder.setMessage("¿Estás seguro de que deseas eliminar este producto?")
                builder.setPositiveButton("Eliminar") { _, _ ->
                    if (idCliente.isNotEmpty()) eliminarCliente(idCliente)

                    Toast.makeText(requireContext(), "Cliente Eliminado", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()

                }
                builder.setNegativeButton("Cancelar", null)
                builder.show()

                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}