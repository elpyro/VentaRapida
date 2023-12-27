package com.castellanoseloy.ventarapida.ui.registros.pestanas

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.datos.ModeloFactura
import com.castellanoseloy.ventarapida.datos.ModeloProductoFacturado
import com.castellanoseloy.ventarapida.procesos.ProductosCompradosFacturadosDiffCallback
import com.castellanoseloy.ventarapida.procesos.RegistrosDiffCallback
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat

import java.util.*


class HistorialProductoAdaptador(
    var listaFacturas: MutableList<ModeloProductoFacturado>,
) : RecyclerView.Adapter<HistorialProductoAdaptador.FacturaViewHolder>() {
    private var progressDialog: ProgressDialog? = null
    // Este método se llama cuando RecyclerView necesita crear un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        // Inflar el diseño del item_producto para crear la vista del ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_factura_ventas, parent, false)

        return FacturaViewHolder(view)
    }

    // Este método se llama cuando RecyclerView necesita mostrar un elemento en una posición determinada
    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {

        // Obtener la clave del elemento que corresponde a la posición
        val Factura =listaFacturas[position]


        // Vincular los datos del producto con la vista del ViewHolder
        holder.bind(Factura)
        holder.cardView.isClickable = true
        // Verificar si la vista está habilitada para interactuar
        if (holder.cardView.isClickable) {

            holder.cardView.setOnClickListener {
                progressDialog?.show() // Mostrar el ProgressDialog
                // Deshabilitar la interacción de la vista
                holder.cardView.isClickable = false

                val tablaReferencia =
                    if (Factura.tipoOperacion.equals("Surtido")) "ProductosComprados" else "ProductosFacturados"
                buscarFactura(tablaReferencia, Factura.id_pedido) { facturas ->
                    // Aquí obtienes la lista de facturas y puedes realizar las acciones necesarias
                    for (factura in facturas) {
                        if (!Factura.tipoOperacion.equals("Surtido")) onClickItem?.invoke(factura)
                        else onClickItemSurtido?.invoke(factura)
                    }
                    progressDialog?.dismiss() // Ocultar el ProgressDialog después de completar la acción
                    // Volver a habilitar la interacción después de completar la acción

                }
            }
        }
        }


    fun updateData(newList: MutableList<ModeloProductoFacturado>) {
        val diffResult = DiffUtil.calculateDiff(ProductosCompradosFacturadosDiffCallback(listaFacturas, newList))
        listaFacturas = ArrayList(newList) // Convierte a ArrayList
        diffResult.dispatchUpdatesTo(this)
    }

    private var onClickItem: ((ModeloFactura) -> Unit)? = null
    private var onClickItemSurtido: ((ModeloFactura) -> Unit)? = null

    // Configurar el callback para el evento de click en un item de la lista
    fun setOnClickItem(callback: (ModeloFactura) -> Unit) {
        this.onClickItem = callback
    }
    fun setOnClickItemSurtido(callback: (ModeloFactura) -> Unit) {
        this.onClickItemSurtido = callback
    }
    // Este método devuelve el número de elementos en la lista de productos
    override fun getItemCount(): Int {
        return listaFacturas.size
    }


    // ViewHolder para la vista de cada elemento de la lista de productos
        inner class FacturaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cliente: TextView = itemView.findViewById(R.id.textView_cliente)
        val total: TextView = itemView.findViewById(R.id.textView_valor)
        val vendedor: TextView = itemView.findViewById(R.id.textView_vendedor)
        val fecha: TextView = itemView.findViewById(R.id.textView_fecha)
        val id: TextView = itemView.findViewById(R.id.textView_id)
        val cardView:CardView=itemView.findViewById(R.id.cardview_itemProducto)

        @SuppressLint("SetTextI18n")
        fun bind(factura: ModeloProductoFacturado) {


            cliente.text="Cantidad: "+factura.cantidad

            vendedor.text=factura.vendedor
            fecha.text=factura.fecha
            total.setTextColor(ContextCompat.getColor(itemView.context, R.color.verde))
            if(factura.productoEditado.equals("Inventario Editado")){
                total.text="Editado"
                total.setTextColor(ContextCompat.getColor(itemView.context, R.color.naranja))
            }else{
                total.text= factura.tipoOperacion
                if(factura.tipoOperacion.equals("Surtido")){
                    total.setTextColor(ContextCompat.getColor(itemView.context, R.color.amarillo))
                }
            }
            id.text=factura.id_pedido.substring(0, 5)  //solo mostramos los primero 5 digitos en la vista para evitar exeso de datos

            // Inicializar progressDialog si es nulo
            if (progressDialog == null) {
                progressDialog?.setIcon(R.drawable.logo2_compra_rapidita)
                progressDialog = ProgressDialog(itemView.context)
                progressDialog?.setMessage("Cargando...") // Puedes personalizar el mensaje
                progressDialog?.setCancelable(false)
            }
        }

    }

    fun buscarFactura(tablaReferencia: String, idPedido: String, callback: (List<ModeloFactura>) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val tablaRef = database.getReference(DatosPersitidos.datosEmpresa.id).child(tablaReferencia)

        tablaRef.orderByChild("id_pedido").equalTo(idPedido).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val facturas = mutableListOf<ModeloFactura>()

                    for (facturaSnapshot in snapshot.children) {
                        val factura = facturaSnapshot.getValue(ModeloFactura::class.java)
                        factura?.let {
                            if (!factura.fecha.isNullOrEmpty()) {
                                facturas.add(it)
                            }
                        }
                    }

                    // Llamar al callback con los resultados
                    callback(facturas)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("MiApp", "Error al buscar facturas: ${error.message}")
                    // Puedes manejar el error de alguna manera si lo necesitas
                }
            })
    }
}
