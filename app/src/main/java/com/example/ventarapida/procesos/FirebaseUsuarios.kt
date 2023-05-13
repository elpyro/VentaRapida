package com.example.ventarapida.procesos

import com.example.ventarapida.MainActivity
import com.google.firebase.database.FirebaseDatabase

object FirebaseUsuarios {


    private const val TABLA_REFERENCIA = "Usuarios"

    fun guardarUsuario(updates: HashMap<String, Any>) {
        val database = FirebaseDatabase.getInstance()

        val registroRef = database.getReference(MainActivity.datosEmpresa.id).child(TABLA_REFERENCIA).child((updates["id"] as String))

        registroRef.updateChildren(updates)
    }
}