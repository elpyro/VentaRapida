package com.castellanoseloy.cataplus.baseDatos

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MiBaseDatos.db"
        private const val DATABASE_VERSION = 3 // Incrementa la versión de la base de datos
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            """
            CREATE TABLE IF NOT EXISTS transaccionesSumaRestaProductos(
                idTransaccion TEXT, 
                idProducto TEXT, 
                cantidad TEXT, 
                subido TEXT,
                listaVariables TEXT -- para guardar la lista de variables como JSON
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db?.execSQL("ALTER TABLE transaccionesSumaRestaProductos ADD COLUMN listaVariables TEXT")
        }
    }
}
