package com.castellanoseloy.ventarapida

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

import com.castellanoseloy.ventarapida.datos.ModeloDatosEmpresa
import com.castellanoseloy.ventarapida.datos.ModeloProducto
import com.castellanoseloy.ventarapida.datos.ModeloUsuario
import com.castellanoseloy.ventarapida.procesos.FirebaseDatosEmpresa
import com.castellanoseloy.ventarapida.procesos.FirebaseProductos
import com.castellanoseloy.ventarapida.procesos.FirebaseUsuarios
import com.castellanoseloy.ventarapida.procesos.Preferencias
import com.castellanoseloy.ventarapida.procesos.Suscripcion
import com.castellanoseloy.ventarapida.procesos.Utilidades.convertirCadenaAFecha
import com.castellanoseloy.ventarapida.procesos.UtilidadesBaseDatos
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.ActivityMainBinding

import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity() {

    companion object {
        const val JOB_ID = 1000 // Cambia este número por uno que no esté siendo utilizado en tu app

        var ventaProductosSeleccionados = mutableMapOf<ModeloProducto, Int>()
        var compraProductosSeleccionados = mutableMapOf<ModeloProducto, Int>()

        var verPublicidad: Boolean = true

        //Elementos sacados de las preferencias para usarlos en la aplicacion
        var tono = true
        var mostrarAgotadosCatalogo= true
        var datosEmpresa: ModeloDatosEmpresa = ModeloDatosEmpresa()
        var datosUsuario: ModeloUsuario = ModeloUsuario()
        var planVencido:Boolean? =false
        lateinit var logotipo: ImageView
        lateinit var editText_nombreEmpresa: TextView
        lateinit var preferencia_informacion_superior:String
        lateinit var preferencia_informacion_inferior:String
        lateinit var edit_text_preference_codigo_area:String
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



    private var suscripcion=Suscripcion()


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
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if(datosUsuario.perfil=="Administrador"){
            navView.menu
                .setGroupVisible(R.id.panel_administrador, true)
            navView.menu
                .setGroupVisible(R.id.panel_reporte_administrador, true)
        }

        if(datosUsuario.perfil=="Vendedor"){
            navView.menu
                .setGroupVisible(R.id.panel_reporte_vendedor, true)
        }

        if(datosUsuario.perfil=="Inactivo"){
            navView.menu
                .setGroupVisible(R.id.panel_administrador, false)
            navView.menu
                .setGroupVisible(R.id.panel_reporte_administrador, false)
            navView.menu
                .setGroupVisible(R.id.panel_reporte_vendedor, false)

        }

        if(!datosEmpresa.plan.equals("Ilimitado")){
            val proximoPago=convertirCadenaAFecha(datosEmpresa.proximo_pago)

            Log.d("pagos", "su proximo pago es: ${proximoPago} y su plan es ${datosEmpresa.plan}")
            if (proximoPago!=null ) planVencido = suscripcion.verificarFinSuscripcion(proximoPago)

            if (planVencido!!){
                verPublicidad=true
                NotificacionPlanVencido()
            }

        }
        usuariosConectados()
        comportamientoBotonBack()
    }

    private fun comportamientoBotonBack() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Obtén el destino actual del NavController
                val currentDestination = navController.currentDestination
                // Verifica si estás en la pantalla principal (ajusta esto según tu configuración)
                val isOnHomeScreen = currentDestination?.id == R.id.nav_host_fragment_content_main
                if (!isOnHomeScreen) {
                    // No estás en la pantalla principal, permite el comportamiento predeterminado
                    navController.navigateUp()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }


    private fun verificarCantidadUsuariosPlan(usuariosActivos: Int) {

        Log.w("Usuarios", "Usuarios activos en la cuenta: $usuariosActivos")

        if(datosEmpresa.plan == "Empresarial" && usuariosActivos>30){
            NotificacionPlanExedido()
        }
        if(datosEmpresa.plan == "Premium"&& usuariosActivos>6){
            NotificacionPlanExedido()
        }
        if(datosEmpresa.plan == "Basico"&& usuariosActivos>3){
            NotificacionPlanExedido()
        }
    }

    private fun usuariosConectados() {
        val tareaUsuarios = FirebaseUsuarios.buscarTodosUsuariosPorEmpresa()
        var usuariosActivos=0
        tareaUsuarios.addOnSuccessListener { usuarios ->
            if(usuarios.isNotEmpty()){
                for (usuario in usuarios){
                    if(usuario.perfil != "Inactivo"){
                        usuariosActivos++
                    }
                }
                verificarCantidadUsuariosPlan(usuariosActivos)
            }
        }
    }


    fun NotificacionPlanExedido (){
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Limite de Usuarios Exedidos")
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setMessage("La cuenta ha exedido la cantidad de usuarios permitios, por favor contate al admistrador de la cuenta")

        if(datosUsuario.id.equals(datosEmpresa.idDuenoCuenta)){
            alertDialogBuilder.setMessage("Ha cuenta ha exedido la cantidad de usuarios permitios, inactive usuarios que no esten usando la app o aplique un nuevo plan")
            alertDialogBuilder.setCancelable(true)
            alertDialogBuilder.setPositiveButton("Verificar") { _, _ ->
                AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener { task: Task<Void?>? ->
                        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        navController.navigate(R.id.listaUsuarios)
                    }
            }
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun NotificacionPlanVencido (){
        if(!datosUsuario.id.equals(datosEmpresa.idDuenoCuenta)){

            navView.menu
                .setGroupVisible(R.id.panel_administrador, false)
            navView.menu
                .setGroupVisible(R.id.panel_reporte_administrador, false)
            navView.menu
                .setGroupVisible(R.id.panel_reporte_vendedor, false)

            navView.menu
                .setGroupVisible(R.id.panelVentas, false)

            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Plan Vencido")
            alertDialogBuilder.setCancelable(false)
            alertDialogBuilder.setMessage("El plan se ha vencido por favor renueve el plan para ultilizarlo con mas de 1 usuario")

            alertDialogBuilder.setPositiveButton("Ver Suscripciones") { _, _ ->
                AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener { task: Task<Void?>? ->
                        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        navController.navigate(R.id.suscripcionesDisponibles)
                    }
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }else{
            val rootView = findViewById<View>(android.R.id.content)
            val snackbar = Snackbar.make(rootView, "Plan vencido", Snackbar.LENGTH_SHORT)
            val snackbarView = snackbar.view
            snackbarView.setBackgroundResource(R.color.rojo)
            snackbar.show()
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

        cargarEmpresa()

    }

    private fun cargarEmpresa() {
        //cargar nuevamente los datos de la empresa
        FirebaseDatosEmpresa.obtenerDatosEmpresa(
            datosUsuario.idEmpresa,
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Procesar los datos en el snapshot
                    datosEmpresa = snapshot.getValue(ModeloDatosEmpresa::class.java)!!
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar el error
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }


    fun cargarDialogoProceso() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Un momento...")
        progressDialog?.setCancelable(true)
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

        if(datosUsuario.perfil=="Inactivo"){

            val alertDialogBuilder = AlertDialog.Builder(this)

            alertDialogBuilder.setCancelable(false)
            alertDialogBuilder.setTitle("Usuario Inactivo")
            alertDialogBuilder.setMessage("Su usuario se encuentra Inactivo para ${datosEmpresa.nombre} pongase en contacto con el administrador")
            alertDialogBuilder.setPositiveButton("Aceptar") { _, _ ->

                AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener { task: Task<Void?>? ->

                     ventaProductosSeleccionados.clear()
                     compraProductosSeleccionados.clear()

                        Toast.makeText(this, "Sesion Cerrada", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                        this.finish()

                    }
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

    }

}