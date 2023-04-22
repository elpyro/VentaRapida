package com.example.ventarapida

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
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.ui.nuevoProducto.NuevoProducto
import com.example.ventarapida.procesos.Preferencias
import com.example.ventarapida.procesos.UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos
import com.example.ventarapida.procesos.FirebaseProductos.transaccionesCambiarCantidad
import com.google.android.material.navigation.NavigationView



class MainActivity : AppCompatActivity() {

    companion object {
        const val JOB_ID = 1000 // Cambia este número por uno que no esté siendo utilizado en tu app
        var productosSeleccionados = mutableMapOf<ModeloProducto, Int>()
    }
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




       cargarDatos()

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



    private fun cargarDatos() {
        val preferenciasServicios= Preferencias()
        preferenciasServicios.obtenerVentaPendiente(this)
        preferenciasServicios.obtenerServicioPendiente(this)

        val transaccionesPendientes= obtenerTransaccionesSumaRestaProductos(this)
        transaccionesCambiarCantidad(this, transaccionesPendientes)
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