package com.example.ventarapida

import android.app.ProgressDialog
import android.content.Context
import android.media.Image
import android.os.Bundle
import android.view.Menu
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
import com.example.ventarapida.datos.ModeloDatosEmpresa
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.ui.nuevoProducto.NuevoProducto
import com.example.ventarapida.procesos.Preferencias
import com.example.ventarapida.procesos.UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos
import com.example.ventarapida.procesos.FirebaseProductos.transaccionesCambiarCantidad
import com.example.ventarapida.procesos.PermissionManager
import com.google.android.material.navigation.NavigationView



class MainActivity : AppCompatActivity() {
    private lateinit var permissionManager: PermissionManager
    companion object {
        const val JOB_ID = 1000 // Cambia este número por uno que no esté siendo utilizado en tu app
        var ventaProductosSeleccionados = mutableMapOf<ModeloProducto, Int>()
        var compraProductosSeleccionados = mutableMapOf<ModeloProducto, Int>()

        //Elementos sacados de las preferencias para usarlos en la aplicacion
        var tono = true
        lateinit var datosEmpresa: ModeloDatosEmpresa
        lateinit var logotipo: ImageView
        lateinit var editText_nombreEmpresa: TextView
        lateinit var preferencia_informacion_superior:String
        lateinit var preferencia_informacion_inferior:String
        var progressDialog: ProgressDialog? = null
        fun init(context: Context) {
            logotipo = ImageView(context)
            editText_nombreEmpresa= TextView(context)
        }
    }
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init(this)
        cargarDatos()

        cargarDialogoProceso()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        val navHeader = navView.getHeaderView(0)
        logotipo = navHeader.findViewById<ImageView>(R.id.imageView)
        editText_nombreEmpresa=navHeader.findViewById<TextView>(R.id.textView_nombreEmpresa)

        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        // Verifica y solicita el permiso de almacenamiento externo
        permissionManager = PermissionManager(this as AppCompatActivity)
        permissionManager.checkAndRequestStoragePermission()


    }



    private fun cargarDatos() {
        val preferenciasServicios= Preferencias()
        preferenciasServicios.preferenciasConfiguracion(this)

        preferenciasServicios.obtenerSeleccionPendiente(this,"venta_seleccionada")
        preferenciasServicios.obtenerSeleccionPendiente(this,"compra_seleccionada")
        preferenciasServicios.obtenerServicioPendiente(this)

        val transaccionesPendientes= obtenerTransaccionesSumaRestaProductos(this)
        transaccionesCambiarCantidad(this, transaccionesPendientes)
    }




    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    fun cargarDialogoProceso() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Cargando...")
        progressDialog?.setCancelable(false)
    }




    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }



}