package com.example.ventarapida

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ventarapida.databinding.ActivityMainBinding
import com.example.ventarapida.datos.ModeloDatosEmpresa
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloUsuario
import com.example.ventarapida.procesos.FirebaseProductos
import com.example.ventarapida.procesos.Preferencias
import com.example.ventarapida.procesos.UtilidadesBaseDatos
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    companion object {
        const val JOB_ID = 1000 // Cambia este número por uno que no esté siendo utilizado en tu app

        var ventaProductosSeleccionados = mutableMapOf<ModeloProducto, Int>()
        var compraProductosSeleccionados = mutableMapOf<ModeloProducto, Int>()

        //Elementos sacados de las preferencias para usarlos en la aplicacion
        var tono = true
        var datosEmpresa: ModeloDatosEmpresa = ModeloDatosEmpresa()
        var datosUsuario: ModeloUsuario = ModeloUsuario()

        lateinit var logotipo: ImageView
        lateinit var editText_nombreEmpresa: TextView
        lateinit var preferencia_informacion_superior:String
        lateinit var preferencia_informacion_inferior:String
        var progressDialog: ProgressDialog? = null
        lateinit var  navController: NavController
        lateinit var  drawerLayout: DrawerLayout
        lateinit var navView: NavigationView

         lateinit var appBarConfiguration: AppBarConfiguration
         lateinit var binding: ActivityMainBinding
        fun init(context: Context) {
            logotipo = ImageView(context)
            editText_nombreEmpresa= TextView(context)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //cargar las preferencias primero para evitar errores de carga
        val preferenciasServicios= Preferencias()
        preferenciasServicios.preferenciasConfiguracion(this)

        if (ventaProductosSeleccionados.isNotEmpty()) {
            Toast.makeText(this,"Lista venta recuperada",Toast.LENGTH_LONG).show()
        }

        init(this)

        cargarDialogoProceso()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarDatos()

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        val navHeader = navView.getHeaderView(0)
        logotipo = navHeader.findViewById<ImageView>(R.id.imageView)
        editText_nombreEmpresa=navHeader.findViewById<TextView>(R.id.textView_nombreEmpresa)

        editText_nombreEmpresa.text = datosEmpresa.nombre

        //colocar logotipo en el menu lateral
        if (!datosEmpresa.url.isEmpty()) {
            Picasso.get()
                .load(datosEmpresa.url)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) // Intenta cargar desde caché, no almacena en caché
                .into(logotipo, object : Callback {
                    override fun onSuccess() {
                        // La imagen se cargó correctamente desde la caché o se descargó y almacenó en caché
                        logotipo.setImageDrawable(logotipo.drawable)
                    }

                    override fun onError(e: Exception?) {
                        // Ocurrió un error al cargar la imagen
                        // Puedes manejar el error aquí si es necesario
                    }
                })
        }


        if (datosEmpresa.premiun.equals("true")){
            appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home), drawerLayout)
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)

            if(datosUsuario.perfil=="Administrador"){
                navView.getMenu()
                    .setGroupVisible(R.id.panel_administrador, true)
            }

            if(datosUsuario.perfil=="Vendedor"){
                navView.getMenu()
                    .setGroupVisible(R.id.panel_vendedor, true)
            }

        }else{
            Toast.makeText(this, "Pasate a Premium",Toast.LENGTH_LONG).show()
        }


    }

    fun mostrarFabBottonTransacciones(context: Context) {
        val transaccionesPendientes=
            UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos(context)

        if(transaccionesPendientes.size<1){
            binding.appBarMain.fabSincronizar.visibility= View.GONE
        }else{
            binding.appBarMain.fabSincronizar.visibility=View.VISIBLE

            binding.appBarMain.fabSincronizar.setOnClickListener { view ->
                Toast.makeText(context,"Sincronizando "+transaccionesPendientes.size.toString()+" produtos",Toast.LENGTH_LONG).show()
            }

        }


    }

    private fun cargarDatos() {
        val preferenciasServicios= Preferencias()
        preferenciasServicios.obtenerServicioPendienteSubirFoto(this)

        val transaccionesPendientes=
            UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos(this)
        FirebaseProductos.transaccionesCambiarCantidad(this, transaccionesPendientes)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }


    fun cargarDialogoProceso() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Un momento...")
        progressDialog?.setCancelable(false)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()

        if(datosUsuario.perfil.isNullOrEmpty()){
            finish()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

}