package com.castellanoseloy.ventarapida.ui.usuarios

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.ventarapida.MainActivity
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.FragmentListaUsuariosBinding
import com.castellanoseloy.ventarapida.datos.ModeloUsuario
import com.castellanoseloy.ventarapida.procesos.FirebaseUsuarios
import com.castellanoseloy.ventarapida.procesos.Utilidades
import com.castellanoseloy.ventarapida.procesos.Utilidades.eliminarAcentosTildes
import com.google.android.gms.ads.AdRequest
import java.util.ArrayList


@Suppress("DEPRECATION")
class ListaUsuarios : Fragment() {

    private var binding: FragmentListaUsuariosBinding? = null
    private lateinit var vista: View
    private var lista: ArrayList<ModeloUsuario>? = null
    private lateinit var adaptador: UsuariosAdaptador


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListaUsuariosBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        if(MainActivity.verPublicidad)  initLoadAds()

        return binding!!.root
    }
    private fun initLoadAds() {
        binding?.banner?.visibility=View.VISIBLE
        val adRequest = AdRequest.Builder().build()
        binding?.banner?.loadAd(adRequest)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_nuevo_usuario, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_nuevo_usuario ->{
                Navigation.findNavController(vista).navigate(R.id.registroUsuarios)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view

        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding!!.recyclerViewClientes.layoutManager = gridLayoutManager
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        val tareaUsuarios = FirebaseUsuarios.buscarTodosUsuariosPorEmpresa()

        tareaUsuarios.addOnSuccessListener { usuarios ->
            lista= usuarios as ArrayList<ModeloUsuario>?
            adaptador = UsuariosAdaptador(lista!!)
            binding?.recyclerViewClientes?.adapter = adaptador

            listenerAdaptador()

            val busqueda = binding?.searchViewBuscarCliente?.query.toString()
            if(busqueda!=""){
                filtro(busqueda)
            }
        }
        listeners()

    }

    override fun onResume() {
        super.onResume()
        usuariosConectados()
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
                    val planActual=verificarPlan()
                    binding?.textViewUsuariosActivos?.text="Usuarios Activos $usuariosActivos\n$planActual"
                }
            }
        }
    }

    private fun verificarPlan(): String {
        var plan="No disponible"
        if(MainActivity.datosEmpresa.plan == "Empresarial") plan="Plan Empresarial (30 usuarios activos)"
        if(MainActivity.datosEmpresa.plan == "Premium") plan="Plan Premium (10 usuarios activos)"
        if(MainActivity.datosEmpresa.plan == "Basico") plan="Plan Básico (3 usuarios activos)"
        if(MainActivity.datosEmpresa.plan == "Gratuito") plan="Prueba gratuita (30 usuarios activos)"
        return plan
    }

    private fun listenerAdaptador() {

        adaptador.setOnClickItem  { item ->
            abriDetalle(item,vista)
        }
    }

    private fun listeners() {
        binding?.recyclerViewClientes?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // se está desplazando hacia abajo
                    Utilidades.ocultarTeclado(requireContext(), vista)

                }
            }
        })

        binding!!.searchViewBuscarCliente.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filtro(newText)
                }
                return true
            }
        })
        //desbloquea searchview al seleccionarlo
        binding?.searchViewBuscarCliente?.setOnClickListener {
            binding?.searchViewBuscarCliente?.isIconified=false
        }
    }

    private fun filtro(textoParaFiltrar: String) {

        val filtro = lista?.filter { objeto: ModeloUsuario ->
            objeto.nombre.eliminarAcentosTildes().contains(textoParaFiltrar.eliminarAcentosTildes(), ignoreCase = true)
        }
        adaptador = filtro?.let { UsuariosAdaptador(it as MutableList<ModeloUsuario>) }!!
        binding?.recyclerViewClientes?.adapter =adaptador

        listenerAdaptador()


    }

    private fun abriDetalle(item: ModeloUsuario, vista: View) {
        val bundle = Bundle()
        bundle.putSerializable("modelo", item)
        Navigation.findNavController(vista).navigate(R.id.registroUsuarios,bundle)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}