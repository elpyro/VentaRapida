package com.castellanoseloy.ventarapida.procesos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.castellanoseloy.ventarapida.servicios.DatosPersitidos
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.datos.Variable
import com.google.android.material.snackbar.Snackbar
import com.itextpdf.text.Element
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Utilidades {

    fun crearSnackBarr(s: String, view: View) {

        val snackbar = Snackbar.make( view,s,Snackbar.LENGTH_SHORT)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundResource(R.color.rojo)
        snackbar.show()
    }

    fun calcularDiferenciasDeVariables(
        listaOriginal: List<Variable>?,
        listaModificada: List<Variable>?,
        multiplicador: Int
    ): MutableList<Variable> {
        val diferencias = mutableListOf<Variable>()

        // Mapear la lista original por idVariable
        val mapaOriginal = listaOriginal?.associateBy { it.idVariable } ?: emptyMap()
        // Mapear la lista modificada por idVariable
        val mapaModificada = listaModificada?.associateBy { it.idVariable } ?: emptyMap()

        // Iterar sobre la lista modificada
        listaModificada?.forEach { variableModificada ->
            val variableOriginal = mapaOriginal[variableModificada.idVariable]

            if (variableOriginal != null) {
                // Calcular la diferencia de cantidades
                var diferenciaCantidad = variableModificada.cantidad - variableOriginal.cantidad
                diferenciaCantidad=diferenciaCantidad*multiplicador
                if (diferenciaCantidad != 0) {
                    // Crear una nueva variable con la diferencia
                    val variableConDiferencia = variableModificada.copy(cantidad = diferenciaCantidad)
                    diferencias.add(variableConDiferencia)
                }
            } else {
                // Si la variable no estaba en la lista original, agregarla con su cantidad actual
                diferencias.add(variableModificada)
            }
        }

        // Iterar sobre la lista original para encontrar variables que ya no están en la lista modificada
        listaOriginal?.forEach { variableOriginal ->
            if (!mapaModificada.containsKey(variableOriginal.idVariable)) {
                // Si la variable no está en la lista modificada, agregarla con la cantidad negativa correspondiente
                var cantidadConDiferencia = -variableOriginal.cantidad * multiplicador
                val variableConDiferencia = variableOriginal.copy(cantidad = cantidadConDiferencia)
                diferencias.add(variableConDiferencia)
            }
        }
        Log.d("PromtFacturaGuardada", "Hubo cambios en las variables: $diferencias")
        return diferencias
    }


    fun verificarPermisosAdministrador(): Boolean{
        return DatosPersitidos.datosUsuario.perfil.equals("Administrador")
    }
    fun String.eliminarAcentosTildes(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        val pattern = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        return pattern.replace(normalized, "").lowercase(Locale.getDefault())
    }
        fun mostrarVariantesAdaptador(listaVariables:List<Variable>? = null, textView_variante: TextView) {
        textView_variante.visibility = View.GONE
            Log.d("mostrarVariantesRecibidas",listaVariables.toString())
        if (!listaVariables.isNullOrEmpty()) {
            // Hacer visible el TextView
            textView_variante.visibility = View.VISIBLE

            // Construir el texto con el formato "nombre X cantidad"
            val variantesTexto =
                listaVariables!!.joinToString(separator = "\n") { variable ->
                    "${variable.nombreVariable} X ${variable.cantidad}"
                }

            // Asignar el texto al TextView
            textView_variante.text = variantesTexto
        } else {
            // Ocultar el TextView si no hay variables
            textView_variante.visibility = View.GONE
        }
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

        return FileProvider.getUriForFile(context, "com.castellanoseloy.ventarapida.fileprovider", imageFile)
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



    fun cargarImagen(productUrl: String, imagenProducto: ImageView) {
        // Cargar la imagen solo si la URL no está vacía y es diferente a la anterior
        if (!productUrl.isEmpty() && imagenProducto.tag != productUrl) {
            imagenProducto.tag = productUrl
            Picasso.get()
                .load(productUrl)
                .networkPolicy(NetworkPolicy.OFFLINE) // Configurar la política de caché y persistencia
                .into(imagenProducto, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        // La imagen se cargó exitosamente desde la caché o persistencia
                    }

                    override fun onError(e: Exception) {
                        // Ocurrió un error al cargar la imagen desde la caché o persistencia
                        // Intentar cargar la imagen desde la red
                        Picasso.get().load(productUrl).into(imagenProducto)
                    }
                })
        } else if (productUrl.isEmpty()) {
            // Si la URL está vacía, mostrar una imagen por defecto o limpiar la vista
            // dependiendo del diseño que se quiera obtener
            imagenProducto.setImageResource(R.drawable.ic_menu_camera)
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

    fun calcularDiasRestantes(fechaObjetivo: Date): Pair<String, Long> {
        // Obtener la fecha actual
        val fechaActual = Calendar.getInstance().time

        // Calcular la diferencia en milisegundos entre las fechas
        val diferenciaMillis = fechaObjetivo.time - fechaActual.time

        // Calcular la diferencia en días
        val diasRestantes = TimeUnit.MILLISECONDS.toDays(diferenciaMillis)

        // Crear un String que indique cuántos días restan
        var mensaje = "Quedan $diasRestantes días"

        if (diasRestantes.toInt() < 0) {
            mensaje = "Plan Vencido"
        }

        // Retornar el mensaje y los días restantes
        return Pair(mensaje, diasRestantes)
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

     fun generarLogoCell(context: Context, logotipo: Drawable): PdfPCell {
        val logoTable = PdfPTable(1)
        logoTable.widthPercentage = 100f
        logoTable.defaultCell.border = PdfPCell.NO_BORDER
        logoTable.horizontalAlignment = Element.ALIGN_RIGHT
        logoTable.defaultCell.verticalAlignment = Element.ALIGN_RIGHT

        val logoCell: PdfPCell

        val logoDrawable = logotipo

        if (logoDrawable != null && logoDrawable is BitmapDrawable) {
            // Logo obtenido con éxito
            val bmp = logoDrawable.bitmap
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val logo = Image.getInstance(stream.toByteArray())
            logo.widthPercentage = 70f
            logo.scaleToFit(155f, 70f)

            logoCell = PdfPCell(logo)
            logoCell.horizontalAlignment = Element.ALIGN_CENTER
            logoCell.verticalAlignment = Element.ALIGN_CENTER
            logoCell.border = PdfPCell.NO_BORDER
            logoTable.addCell(logoCell)
        } else {
            // No se pudo obtener el logotipo, se usa una imagen predeterminada
            val defaultDrawable = ContextCompat.getDrawable(context, R.drawable.logo2_compra_rapidita)

            if (defaultDrawable is BitmapDrawable) {
                val bmp = defaultDrawable.bitmap
                val stream = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val logo = Image.getInstance(stream.toByteArray())
                logo.widthPercentage = 70f
                logo.scaleToFit(155f, 70f)

                logoCell = PdfPCell(logo)
            } else {
                // Convertir el VectorDrawable a Bitmap
                val width = defaultDrawable?.intrinsicWidth
                val height = defaultDrawable?.intrinsicHeight
                val bitmap = Bitmap.createBitmap(width!!, height!!, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                defaultDrawable?.setBounds(0, 0, canvas.width, canvas.height)
                defaultDrawable?.draw(canvas)

                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val logo = Image.getInstance(stream.toByteArray())
                logo.widthPercentage = 70f
                logo.scaleToFit(155f, 70f)

                logoCell = PdfPCell(logo)
            }

            logoCell.horizontalAlignment = Element.ALIGN_CENTER
            logoCell.verticalAlignment = Element.ALIGN_CENTER
            logoCell.border = PdfPCell.NO_BORDER
            logoTable.addCell(logoCell)
        }

        return logoCell
    }

}