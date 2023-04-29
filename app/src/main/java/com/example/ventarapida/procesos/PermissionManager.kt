package com.example.ventarapida.procesos

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: AppCompatActivity) {

    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 100
    }

    fun checkAndRequestStoragePermission() {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(activity, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                REQUEST_CODE_STORAGE_PERMISSION
            )
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso concedido, se puede acceder al almacenamiento externo
                } else {
                    // Permiso denegado, no se puede acceder al almacenamiento externo
                }
                return
            }
        }
    }
}