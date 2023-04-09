package com.example.ventarapida

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ve.DetalleProducto
import com.example.ventarapida.databinding.ActivityMainBinding
import com.example.ventarapida.ui.data.ModeloProducto
import com.example.ventarapida.ui.nuevoProducto.NuevoProducto
import com.example.ventarapida.ui.process.ServiciosSubirFoto
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity() {

    companion object {
        const val JOB_ID = 1000 // Cambia este número por uno que no esté siendo utilizado en tu app
        var productosSeleccionados = mutableMapOf<ModeloProducto, Int>()
    }
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cargarPreferencias()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    fun obtenerVentaPendiente() {
        val sharedPreferences = this.getSharedPreferences("productos_seleccionados", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("seleccion_venta", "")

        if (json!!.isNotEmpty()) {
            val mapType = object : TypeToken<MutableMap<String, Int>>() {}.type
            val mapString: MutableMap<String, Int> = gson.fromJson(json, mapType)

               mapString.forEach { (productoJson, cantidad) ->

                val modeloProducto = parsearModeloProducto(productoJson)

                productosSeleccionados[modeloProducto] = cantidad
            }

        }

        return
    }

    fun parsearModeloProducto(productoJson: String): ModeloProducto {
        val fields = productoJson.split(", ")
        val cantidad = fields[0].substringAfter("=")
        val codigo = fields[1].substringAfter("=")
        val descripcion = fields[2].substringAfter("=")
        val fecha_ultima_modificacion = fields[3].substringAfter("=")
        val id = fields[4].substringAfter("=")
        val nombre = fields[5].substringAfter("=")
        val p_compra = fields[6].substringAfter("=")
        val p_diamante = fields[7].substringAfter("=")
        val url = fields[8].substringAfter("=")
        val descuento = fields[9].substringAfter("=")
        val precio_descuento = ""
        return ModeloProducto(cantidad, codigo, descripcion, fecha_ultima_modificacion, id, nombre, p_compra, p_diamante, url, descuento, precio_descuento)
    }


    private fun cargarPreferencias() {
        obtenerVentaPendiente()
        obtenerServicioPendiente()
    }

    // Crear una función para obtener los datos del servicio pendiente de la preferencia
    fun obtenerServicioPendiente() {

            val fotosParaSubir= ServiciosSubirFoto()
            val serviciosPendientes = fotosParaSubir.getServiciosPendientes(applicationContext)

            serviciosPendientes.forEach { servicio ->
            val fileUri = servicio.first
            val storageRefString = servicio.second
            val idProducto = servicio.third

            fotosParaSubir.guardarServicioPendiente(applicationContext,fileUri,storageRefString,idProducto)

        }



    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is DetalleProducto || fragment is NuevoProducto ) {
            fragment.setHasOptionsMenu(true)
        }else{
            fragment.setHasOptionsMenu(false)
        }
    }

}