package com.example.ventarapida.procesos

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.FileProvider
import com.example.ventarapida.MainActivity
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Utilidades {

    fun verificarPermisosAdministrador(): Boolean{
        return MainActivity.datosUsuario.perfil.equals("Administrador")
    }
    fun String.eliminarAcentosTildes(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        val pattern = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        return pattern.replace(normalized, "").lowercase(Locale.getDefault())
    }

    fun obtenerImageUriParaCompartir(bitmap: Bitmap, context: Context): Uri {
        //otorga el fileprovinder para compartir
        val idImagen= UUID.randomUUID().toString()
        val imagePath = "${context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/$idImagen.png"
        val imageFile = File(imagePath)

        // Guardar el bitmap como archivo en el almacenamiento externo
        imageFile.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        return FileProvider.getUriForFile(context, "com.example.ventarapida.fileprovider", imageFile)
    }
    fun String.eliminarPuntosComasLetras(): String {
        return this.replace(Regex("[^\\d.]"), "")
            .replace(".", "")
            .replace(",", "")
    }

    fun String.eliminarPuntos(): String {
        return this.replace(".", "")
    }

    fun fechaActual():String{
        val fechaActual = Date()
        // Crear un formato de fecha personalizado
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        // Formatear la fecha actual en el formato deseado
        return  formatoFecha.format(fechaActual)
    }
    fun esperarUnSegundo(){
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun ocultarTeclado(context: Context,view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun separarNumerosDelString(string: String): Pair<String, String?> {
        // Expresión regular para detectar números al final del string
        val regex = "(\\d+)$"
        val matchResult = Regex(regex).find(string)
        if (matchResult != null) {
            // Si hay números al final del string, separarlos del resto del string
            val numeros = matchResult.value
            val resto = string.removeSuffix(numeros)
            return Pair(resto, numeros)
        } else {
            // Si no hay números al final del string, devolver el string completo y null
            return Pair(string.trim(), null)
        }
    }


    fun obtenerFechaActual(): String {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")) // Especifica la configuración regional adecuada
        val fechaActual = Date()
        return formatoFecha.format(fechaActual)
    }

    fun obtenerHoraActual(): String {
        val formatoHora = SimpleDateFormat("HH:mm:ss", Locale("es", "ES")) // Especifica la configuración regional adecuada
        val horaActual = Date()
        return formatoHora.format(horaActual)
    }

    fun convertirCadenaAFecha(cadenaFecha: String): Date? {
        // Define el formato de la cadena de fecha
        val formato = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)

        // Establece la zona horaria adecuada (en este caso, GMT-05:00)
        formato.timeZone = TimeZone.getTimeZone("GMT-05:00")

        try {
            // Intenta analizar la cadena de fecha en un objeto Date
            return formato.parse(cadenaFecha)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }



    fun obtenerFechaUnix():Long{
        // Obtener la fecha actual sin la hora
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val fecha = calendar.timeInMillis
        return fecha
    }

    fun convertirFechaAUnix(fecha: String): Long {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaObjeto = formatoFecha.parse(fecha)

        val calendar = Calendar.getInstance()
        calendar.time = fechaObjeto
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }



    fun convertirUnixAFecha(fechaUnix: Long): String {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fecha = Date(fechaUnix)
        return formatoFecha.format(fecha)
    }

    fun calcularDiasRestantes(fechaObjetivo: Date): String {
        // Obtener la fecha actual
        val fechaActual = Calendar.getInstance().time

        // Calcular la diferencia en milisegundos entre las fechas
        val diferenciaMillis = fechaObjetivo.time - fechaActual.time

        // Calcular la diferencia en días
        val diasRestantes = TimeUnit.MILLISECONDS.toDays(diferenciaMillis)

        // Crear un String que indique cuántos días restan
        var mensaje = "Quedan $diasRestantes días"

        if(diasRestantes.toInt()<0){
            mensaje="Plan Vencido"
        }

        return mensaje
    }

    fun convertirFechaLegible(fechaStr: String): String {
        try {
            val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val fecha = inputFormat.parse(fechaStr)
            return outputFormat.format(fecha)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return fechaStr // Si no se puede convertir, devuelve la cadena original
    }

    fun EditText.escribirFormatoMoneda() {
        val textWatcher = object : TextWatcher {
            var isProgrammaticChange = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isProgrammaticChange && s != null) {
                    removeTextChangedListener(this)
                    val precio = s.toString()
                    setText(precio.formatoMonenda())
                    setSelection(text.length)
                    addTextChangedListener(this)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        addTextChangedListener(textWatcher)
    }

    fun String.formatoMonenda(): String {
        val formatoMoneda = DecimalFormat("###,###.00") // Dos decimales
        formatoMoneda.decimalFormatSymbols = DecimalFormatSymbols(Locale("es", "CO"))
        val valorDouble = this.toDoubleOrNull() ?: return this // Retorna el string original si no se puede convertir a double

        if (valorDouble == 0.0) {
            return "0"
        }

        val formattedValue = formatoMoneda.format(valorDouble)

        // Verifica si el resultado tiene ",00" o ".00" al final de la cadena y si es así, lo reemplaza por "0"
        val resultWithoutDecimals = formattedValue.replace(Regex("[,.]00$"), "")

        // Verifica si el resultado tiene ".0" al final de la cadena y si es así, lo reemplaza por ""
        return resultWithoutDecimals.replace(".0$", "")
    }
}