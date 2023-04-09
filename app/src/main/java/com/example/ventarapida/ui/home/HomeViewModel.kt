package com.example.ventarapida.ui.home

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ventarapida.MainActivity.Companion.productosSeleccionados
import com.example.ventarapida.R
import com.example.ventarapida.ui.data.ModeloProducto
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import java.text.NumberFormat
import java.util.*

class HomeViewModel : ViewModel() {

    lateinit var context: Context // propiedad para almacenar el contexto
    val productosLiveData = MutableLiveData<List<ModeloProducto>>()

   

    val totalSeleccionLiveData=MutableLiveData<String>()

    var totalCarritoLiveData=MutableLiveData<String>()
    fun restarProductoSeleccionado( producto: ModeloProducto){
        if (productosSeleccionados.containsKey(producto)) {
            productosSeleccionados[producto] = productosSeleccionados[producto]!! - 1
            if (productosSeleccionados[producto]!! <= 0) {
                productosSeleccionados.remove(producto)
            }
        }
        calcularTotal()
    }
    fun agregarProductoSeleccionado(producto: ModeloProducto) {
        if (productosSeleccionados.containsKey(producto)) {
            productosSeleccionados[producto] = productosSeleccionados[producto]!! + 1
        } else {
            productosSeleccionados[producto] = 1
        }
        crearTono(context)
        calcularTotal()

    }

    fun calcularTotal(){
        var total = 0.0
        for ((producto, cantidad) in productosSeleccionados) {
            total += producto.p_diamante.toDouble() * cantidad.toDouble()
        }
        val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val valorFormateado = formatoMoneda.format(total)
        totalCarritoLiveData.value = valorFormateado

        totalSeleccionLiveData.value=productosSeleccionados.size.toString()

        guardarPreferenciaListaSeleccionada(context, productosSeleccionados)
    }



    fun guardarPreferenciaListaSeleccionada(context: Context, map: MutableMap<ModeloProducto, Int>) {

        limpiarPreferenciaListaSeleccionada(context)

        val sharedPreferences = context.getSharedPreferences("productos_seleccionados", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(map)
        editor.putString("seleccion_venta", json)
        editor.apply()
    }

    fun limpiarPreferenciaListaSeleccionada(context: Context) {

        val sharedPreferences = context.getSharedPreferences("productos_seleccionados", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("seleccion_venta")
        editor.apply()

    }

    fun crearTono(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.coin)
        mediaPlayer.start()
    }
    fun actualizarCantidadProducto(producto: ModeloProducto, nuevaCantidad: Int) {
        if (nuevaCantidad > 0) {
            productosSeleccionados[producto] = nuevaCantidad
        } else {
            productosSeleccionados.remove(producto)
        }
        crearTono(context)
        calcularTotal()
    }

        fun eliminarCarrito(){
            productosSeleccionados.clear()
            calcularTotal()
        }

    fun getProductos(): LiveData<List<ModeloProducto>> {

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val productReference = firebaseDatabase.getReference("Productos")

        productReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productos = mutableListOf<ModeloProducto>()

                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(ModeloProducto::class.java)
                    productos.add(producto!!)
                }

                productosLiveData.value = productos
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductViewModel", "Error al cargar productos", error.toException())
            }
        })

        return productosLiveData
    }

    fun separarNumerosDelString(string: String): Pair<String, String?> {
        // Expresión regular para detectar números al final del string
        val regex = "(\\d+)$"
        val matchResult = Regex(regex).find(string)
        if (matchResult != null) {
            // Si hay números al final del string, separarlos del resto del string
            val numeros = matchResult.value
            val resto = string.removeSuffix(numeros)
            return Pair(resto, numeros)
        } else {
            // Si no hay números al final del string, devolver el string completo y null
            return Pair(string.trim(), null)
        }
    }
}