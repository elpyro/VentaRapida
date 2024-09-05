package com.castellanoseloy.cataplus.ui.promts

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.castellanoseloy.cataplus.R
import android.view.LayoutInflater
import android.widget.Button
import com.castellanoseloy.cataplus.Login
import com.castellanoseloy.cataplus.servicios.DatosPersitidos
import com.castellanoseloy.cataplus.procesos.FirebaseUsuarios
import com.castellanoseloy.cataplus.procesos.Preferencias
import com.firebase.ui.auth.AuthUI


class PromtEliminarCuenta {
    private var alertDialog: AlertDialog? = null
    private var editTextConfirmacions: EditText? = null
    private var button_eliminar: Button? = null
    fun eliminar(context: Context, activity: Activity) {


        val dialogBuilder = AlertDialog.Builder(context)

// Inflar el layout para el diálogo
        // Inflar el layout para el diálogo
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.promt_eliminar_cuenta, null)
        dialogBuilder.setView(dialogView)

        editTextConfirmacions = dialogView.findViewById(R.id.edittext_confirmacion)
        button_eliminar = dialogView.findViewById(R.id.button_eliminarCuenta)

        button_eliminar?.setOnClickListener {
            val confimacion = editTextConfirmacions?.text.toString().trim()

            if (confimacion.equals("ELIMINAR")) {
                Log.d("Cuenta", "Cuenta eliminada")
                if (DatosPersitidos.datosUsuario.id.isNotEmpty()) FirebaseUsuarios.eliminarUsuarioPorId(
                    DatosPersitidos.datosUsuario.id
                ).addOnCompleteListener {
                    Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_LONG).show()
                alertDialog?.dismiss()
                    cerrarSesion(context, activity)
                }
            }else{
                Toast.makeText(context,"Escriba ELIMINAR en mayusculas para borrar su cuenta", Toast.LENGTH_LONG).show()
            }

        }

        alertDialog = dialogBuilder.create()
        alertDialog?.show()

    }


    fun cerrarSesion(context: Context, activity: Activity) {
        DatosPersitidos.ventaProductosSeleccionados.clear()
        DatosPersitidos.compraProductosSeleccionados.clear()

        val preferencias= Preferencias()
        preferencias.guardarPreferenciaListaSeleccionada(context,
            DatosPersitidos.compraProductosSeleccionados,"compra_seleccionada"
        )
        preferencias.guardarPreferenciaListaSeleccionada(context,
            DatosPersitidos.ventaProductosSeleccionados,"venta_seleccionada"
        )

        AuthUI.getInstance().signOut(context)
            .addOnCompleteListener {

                Toast.makeText(context, "Sesion Cerrada", Toast.LENGTH_LONG).show()
                val intent = Intent(context, Login::class.java)
                activity.startActivity(intent)
                activity.finish()
            }
    }
}