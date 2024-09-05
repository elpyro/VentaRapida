package com.castellanoseloy.cataplus.procesos


import android.util.Log
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.datos.ModeloUsuario
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


object FirebaseUsuarios {


    private const val TABLA_REFERENCIA = "Usuarios"

    fun guardarUsuario(updates: HashMap<String, Any>) {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(TABLA_REFERENCIA).child((updates["id"] as String))
        registroRef.keepSynced(true)
        registroRef.updateChildren(updates)
    }

    fun eliminarUsuarioPorId(idUsuario: String): Task<Void> {
        val database = FirebaseDatabase.getInstance()
        val usuarioRef = database.getReference(TABLA_REFERENCIA).child(idUsuario)
        // Eliminar el usuario
        return usuarioRef.removeValue()
    }

    fun buscarUsuariosPorCorreo(correo: String): Task<MutableList<ModeloUsuario>> {
        val database = FirebaseDatabase.getInstance()
        val usuariosRef = database.getReference(TABLA_REFERENCIA)
        usuariosRef.keepSynced(true)
        val taskCompletionSource = TaskCompletionSource<MutableList<ModeloUsuario>>()
        usuariosRef.orderByChild("correo").equalTo(correo.toLowerCase())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val usuarios = mutableListOf<ModeloUsuario>()

                    for (snapshot in dataSnapshot.children) {
                        val usuario = snapshot.getValue(ModeloUsuario::class.java)
                        usuario?.let { usuarios.add(it) }
                    }

                    taskCompletionSource.setResult(usuarios)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // error
                    taskCompletionSource.setException(databaseError.toException())
                }
            })
        return taskCompletionSource.task
    }

    fun buscarTodosUsuariosPorEmpresa(perfil: String = "Todos"): Task<MutableList<ModeloUsuario>> {//perfiles: todos, vendedor, administrador, inactivo
        Log.d("Perfiles", "Buscando Perfiles $perfil")
        val database = FirebaseDatabase.getInstance()
        val usuariosRef = database.getReference(TABLA_REFERENCIA)
        usuariosRef.keepSynced(true)
        val taskCompletionSource = TaskCompletionSource<MutableList<ModeloUsuario>>()
        usuariosRef.orderByChild("idEmpresa").equalTo(DatosPersitidos.datosEmpresa.id)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val usuarios = mutableListOf<ModeloUsuario>()


                    for (snapshot in dataSnapshot.children) {
                        val usuario = snapshot.getValue(ModeloUsuario::class.java)
                        if (usuario != null) {
                            // Filtrar por perfil
                            if (perfil.equals("Todos") ||
                                (perfil.equals("Vendedor")&& usuario.perfil.equals("Vendedor") ) ||
                                (perfil.equals("Admistrador")&& usuario.perfil.equals("Administrador")) ||
                                (perfil.equals("Inactivo") && usuario.perfil.equals("Inactivo"))
                            ) {
                                usuarios.add(usuario)
                            }
                        }


                    }

                    taskCompletionSource.setResult(usuarios)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // error
                    taskCompletionSource.setException(databaseError.toException())
                }
            })
        return taskCompletionSource.task
    }


}