package com.example.ventarapida.procesos

import android.widget.Toast
import com.example.ventarapida.MainActivity
import com.example.ventarapida.datos.ModeloClientes
import com.example.ventarapida.datos.ModeloUsuario
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseUsuarios {


    private const val TABLA_REFERENCIA = "Usuarios"

    fun guardarUsuario(updates: HashMap<String, Any>) {
        val database = FirebaseDatabase.getInstance()
        val registroRef = database.getReference(TABLA_REFERENCIA).child((updates["id"] as String))
        registroRef.updateChildren(updates)
    }

    fun buscarUsuariosPorCorreo(correo: String): Task<MutableList<ModeloUsuario>> {
        val database = FirebaseDatabase.getInstance()
        val usuariosRef = database.getReference(TABLA_REFERENCIA)
        usuariosRef.keepSynced(true)
        val taskCompletionSource = TaskCompletionSource<MutableList<ModeloUsuario>>()
        usuariosRef.orderByChild("correo").equalTo(correo.toLowerCase()).addListenerForSingleValueEvent(object :
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


}