package com.example.ventarapida

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
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
        val productosSeleccionados = mutableMapOf<ModeloProducto, Int>()
    }
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Obtener instancia de SharedPreferences
        sharedPreferences = getSharedPreferences("PreferenciaSubirFotos", Context.MODE_PRIVATE)

        // Cargar valores previamente guardados en las preferencias
        cargarPreferencias()



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

    private fun cargarPreferencias() {
//        cargarPeferenciaProductosSeleccionados()
        obtenerServicioPendiente()

//        val preferences = getSharedPreferences("venta_productos_seleccionados", Context.MODE_PRIVATE)
////        val editor = preferences.edit()
////        editor.clear()
////        editor.apply()
//        val gson = Gson()
//        val productosSeleccionadosString = preferences.getString("productos_seleccionados", "")
//        val entries = productosSeleccionadosString?.split("|")
//        entries?.forEach {
//           // aquí puedes crear un objeto ModeloProducto y agregarlo al mapa o hacer lo que necesites con los datos
//
//
//            val jsonString = it
//            val jsonElement = JsonParser().parse(jsonString)
//            val jsonObject = jsonElement.asJsonObject
//            val entry = jsonObject.entrySet().first()
//            val key = entry.key
//            val value = entry.value.asInt
//            val splitKey = key.split("(")[1].split(")")[0]
//            val splitValues = splitKey.split(", ")
//            val cantidad = splitValues[0].split("=")[1]
//            val codigo = splitValues[1].split("=")[1]
//            val descripcion = splitValues[2].split("=")[1]
//            val fechaUltimaModificacion = splitValues[3].split("=")[1]
//            val id = splitValues[4].split("=")[1]
//            val nombre = splitValues[5].split("=")[1]
//            val pCompra = splitValues[6].split("=")[1]
//            val pDiamante = splitValues[7].split("=")[1]
//            val url = splitValues[8].split("=")[1]
//            val descuento = splitValues[9].split("=")[1]
//            val precioDescuento = splitValues[10].split("=")[1]
//
//            val modeloProducto = ModeloProducto(
//                cantidad,
//                codigo,
//                descripcion,
//                fechaUltimaModificacion,
//                id,
//                nombre,
//                pCompra,
//                pDiamante,
//                url,
//                descuento,
//                precioDescuento
//            )
//            productosSeleccionados[modeloProducto] = value
//            Toast.makeText(this, "$productosSeleccionados", Toast.LENGTH_LONG).show()
//        }

//        val gson = Gson()
//
//// Obtener referencia a las preferencias compartidas
//        val prefs = getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
//
//// Recuperar los datos del mapa de las preferencias
//        val productosSeleccionadosString = prefs.getString("productos_seleccionados", "")
//        val type = object : TypeToken<MutableMap<ModeloProducto, Int>>() {}.type
//         productosSeleccionados = if (productosSeleccionadosString.isNullOrEmpty()) {
//            mutableMapOf()
//        } else {
//            gson.fromJson(productosSeleccionadosString, type)
//        }
//
//                val entries = productosSeleccionadosString?.split("|")
//        entries?.forEach {
//           // aquí puedes crear un objeto ModeloProducto y agregarlo al mapa o hacer lo que necesites con los datos
//
//
//            val jsonString = it
//            val jsonElement = JsonParser().parse(jsonString)
//            val jsonObject = jsonElement.asJsonObject
//            val entry = jsonObject.entrySet().first()
//            val key = entry.key
//            val value = entry.value.asInt
//            val splitKey = key.split("(")[1].split(")")[0]
//            val splitValues = splitKey.split(", ")
//            val cantidad = splitValues[0].split("=")[1]
//            val codigo = splitValues[1].split("=")[1]
//            val descripcion = splitValues[2].split("=")[1]
//            val fechaUltimaModificacion = splitValues[3].split("=")[1]
//            val id = splitValues[4].split("=")[1]
//            val nombre = splitValues[5].split("=")[1]
//            val pCompra = splitValues[6].split("=")[1]
//            val pDiamante = splitValues[7].split("=")[1]
//            val url = splitValues[8].split("=")[1]
//            val descuento = splitValues[9].split("=")[1]
//            val precioDescuento = splitValues[10].split("=")[1]
//
//            val modeloProducto = ModeloProducto(
//                cantidad,
//                codigo,
//                descripcion,
//                fechaUltimaModificacion,
//                id,
//                nombre,
//                pCompra,
//                pDiamante,
//                url,
//                descuento,
//                precioDescuento
//            )
//            productosSeleccionados[modeloProducto] = value
//            Toast.makeText(this, "$productosSeleccionados", Toast.LENGTH_LONG).show()
//        }
//
//    Toast.makeText(this, productosSeleccionados.size.toString(),Toast.LENGTH_LONG).show()

        //        //     RECUPERAR ARRAYLIST GUARDAO EN PREFERENCIAS     implementation 'com.google.code.gson:gson:2.8.6'

        //        //     RECUPERAR ARRAYLIST GUARDAO EN PREFERENCIAS     implementation 'com.google.code.gson:gson:2.8.6'
//
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
//
//                val intent = Intent(context, UploadService::class.java)
//                intent.putExtra("fileUri", fileUri)
//                intent.putExtra("storageRef", storageRefString.toString())
//                intent.putExtra("idProducto", idProducto)
//
//
//                // Iniciar el servicio en segundo plano utilizando JobIntentService
//                enqueueWork(context, UploadService::class.java, MainActivity.JOB_ID, intent)
//


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

    override fun onPause() {
        super.onPause()
         // Guardar valores en las preferencias
//        guardarPreferencias()
    }

}