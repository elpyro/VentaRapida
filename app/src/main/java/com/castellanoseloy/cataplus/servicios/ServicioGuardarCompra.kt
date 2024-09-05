package com.castellanoseloy.cataplus.servicios
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.castellanoseloy.cataplus.datos.ModeloProducto
import com.castellanoseloy.cataplus.datos.ModeloProductoFacturado
import com.castellanoseloy.cataplus.datos.ModeloTransaccionSumaRestaProducto
import com.castellanoseloy.cataplus.procesos.FirebaseFacturaOCompra
import com.castellanoseloy.cataplus.procesos.FirebaseProductoFacturadosOComprados
import com.castellanoseloy.cataplus.procesos.FirebaseProductos
import com.castellanoseloy.cataplus.procesos.Utilidades
import java.util.UUID

class ServicioGuadarCompra : JobIntentService() {


    companion object {
        private var servicioListener: ServicioListener? = null

        fun setServicioListener(listener: ServicioListener) {
            servicioListener = listener
        }
    }

    override fun onHandleWork(intent: Intent) {
        val datosPedido = intent.getSerializableExtra("datosPedido") as? HashMap<String, Any>

        val listasConvertida=
            datosPedido?.let { convertirLista(DatosPersitidos.ventaProductosSeleccionados, it) }

        subirDatos(datosPedido!!, listasConvertida!!.first!! , applicationContext!!)
        FirebaseProductos.transaccionesCambiarCantidad(applicationContext, listasConvertida.second)

        servicioListener?.onServicioTerminado()
    }

    fun subirDatos(
        datosPedido: HashMap<String, Any>,
        listaProductosFacturados:ArrayList<ModeloProductoFacturado>,
        context: Context
    ) {

        FirebaseFacturaOCompra.guardarDetalleFacturaOCompra("Compra",datosPedido)
        FirebaseProductoFacturadosOComprados.guardarProductoFacturado("ProductosComprados",listaProductosFacturados,"compra",context)

        FirebaseFacturaOCompra.guardarDetalleFacturaOCompra("Factura",datosPedido)
        FirebaseProductoFacturadosOComprados.guardarProductoFacturado("ProductosFacturados",listaProductosFacturados,"venta",context)

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
                    porcentajeDescuento = descuento,
                    productoEditado = producto.editado,
                    fecha = fechaActual,
                    hora = horaActual,
                    imagenUrl = producto.url,
                    fechaBusquedas = Utilidades.obtenerFechaUnix(),
                    estadoRecaudo = recaudo,
                    listaVariables = producto.listaVariables

                )
                listaProductosFacturados.add(productoFacturado)

                val restarProducto = ModeloTransaccionSumaRestaProducto(
                    idTransaccion = id_producto_pedido,  //la transaccion tiene el mismo id
                    idProducto = producto.id,
                    cantidad = (cantidadSeleccionada).toString(),
                    subido ="false",
                    listaVariables = producto.listaVariables
                )

                listaDescontarInventario.add(restarProducto)
            }
        }

        return Pair(listaProductosFacturados, listaDescontarInventario)
    }
}