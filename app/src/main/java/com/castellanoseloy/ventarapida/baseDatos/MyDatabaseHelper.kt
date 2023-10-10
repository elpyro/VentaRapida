package com.castellanoseloy.ventarapida.baseDatos

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MiBaseDatos.db"
        private const val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Aquí se ejecutan las sentencias SQL para crear las tablas y definir los campos
        db?.execSQL("CREATE TABLE IF NOT EXISTS transaccionesSumaRestaProductos(idTransaccion Text , idProducto TEXT, cantidad Text, subido Text)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Aquí se ejecutan las sentencias SQL para actualizar la estructura de la base de datos
        db?.execSQL("DROP TABLE IF EXISTS transaccionesSumaRestaProductos")
        onCreate(db)
    }


}