package com.castellanoseloy.cataplus.ui.promts

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.castellanoseloy.cataplus.R
import com.castellanoseloy.cataplus.datos.ModeloProducto
import com.castellanoseloy.cataplus.datos.ModeloProductoFacturado
import com.castellanoseloy.cataplus.procesos.FirebaseProductoFacturadosOComprados
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.datos.ModeloFactura
import com.castellanoseloy.cataplus.datos.Variable

import com.castellanoseloy.cataplus.procesos.FirebaseFacturaOCompra.guardarDetalleFacturaOCompra
import com.castellanoseloy.cataplus.procesos.FirebaseProductoFacturadosOComprados.actualizarPrecioDescuento
import com.castellanoseloy.cataplus.procesos.FirebaseProductos
import com.castellanoseloy.cataplus.procesos.Utilidades
import com.castellanoseloy.cataplus.procesos.UtilidadesBaseDatos.editarProductoTransaccion
import com.google.android.material.textfield.TextInputLayout
import java.util.UUID


class PromtFacturaGuardada {


    private lateinit var nuevaCantidad: String
    private lateinit var textInputLayoutCantidad: TextInputLayout
    private var button_cambiarCantidad: Button? = null
    private lateinit var tablaReferencia: String
    private lateinit var tipoRecibido: String
    private lateinit var contextoRecibido: Context
    private lateinit var itemRecibido: ModeloProductoFacturado
    private lateinit var itemSinCambios: ModeloProductoFacturado
    private var imageView_foto: ImageView? = null
    private var editTextPrecio: EditText? = null
    private var editTextCantidad: EditText? = null
    private var editTextProducto: EditText? = null

    fun editarProducto(tipo:String, item: ModeloProductoFacturado, context: Context){
        itemRecibido=item
        itemSinCambios=item.copy()
        contextoRecibido=context
        tipoRecibido=tipo

        val dialogBuilder = AlertDialog.Builder(context)

        tablaReferencia=""

        if (tipo == "compra"){
            tablaReferencia="ProductosComprados"
            var lista= DatosPersitidos.compraProductosSeleccionados
        }
        if (tipo == "venta"){
            tablaReferencia="ProductosFacturados"
            var lista= DatosPersitidos.ventaProductosSeleccionados
        }

// Inflar el layout para el diálogo
        // Inflar el layout para el diálogo
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.promt_factura, null)
        dialogBuilder.setView(dialogView)

        editTextProducto = dialogView.findViewById(R.id.promt_producto)
        editTextCantidad = dialogView.findViewById(R.id.promt_cantidad)
        editTextPrecio = dialogView.findViewById(R.id.promt_precio)
        imageView_foto= dialogView.findViewById(R.id.imageView_foto)
        button_cambiarCantidad=dialogView.findViewById(R.id.button_cambiarCantidad)
        textInputLayoutCantidad=dialogView.findViewById(R.id.text_input_layout_cantidad)


        // Seleccionar tode el contenido del EditText al recibir foco
        editTextProducto!!.setSelectAllOnFocus(true)
        editTextCantidad!!.setSelectAllOnFocus(true)
        editTextPrecio!!.setSelectAllOnFocus(true)

        Utilidades.cargarImagen(item.imagenUrl, imageView_foto!!)
        editTextProducto!!.setText( item.producto)
        editTextCantidad!!.setText(item.cantidad)
        if (tipo == "venta") editTextPrecio!!.setText(item.venta)
        if (tipo == "compra") editTextPrecio!!.setText(item.costo)


        // Si el producto tiene variantes, mostrar el prompt de edición de variantes cuando se presiona el el button_cambiarCantidad
        configurarVariantes(item, context)

        // Configurar el botón "Aceptar"
        dialogBuilder.setPositiveButton("Cambiar") { _, _ ->
            if(itemSinCambios.listaVariables.isNullOrEmpty()) {
                nuevaCantidad = editTextCantidad?.text.toString().takeIf { it.isNotEmpty() } ?: "0"
                modificar(nuevaCantidad)
            }else{
                modificar(nuevaCantidad)
            }

        }


        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            // Restaurar la copia original del item si se cancela
            itemRecibido.listaVariables = itemSinCambios.listaVariables
            itemRecibido.cantidad = itemSinCambios.cantidad
        }

        if(itemSinCambios.listaVariables.isNullOrEmpty()) {
            dialogBuilder.setNeutralButton("Eliminar") { _, _ ->
                modificar("0")
            }
        }




        val alertDialog = dialogBuilder.create()
        alertDialog.show()



    }

    private fun configurarVariantes(
        item: ModeloProductoFacturado,
        context: Context
    ) {
        if (!item.listaVariables.isNullOrEmpty()) {
            textInputLayoutCantidad.visibility = View.GONE
            button_cambiarCantidad?.visibility = View.VISIBLE
            button_cambiarCantidad?.setText("Cantidad: " + item.cantidad)

            FirebaseProductos.buscarProductoPorId(item.id_producto!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val producto = task.result
                        if (producto != null) {
                            button_cambiarCantidad?.setOnClickListener {
                                val promtEditarVariantesSeleccionadas = PromtSeleccionarVariantes()


                                val productoCompleto = convertirToModeloProducto()


                                var productoSeleccionados = mutableMapOf<ModeloProducto, Int>()

                                productoSeleccionados[productoCompleto.copy()] =
                                    itemRecibido.cantidad.toInt()

                                promtEditarVariantesSeleccionadas.agregar(
                                    context,
                                    producto.copy(),
                                    productoSeleccionados
                                ) {
                                    Log.d("PromtFacturaGuardada", "Lista actualizada: $it")
                                    // Actualizar la cantidad en el item recibido
                                    itemRecibido.listaVariables = it
                                    Log.d("PromtFacturaGuardada", "itemRecibido.listaVariables: \n ${itemRecibido.listaVariables} \n ${itemSinCambios.listaVariables} ")
                                    //cuenta las cantidades de las variantes
                                    val cantidadTotalDeVariables = it.sumOf { it.cantidad ?: 0 }
                                    itemRecibido.cantidad = cantidadTotalDeVariables.toString()
                                    button_cambiarCantidad?.setText("Cantidad: " + itemRecibido.cantidad)
                                    nuevaCantidad=cantidadTotalDeVariables.toString()

                                }
                            }
                        }

                    }
                }
            Log.d("edicion", "Producto con variables: ${item.producto}")

        }
    }

    private fun convertirToModeloProducto(): ModeloProducto {
        val productoCompleto = ModeloProducto(
            cantidad = itemRecibido.cantidad,
            codigo = "",
            descripcion = "",
            fecha_ultima_modificacion = "",
            id = itemRecibido.id_producto,
            nombre = itemRecibido.producto,
            p_compra = "",
            p_diamante = "",
            url = "",
            descuento = "",
            precio_descuento = "",
            comentario = "",
            proveedor = "",
            editado = "",
            listaVariables = itemRecibido.listaVariables
        )
        return productoCompleto
    }

    fun modificar(nuevaCantidad:String){
        val nuevoNombre=editTextProducto?.text.toString().takeIf { it.isNotEmpty() } ?: "Item"
        val nuevoPrecio = (editTextPrecio?.text.toString()).takeIf { it.isNotEmpty() } ?: "0"
        var diferenciaListaVariables= mutableListOf<Variable>()

        val cantidadAnterior = itemSinCambios.cantidad

        itemRecibido.producto=nuevoNombre
        itemRecibido.cantidad=nuevaCantidad
        itemRecibido.productoEditado="true"

        var multiplicador=1
        if (tipoRecibido == "venta")  {
            itemRecibido.venta=nuevoPrecio
        }
        if (tipoRecibido == "compra")  {
            itemRecibido.costo=nuevoPrecio
            multiplicador=-1

        }

        val diferenciaCantidad = nuevaCantidad.toInt() - cantidadAnterior.toInt()

        var descuento= 0.0
        if(itemRecibido.porcentajeDescuento.isNotEmpty())descuento=itemRecibido.porcentajeDescuento.toDouble()
        if(descuento!=0.0){
            val precioNuevoDescuento=(nuevoPrecio.toDouble() /descuento)
            val precioNuevoDescuento2=precioNuevoDescuento-nuevoPrecio.toDouble()
            itemRecibido.precioDescuentos=precioNuevoDescuento2.toString()
        }else{
            itemRecibido.precioDescuentos=nuevoPrecio
        }

        var cambiosVariable: Boolean
        if(itemRecibido.listaVariables!=itemSinCambios.listaVariables){
            Log.d("PromtFacturaGuardada","Los item cambiaron: "+itemRecibido.listaVariables.toString())
                cambiosVariable = true

                diferenciaListaVariables = Utilidades.calcularDiferenciasDeVariables(itemSinCambios.listaVariables,itemRecibido.listaVariables,multiplicador)
            } else {
                Log.d("PromtFacturaGuardada", "No hubo cambios en las variables")
                cambiosVariable = false
            }




        if(diferenciaCantidad!=0 || cambiosVariable){
            //hacer una cola para restar o sumar las cantidades del inventario
            val productosSeleccionados: MutableMap<ModeloProducto, Int> = mutableMapOf()
            val nuevoProducto = ModeloProducto(id = itemRecibido.id_producto)
            //multiplicamos *-1  para que en vez de restar sume en la base de datos
            productosSeleccionados[nuevoProducto] = diferenciaCantidad
            val listaProductosEditar = arrayListOf<ModeloProductoFacturado>()

            productosSeleccionados.forEach{ (producto, cantidadSeleccionada)->
                //calculamos el precio descuento para tener la referencia para los reportes
                if (cantidadSeleccionada!=0){

                    val productoFacturado = ModeloProductoFacturado(
                        id_producto_pedido = UUID.randomUUID().toString(),
                        id_producto = producto.id,
                        id_vendedor = DatosPersitidos.datosUsuario.id,
                        vendedor = DatosPersitidos.datosUsuario.nombre,
                        producto = producto.nombre,
                        precioDescuentos= itemRecibido.precioDescuentos,
                        cantidad = cantidadSeleccionada.toString(),
                        costo = producto.p_compra,
                        venta = producto.p_diamante,
                        imagenUrl = producto.url,
                        listaVariables =   diferenciaListaVariables
                    )

                    listaProductosEditar.add(productoFacturado)

                    editarProductoTransaccion(
                        contextoRecibido,
                        tipoRecibido,
                        diferenciaCantidad,
                        producto.id,
                        productoFacturado,
                        cambiosVariable
                    )

                }

            }
        }
        val listaProductosFacturados = arrayListOf<ModeloProductoFacturado>()
        listaProductosFacturados.add(itemRecibido)
        if(nuevaCantidad.toInt()!=0 ){
            //si es edicion no crea aqui la cola de transaccion
            FirebaseProductoFacturadosOComprados.guardarProductoFacturado(
                tablaReferencia,
                listaProductosFacturados,
                "edicion",
                contextoRecibido
            )

        } else {
            FirebaseProductoFacturadosOComprados.eliminarProductoFacturado(
                tablaReferencia,
                listaProductosFacturados,
                contextoRecibido,
                tipoRecibido
            )
            Toast.makeText(
                contextoRecibido,
                cantidadAnterior + "x " + itemRecibido.producto + " Eliminados",
                Toast.LENGTH_LONG
            ).show()
        }

    }




    fun promtEditarDatosCliente(datosFactura: ModeloFactura, context: FragmentActivity,vista : View) {
        val dialogBuilder = AlertDialog.Builder(context)

// Inflar el layout para el diálogo
        // Inflar el layout para el diálogo
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.promt_datos_cliente, null)
        dialogBuilder.setView(dialogView)

        val editTextCliente = dialogView.findViewById<EditText>(R.id.edit_text_tienda)
        val editTextTelefono = dialogView.findViewById<EditText>(R.id.editText_telefono)
        val editTextDocumento= dialogView.findViewById<EditText>(R.id.editText_documento)
        val editTextDireccion = dialogView.findViewById<EditText>(R.id.editText_direccion)
        val botonBuscar=dialogView.findViewById<ImageButton>(R.id.imageButton_buscarCliente)

        // Seleccionar tode el contenido del EditText al recibir foco
        editTextCliente.setSelectAllOnFocus(true)
        editTextTelefono.setSelectAllOnFocus(true)
        editTextDocumento.setSelectAllOnFocus(true)
        editTextDireccion.setSelectAllOnFocus(true)

            editTextCliente.setText( datosFactura.nombre)
            editTextTelefono.setText(datosFactura.telefono)
            editTextDocumento.setText(datosFactura.documento)
            editTextDireccion.setText(datosFactura.direccion)



// Configurar el botón "Aceptar"
        dialogBuilder.setPositiveButton("Cambiar") { _, _ ->

            val nuevoNombre=editTextCliente.text.toString()
            val nuevoTelefono = editTextTelefono.text.toString()
            val nuevoDocumento = editTextDocumento.text.toString()
            val nuevaDireccion= editTextDireccion.text.toString()

            val updates = hashMapOf<String, Any>(
                "id_pedido" to datosFactura.id_pedido,
                "nombre" to nuevoNombre,
                "telefono" to nuevoTelefono,
                "documento" to nuevoDocumento,
                "direccion" to nuevaDireccion
            )
            guardarDetalleFacturaOCompra("Factura",updates)
          }

// Configurar el botón "Cancelar"
        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            // No hacer nada
        }

// Mostrar el diálogo
        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        botonBuscar.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("modeloFactura", datosFactura)
            Navigation.findNavController(vista).navigate(R.id.listaClientes,bundle)
            alertDialog.dismiss()
        }
    }

    fun promtEditarModificadoresFactura(datosFactura: ModeloFactura, context: FragmentActivity) {
        val dialogBuilder = AlertDialog.Builder(context)

        // Inflar el layout para el diálogo
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.promt_modificadores_factura, null)
        dialogBuilder.setView(dialogView)

        val editTextDescuento = dialogView.findViewById<EditText>(R.id.edit_descuento)
        val editTextEnvio = dialogView.findViewById<EditText>(R.id.editText_envio)

        // Seleccionar tode el contenido del EditText al recibir foco
        editTextDescuento.setSelectAllOnFocus(true)
        editTextEnvio.setSelectAllOnFocus(true)

        editTextDescuento.setText( datosFactura.descuento)
        editTextEnvio.setText(datosFactura.envio)

// Configurar el botón "Aceptar"
        dialogBuilder.setPositiveButton("Cambiar") { _, _ ->


            val nuevoDescuento=editTextDescuento.text.toString().ifBlank { "0" }
            val nuevoEnvio = editTextEnvio.text.toString().ifBlank { "0" }

            val updates = hashMapOf<String, Any>(
                "id_pedido" to datosFactura.id_pedido,
                "descuento" to nuevoDescuento,
                "envio" to nuevoEnvio
            )

            if (datosFactura.descuento != editTextDescuento.text.toString()) actualizarPrecioDescuento(datosFactura.id_pedido,nuevoDescuento.toDouble())
            guardarDetalleFacturaOCompra("Factura",updates)


        }

// Configurar el botón "Cancelar"
        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            // No hacer nada
        }

// Mostrar el diálogo
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    fun promtEditarDatosCompra(datosFactura: ModeloFactura, context: FragmentActivity) {
        val dialogBuilder = AlertDialog.Builder(context)

// Inflar el layout para el diálogo
        // Inflar el layout para el diálogo
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.promt_datos_compra, null)
        dialogBuilder.setView(dialogView)

        val editTextCliente = dialogView.findViewById<EditText>(R.id.edit_text_tienda)

        // Seleccionar tode el contenido del EditText al recibir foco
        editTextCliente.setSelectAllOnFocus(true)

        editTextCliente.setText( datosFactura.nombre)

// Configurar el botón "Aceptar"
        dialogBuilder.setPositiveButton("Cambiar") { _, _ ->

            val nuevoNombre=editTextCliente.text.toString()

            val updates = hashMapOf<String, Any>(
                "id_pedido" to datosFactura.id_pedido,
                "nombre" to nuevoNombre,
            )
            guardarDetalleFacturaOCompra("Compra",updates)
        }

// Configurar el botón "Cancelar"
        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            // No hacer nada
        }

// Mostrar el diálogo
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}