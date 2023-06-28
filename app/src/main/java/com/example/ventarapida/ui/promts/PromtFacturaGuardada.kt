package com.example.ventarapida.ui.promts

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.example.ventarapida.R
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloProductoFacturado
import com.example.ventarapida.procesos.FirebaseProductoFacturadosOComprados
import com.example.ventarapida.procesos.FirebaseProductos
import com.example.ventarapida.procesos.Utilidades.escribirFormatoMoneda
import com.example.ventarapida.procesos.Utilidades.formatoMonenda
import com.example.ventarapida.procesos.UtilidadesBaseDatos
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import com.example.ventarapida.MainActivity
import com.example.ventarapida.datos.ModeloFactura
import com.example.ventarapida.procesos.FirebaseFacturaOCompra.guardarDetalleFacturaOCompra
import com.example.ventarapida.procesos.FirebaseProductoFacturadosOComprados.actualizarPrecioDescuento
import com.example.ventarapida.procesos.UtilidadesBaseDatos.editarProductoTransaccion
import java.util.UUID


class PromtFacturaGuardada() {


    fun editarProducto( tipo:String, item: ModeloProductoFacturado, context: Context){
        val dialogBuilder = AlertDialog.Builder(context)

        var tablaReferencia=""
        if (tipo.equals("compra")) tablaReferencia="ProductosComprados"
        if (tipo.equals("venta"))  tablaReferencia="ProductosFacturados"

// Inflar el layout para el diálogo
        // Inflar el layout para el diálogo
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.promt_factura, null)
        dialogBuilder.setView(dialogView)

        val editTextProducto = dialogView.findViewById<EditText>(R.id.promt_producto)
        val editTextCantidad = dialogView.findViewById<EditText>(R.id.promt_cantidad)
        val editTextPrecio = dialogView.findViewById<EditText>(R.id.promt_precio)

        // Seleccionar tode el contenido del EditText al recibir foco
        editTextProducto.setSelectAllOnFocus(true)
        editTextCantidad.setSelectAllOnFocus(true)
        editTextPrecio.setSelectAllOnFocus(true)

        editTextProducto.setText( item.producto)
        editTextCantidad.setText(item.cantidad)
        editTextPrecio.setText(item.venta.formatoMonenda())

        editTextPrecio.escribirFormatoMoneda()



// Configurar el botón "Aceptar"
        dialogBuilder.setPositiveButton("Cambiar") { dialogInterface, i ->

            val nuevoNombre=editTextProducto.text.toString().takeIf { it.isNotEmpty() } ?: "Item"
            val nuevaCantidad = editTextCantidad.text.toString().takeIf { it.isNotEmpty() } ?: "0"
            val nuevoPrecio = (editTextPrecio.text.toString().replace(".", "")).takeIf { it.isNotEmpty() } ?: "0"

            val cantidadAnterior = item.cantidad

            item.producto=nuevoNombre
            item.cantidad=nuevaCantidad
            item.venta=nuevoPrecio


            val diferenciaCantidad = nuevaCantidad.toInt() - cantidadAnterior.toInt()

            if(diferenciaCantidad!=0){
                //hacer una cola para restar o sumar las cantidades del inventario
                val productosSeleccionados: MutableMap<ModeloProducto, Int> = mutableMapOf()
                val nuevoProducto = ModeloProducto(id = item.id_producto)
                //multiplicamos *-1  para que en vez de restar sume en la base de datos
                productosSeleccionados[nuevoProducto] = diferenciaCantidad
                val listaProductosEditar = arrayListOf<ModeloProductoFacturado>()

                productosSeleccionados.forEach{ (producto, cantidadSeleccionada)->
                    //calculamos el precio descuento para tener la referencia para los reportes
                    if (cantidadSeleccionada!=0){

                        val productoFacturado = ModeloProductoFacturado(
                            id_producto_pedido = UUID.randomUUID().toString(),
                            id_producto = producto.id,
                            id_vendedor = MainActivity.datosUsuario.id,
                            vendedor = MainActivity.datosUsuario.nombre,
                            producto = producto.nombre,
                            cantidad = cantidadSeleccionada.toString(),
                            costo = producto.p_compra,
                            venta = producto.p_diamante,
                            imagenUrl =producto.url
                        )
                        listaProductosEditar.add(productoFacturado)

                        editarProductoTransaccion(context,tipo,diferenciaCantidad,productoFacturado)

                    }
                }
            }
            val listaProductosFacturados = arrayListOf<ModeloProductoFacturado>()
            listaProductosFacturados.add(item)
            if(nuevaCantidad.toInt()!=0){
                //si es edicion no crea aqui la cola de transaccion
                FirebaseProductoFacturadosOComprados.guardarProductoFacturado(tablaReferencia,listaProductosFacturados,"edicion",context)

            }else{
                FirebaseProductoFacturadosOComprados.eliminarProductoFacturado(tablaReferencia,listaProductosFacturados,context,tipo)
                Toast.makeText(context, cantidadAnterior +"x "+item.producto+" Eliminados", Toast.LENGTH_LONG).show()
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
        dialogBuilder.setPositiveButton("Cambiar") { dialogInterface, i ->

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
        dialogBuilder.setNegativeButton("Cancelar") { dialogInterface, i ->
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
        dialogBuilder.setPositiveButton("Cambiar") { dialogInterface, i ->


            val nuevoDescuento=editTextDescuento.text.toString().ifBlank { "0" }
            val nuevoEnvio = editTextEnvio.text.toString().ifBlank { "0" }

            val updates = hashMapOf<String, Any>(
                "id_pedido" to datosFactura.id_pedido,
                "descuento" to nuevoDescuento,
                "envio" to nuevoEnvio,
            )

            if (datosFactura.descuento != editTextDescuento.text.toString()) actualizarPrecioDescuento(datosFactura.id_pedido,nuevoDescuento.toDouble())
            guardarDetalleFacturaOCompra("Factura",updates)


        }

// Configurar el botón "Cancelar"
        dialogBuilder.setNegativeButton("Cancelar") { dialogInterface, i ->
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
        dialogBuilder.setPositiveButton("Cambiar") { dialogInterface, i ->

            val nuevoNombre=editTextCliente.text.toString()

            val updates = hashMapOf<String, Any>(
                "id_pedido" to datosFactura.id_pedido,
                "nombre" to nuevoNombre,
            )
            guardarDetalleFacturaOCompra("Compra",updates)
        }

// Configurar el botón "Cancelar"
        dialogBuilder.setNegativeButton("Cancelar") { dialogInterface, i ->
            // No hacer nada
        }

// Mostrar el diálogo
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}