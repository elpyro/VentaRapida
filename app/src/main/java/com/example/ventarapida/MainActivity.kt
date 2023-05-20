package com.example.ventarapida

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ventarapida.databinding.ActivityMainBinding
import com.example.ventarapida.datos.ModeloDatosEmpresa
import com.example.ventarapida.datos.ModeloProducto
import com.example.ventarapida.datos.ModeloUsuario
import com.example.ventarapida.procesos.FirebaseUsuarios.buscarUsuariosPorCorreo
import com.example.ventarapida.procesos.Preferencias
import com.example.ventarapida.procesos.PermissionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    companion object {
        const val JOB_ID = 1000 // Cambia este número por uno que no esté siendo utilizado en tu app
        const val RC_SIGN_IN = 123 //inicio sesion gmail
        var ventaProductosSeleccionados = mutableMapOf<ModeloProducto, Int>()
        var compraProductosSeleccionados = mutableMapOf<ModeloProducto, Int>()

        //Elementos sacados de las preferencias para usarlos en la aplicacion
        var tono = true
        var datosEmpresa: ModeloDatosEmpresa = ModeloDatosEmpresa()
        var datosUsuario: ModeloUsuario = ModeloUsuario()
        lateinit var googleSignInClient: GoogleSignInClient
        lateinit var auth: FirebaseAuth
        lateinit var logotipo: ImageView
        lateinit var editText_nombreEmpresa: TextView
        lateinit var preferencia_informacion_superior:String
        lateinit var preferencia_informacion_inferior:String
        var progressDialog: ProgressDialog? = null
        lateinit var  navController: NavController
        lateinit var  drawerLayout: DrawerLayout
        lateinit var navView: NavigationView
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

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        val navHeader = navView.getHeaderView(0)
        logotipo = navHeader.findViewById<ImageView>(R.id.imageView)
        editText_nombreEmpresa=navHeader.findViewById<TextView>(R.id.textView_nombreEmpresa)


        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)




     //   iniciarSesionConGoogle(this)
    }

    fun iniciarSesionConGoogle(context: Context) {

        auth = FirebaseAuth.getInstance()
        // Configurar las opciones de inicio de sesión de Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Construir el cliente de inicio de sesión de Google
        googleSignInClient = GoogleSignIn.getClient(context, gso)
        // Implementar el inicio de sesión de Google en respuesta a un evento de clic o botón

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    private fun cargarDatos() {
        val preferenciasServicios= Preferencias()
        preferenciasServicios.preferenciasConfiguracion(this)
    }




    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    fun cargarDialogoProceso() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Guardando...")
        progressDialog?.setCancelable(false)
    }




    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }



}