package com.castellanoseloy.ventarapida

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
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
import com.castellanoseloy.ventarapida.procesos.FirebaseDatosEmpresa
import com.castellanoseloy.ventarapida.procesos.FirebaseProductos
import com.castellanoseloy.ventarapida.procesos.FirebaseUsuarios
import com.castellanoseloy.ventarapida.procesos.Preferencias
import com.castellanoseloy.ventarapida.procesos.Utilidades.convertirCadenaAFecha
import com.castellanoseloy.ventarapida.procesos.UtilidadesBaseDatos
import com.castellanoseloy.ventarapida.databinding.ActivityMainBinding
import com.castellanoseloy.ventarapida.datos.VersionModel
import com.castellanoseloy.ventarapida.procesos.Suscripcion
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.VersionControlProvider
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.compraProductosSeleccionados
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.datosEmpresa
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.datosUsuario
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.editText_nombreEmpresa
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.interstitial
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.logotipo
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.planVencido
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.progressDialog
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.ventaProductosSeleccionados
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos.Companion.verPublicidad

import com.firebase.ui.auth.AuthUI
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity() {


    lateinit var  navController: NavController
    lateinit var  drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var appBarConfiguration: AppBarConfiguration
    private var suscripcion= Suscripcion()
    private var doubleBackToExitPressedOnce = false

    fun init(context: Context) {
        DatosPersitidos.logotipo = ImageView(context)
        DatosPersitidos.editText_nombreEmpresa = TextView(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verificarPlan()

        DatosPersitidos.binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(DatosPersitidos.binding.root)

        //cargar las preferencias primero para evitar errores de carga
        val preferenciasServicios= Preferencias()
        preferenciasServicios.preferenciasConfiguracion(this)

        if (ventaProductosSeleccionados.isNotEmpty()) {
            Toast.makeText(this,"Lista venta recuperada",Toast.LENGTH_LONG).show()
        }

        init(this)

        cargarDialogoProceso()

        cargarDatos()

        setSupportActionBar(DatosPersitidos.binding.appBarMain.toolbar)

        drawerLayout = DatosPersitidos.binding.drawerLayout
        navView = DatosPersitidos.binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        val navHeader = navView.getHeaderView(0)
        logotipo = navHeader.findViewById<ImageView>(R.id.imageView)
        editText_nombreEmpresa=navHeader.findViewById<TextView>(R.id.textView_nombreEmpresa)

        editText_nombreEmpresa.text = datosEmpresa.nombre

        //colocar logotipo en el menu lateral
        if (!datosEmpresa.url.isEmpty()) {
            Utilidades.cargarImagen(datosEmpresa.url, logotipo)
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



        if(verPublicidad) NotificacionPlanVencido()



        comportamientoBotonBack()
    }

    private fun verificarPlan() {
        if(!datosEmpresa.plan.equals("Ilimitado")){
            val proximoPago=convertirCadenaAFecha(datosEmpresa.proximo_pago)

            Log.d("pagos", "su proximo pago es: ${proximoPago} y su plan es ${datosEmpresa.plan}")
            if (proximoPago!=null ) planVencido = suscripcion.verificarFinSuscripcion(proximoPago)

            if (planVencido!!){
                verPublicidad=true
                cargarAnuncio()
            }
        }
        usuariosConectados()
        Log.d("pruebas", "Se ha establecido la publicidad")
    }

    private fun cargarAnuncio() {

            var adRequest = AdRequest.Builder().build()

            InterstitialAd.load(this, "ca-app-pub-5390342068041092/6706005035", adRequest, object : InterstitialAdLoadCallback(){
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("Anuncios", "El anuncio esta listo para mostrarse")
                    interstitial = interstitialAd
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.e("Anuncios", "No se cargo el anuncio")
                    interstitial = null
                }
            })

    }

    private fun comportamientoBotonBack() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Obtén el destino actual del NavController
                val currentDestination = navController.currentDestination

                // Verifica si estás en la pantalla principal (ajusta esto según tu configuración)
                val isOnHomeScreen = currentDestination?.id == R.id.nav_home
                if (!isOnHomeScreen) {
                    // No estás en la pantalla principal, permite el comportamiento predeterminado
                    navController.navigateUp()
                }else{
                    if (doubleBackToExitPressedOnce) {
                        finish()
                    }else{
                        Toast.makeText(this@MainActivity, "Presione de nuevo para salir", Toast.LENGTH_SHORT).show()
                    }

                    doubleBackToExitPressedOnce = true

                    Handler().postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000) // Restablece el valor después de 2 segundos
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
        alertDialogBuilder.setIcon(R.drawable.logo2_compra_rapidita)
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
        if(datosUsuario.id != datosEmpresa.idDuenoCuenta){

            navView.menu
                .setGroupVisible(R.id.panel_administrador, false)
            navView.menu
                .setGroupVisible(R.id.panel_reporte_administrador, false)
            navView.menu
                .setGroupVisible(R.id.panel_reporte_vendedor, false)

            navView.menu
                .setGroupVisible(R.id.panelVentas, false)

            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setIcon(R.drawable.logo2_compra_rapidita)
            alertDialogBuilder.setTitle("Plan Vencido")
            alertDialogBuilder.setCancelable(false)
            alertDialogBuilder.setMessage("El plan se ha vencido por favor renueve el plan para ultilizarlo con mas de 1 usuario")

            alertDialogBuilder.setPositiveButton("Ver Suscripciones") { _, _ ->
                        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                        navController.navigate(R.id.suscripcionesDisponibles)
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
            DatosPersitidos.binding.appBarMain.fabSincronizar.visibility= View.GONE

        }else{
            DatosPersitidos.binding.appBarMain.fabSincronizar.visibility=View.VISIBLE

            DatosPersitidos.binding.appBarMain.fabSincronizar.setOnClickListener { view ->
                Toast.makeText(context,"Sincronizando "+transaccionesPendientes.size.toString()+" productos",Toast.LENGTH_LONG).show()
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

    private fun verificarVersionActualizada() {
        val version=obtenerInformacionDeVersion(this)

        val versionControlProvider = VersionControlProvider()

// Llamar al método getVersionActual
        versionControlProvider.getVersionActual().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Obtener la instantánea de datos exitosa
                val dataSnapshot = task.result

                // Verificar si la instantánea de datos no es nula y contiene datos
                if (dataSnapshot != null && dataSnapshot.exists()) {
                    // Acceder a los datos específicos según la estructura de tu base de datos en tiempo real
                    val versionActual = dataSnapshot.getValue(VersionModel::class.java)
                    if(version?.second!! < versionActual?.versionCode!!) solicitarActualizacion(versionActual)
                }
            } else {
                // Manejar el error si la tarea no fue exitosa
                val exception = task.exception
                Log.e("Firebase", "Error al obtener la versión actual: ${exception?.message}")
            }
        }


    }

    private fun solicitarActualizacion(versionActual: VersionModel) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setIcon(R.drawable.logo2_compra_rapidita)
        alertDialogBuilder.setCancelable(versionActual.cancelable!!)
        alertDialogBuilder.setTitle("Nueva actualización disponible")
        alertDialogBuilder.setMessage("Se requiere realizar actualización. \n${versionActual.descripcion}")
        alertDialogBuilder.setPositiveButton("Ir a playstore") { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cataplus.page.link/ZCg5"))
            startActivity(intent)
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun obtenerInformacionDeVersion(contexto: Context): Pair<String, Int>? {
        return try {
            val pInfo: PackageInfo = contexto.packageManager.getPackageInfo(contexto.packageName, 0)
            val versionName = pInfo.versionName
            val versionCode = pInfo.versionCode
            Log.d("vesion","El nombre de la version es: $versionName y el vesion code: $versionCode")
            Pair(versionName, versionCode)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
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




    fun cargarDialogoProceso() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setIcon(R.drawable.logo2_compra_rapidita)
        progressDialog?.setMessage("Un momento...")
        progressDialog?.setCancelable(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()

        //obtener version actual si no hay actualizaciones pendientes
        val transaccionesPendientes=
            UtilidadesBaseDatos.obtenerTransaccionesSumaRestaProductos(this)
        if(transaccionesPendientes.isEmpty()) verificarVersionActualizada()

        if(datosUsuario.perfil.isNullOrEmpty()){
            finish()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        if(datosUsuario.perfil=="Inactivo"){

            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setIcon(R.drawable.logo2_compra_rapidita)
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