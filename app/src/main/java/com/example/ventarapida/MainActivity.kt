package com.example.ventarapida

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.JobIntentService

import androidx.fragment.app.Fragment
import com.example.ve.DetalleProducto
import com.example.ventarapida.databinding.ActivityMainBinding
import com.example.ventarapida.ui.nuevoProducto.NuevoProducto
import com.example.ventarapida.ui.nuevoProducto.NuevoProductoViewModel
import com.example.ventarapida.ui.process.UploadService
import androidx.core.app.JobIntentService.enqueueWork
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        obtenerServicioPendiente(this)

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

    companion object {
        const val JOB_ID = 1000 // Cambia este número por uno que no esté siendo utilizado en tu app
    }
    // Crear una función para obtener los datos del servicio pendiente de la preferencia
    fun obtenerServicioPendiente(context: Context) {
        val serviciosPendientes = getServiciosPendientes(applicationContext)

        serviciosPendientes.forEach { servicio ->
            val fileUri = servicio.first
            val storageRefString = servicio.second
            val idProducto = servicio.third

//            borrarServicioPendiente(applicationContext,idProducto.toString())
//
//
            var servicio=UploadService()
            servicio.guardarServicioPendiente(applicationContext,fileUri,storageRefString,idProducto)

//                val intent = Intent(context, UploadService::class.java)
//                intent.putExtra("fileUri", fileUri)
//                intent.putExtra("storageRef", storageRefString.toString())
//                intent.putExtra("idProducto", idProducto)

//
//                // Iniciar el servicio en segundo plano utilizando JobIntentService
//                enqueueWork(context, UploadService::class.java, MainActivity.JOB_ID, intent)



        }



    }

    fun borrarServicioPendiente(context: Context, id: String) {
        val serviciosPendientes = getServiciosPendientes(context).toMutableList()
        val servicioAEliminar = serviciosPendientes.firstOrNull { it.third == id }
        if (servicioAEliminar != null) {
            serviciosPendientes.remove(servicioAEliminar)
            val jsonString = Gson().toJson(serviciosPendientes)
            val prefs = context.getSharedPreferences("servicio_pendiente", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("imagenes_pendientes", jsonString)
            editor.commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    fun getServiciosPendientes(context: Context): List<Triple<String?, String?, String?>> {
        val prefs = context.getSharedPreferences("servicio_pendiente", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("imagenes_pendientes", null)
        return if (jsonString != null) {
            val typeToken = object : TypeToken<List<Triple<String?, String?, String?>>>() {}.type
            Gson().fromJson(jsonString, typeToken)
        } else {
            emptyList()
        }
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