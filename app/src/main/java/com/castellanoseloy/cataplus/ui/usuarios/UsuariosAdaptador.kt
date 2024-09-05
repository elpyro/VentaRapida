package com.castellanoseloy.cataplus.ui.usuarios

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.cataplus.R
import com.castellanoseloy.cataplus.datos.ModeloUsuario

import java.util.*


class UsuariosAdaptador(
    val listaUsuarios: MutableList<ModeloUsuario>,
) : RecyclerView.Adapter<UsuariosAdaptador.UsuarioViewHolder>() {


    private val sortedFacturas = listaUsuarios.sortedWith(
        compareBy { it.nombre }
    )


    // Este método se llama cuando RecyclerView necesita crear un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        // Inflar el diseño del item_producto para crear la vista del ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)

        return UsuarioViewHolder(view)
    }

    // Este método se llama cuando RecyclerView necesita mostrar un elemento en una posición determinada
    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {

        // Obtener la clave del elemento que corresponde a la posición
        val usuario =sortedFacturas[position]

        // Vincular los datos del producto con la vista del ViewHolder
        holder.bind(usuario)

        holder.cardView.setOnClickListener() {
            onClickItem?.invoke(usuario)
        }

        holder.cardView.setOnLongClickListener() {
            onLongClickItem?.invoke(usuario)
            true
        }

    }


    private var onClickItem: ((ModeloUsuario) -> Unit)? = null

    // Configurar el callback para el evento de click en un item de la lista
    fun setOnClickItem(callback: (ModeloUsuario) -> Unit) {
        this.onClickItem = callback
    }

    // Callback para el evento de click largo en un item de la lista
    private var onLongClickItem: ((ModeloUsuario) -> Unit)? = null

    // Este método devuelve el número de elementos en la lista de productos
    override fun getItemCount(): Int {
        return listaUsuarios.size
    }

    // ViewHolder para la vista de cada elemento de la lista de productos
        inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.textView_usuario)
        val direccion: TextView = itemView.findViewById(R.id.textView_perfil)
        val cardView:CardView=itemView.findViewById(R.id.cardview_itemUsuario)

        @SuppressLint("SetTextI18n")
        fun bind(usuario: ModeloUsuario) {
            nombre.text = usuario.nombre
            direccion.text= usuario.perfil
        }

    }
}
