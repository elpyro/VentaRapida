package com.castellanoseloy.ventarapida.ui.supscripciones

import android.app.AlertDialog
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.castellanoseloy.ventarapida.MainActivity
import com.castellanoseloy.ventarapida.R
import com.castellanoseloy.ventarapida.databinding.FragmentSuscripcionesDisponiblesBinding
import com.castellanoseloy.ventarapida.procesos.FirebaseDatosEmpresa
import com.castellanoseloy.ventarapida.procesos.Utilidades.calcularDiasRestantes
import com.castellanoseloy.ventarapida.procesos.Utilidades.convertirCadenaAFecha
import com.castellanoseloy.ventarapida.procesos.Utilidades.convertirFechaLegible

import com.google.common.collect.ImmutableList



class SuscripcionesDisponibles : Fragment() {
    private lateinit var diasRestantes: Pair<String, Long>
    private lateinit var binding: FragmentSuscripcionesDisponiblesBinding
    private lateinit var vista: View
    private lateinit var nuevoPlan:String
    private lateinit var billingClient :BillingClient
    private lateinit var viewModel: SuscripcionesDisponiblesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSuscripcionesDisponiblesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[SuscripcionesDisponiblesViewModel::class.java]

        datosPlan()

        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        conectar()

        return binding.root
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {

                Log.d("ComprasIntegradas", "Compra exitosa")

                val proximaFacturacion=viewModel.obtenerFechaVencimientoPlan().toString()
                val updates = hashMapOf(
                    "id" to MainActivity.datosEmpresa.id,
                    "plan" to nuevoPlan,
                    "proximo_pago" to proximaFacturacion
                )

                FirebaseDatosEmpresa.guardarDatosEmpresa(updates).addOnCompleteListener {
                    MainActivity.datosEmpresa.proximo_pago=proximaFacturacion
                    MainActivity.datosEmpresa.plan=nuevoPlan
                    MainActivity.planVencido=false

                    Toast.makeText(requireContext(),"Plan Actualizado",Toast.LENGTH_SHORT).show()

                    findNavController().popBackStack()
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.e("ComprasIntegradas", "La compra fue cancelada")
        } else {
            Log.e("ComprasIntegradas", "La compra fue cancelada")
            // Handle any other error codes.
        }
    }

    private fun listeners() {


        binding.mainItem.setOnClickListener {
            toggleSecondaryItems(binding.secondaryItems, binding.expandIcon)
        }

        binding.mainItem2.setOnClickListener {
            toggleSecondaryItems(binding.secondaryItems2, binding.expandIcon2)
        }

        binding.mainItem3.setOnClickListener {
            toggleSecondaryItems(binding.secondaryItems3, binding.expandIcon3)
        }
        binding.mainItem4.setOnClickListener {
            toggleSecondaryItems(binding.secondaryItems4, binding.expandIcon4)
        }


        val planActual=MainActivity.datosEmpresa.plan

        binding.buttonPlanBasico.setOnClickListener{
            nuevoPlan="Basico"
            val cambioPlan= viewModel.verificarPlan(nuevoPlan,planActual,diasRestantes)
            if(cambioPlan){
                consultarProductos("suscripcion_plan_basico")
            }else{
                mostrarDialogAlert()
            }

        }
        binding.buttonPlanPremium.setOnClickListener{
            nuevoPlan="Premium"
            val cambioPlan= viewModel.verificarPlan(nuevoPlan,planActual,diasRestantes)
            if(cambioPlan){
                consultarProductos("suscripcion_plan_premium")
            }else{
                mostrarDialogAlert()
            }
        }
        binding.buttonPlanEmpresarial.setOnClickListener{
            nuevoPlan="Empresarial"
            val cambioPlan= viewModel.verificarPlan(nuevoPlan,planActual,diasRestantes)
            if(cambioPlan){
                consultarProductos("suscripcion_plan_empresarial")
            }else{
                mostrarDialogAlert()
            }
        }
    }

    fun toggleSecondaryItems(
        secondaryItems: View,
        expandIcon: ImageView,

        ) {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.animacion_in)
        val fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.animacion_out)

        if (secondaryItems.visibility == View.GONE) {
            secondaryItems.visibility = View.VISIBLE
            secondaryItems.startAnimation(fadeIn)
            expandIcon.setImageResource(R.drawable.baseline_keyboard_double_arrow_up_24)
        } else {
            secondaryItems.visibility = View.GONE
            secondaryItems.startAnimation(fadeOut)
            expandIcon.setImageResource(R.drawable.baseline_keyboard_double_arrow_down_24)
        }
    }
    private fun mostrarDialogAlert() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setTitle("Cambio de plan")
        alertDialogBuilder.setMessage("No puedes cambiar a otro plan con mas de 1 día disponible")
        alertDialogBuilder.setPositiveButton("Aceptar") { _, _ ->

        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun conectar() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    // El BillingClient está listo. Puedes consultar las compras aquí.
                    Log.d("ComprasIntegradas", "Se ha conectado")
                    listeners() //preparar los botones
                }
            }

            override fun onBillingServiceDisconnected() {
                // Intenta restablecer la conexión en el próximo intento a Google Play.
                conectar()
            }
        })

    }

    private fun consultarProductos(plan:String) {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ImmutableList.of(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(plan)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            // Procesar la respuesta en el hilo principal después de una demora de 2 segundos

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("ComprasIntegradas", "respuesta: ${billingResult.responseCode}")
                    // Realiza cualquier acción adicional aquí
                    // La consulta se realizó con éxito, ahora puedes procesar los detalles del producto

                    if (productDetailsList.isNotEmpty()) {
                        // Puedes acceder a los detalles del producto
                        val productDetails = productDetailsList[0]

                        val title = productDetails.title
                        Log.d("ComprasIntegradas", "El producto disponible a comprar es: $title")

                        realizarCobro(productDetails)

                        // Realiza cualquier acción adicional aquí
                    } else {
                        Log.e("ComprasIntegradas", "No se encontraron detatalles del plan")
                    }

                } else {
                    Log.e("ComprasIntegradas", "Error en compra")
                }
        }
    }

    private fun realizarCobro(productDetails: ProductDetails?) {
        Log.d("ComprasIntegradas","detalle del producto ${productDetails!!}")
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(productDetails)
                .setOfferToken(productDetails.subscriptionOfferDetails?.get(0)?.offerToken!! )
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
        Log.d("ComprasIntegradas","Resultado de la compra $billingResult")
    }


    private fun datosPlan() {

        binding.textViewCurrentPlan.text = "Plan Actual: ${MainActivity.datosEmpresa.plan}"

        if(MainActivity.datosEmpresa.proximo_pago != ""){
            binding.textViewExpirationDate.text = "Fecha de Vencimiento: ${convertirFechaLegible(MainActivity.datosEmpresa.proximo_pago)}"

            diasRestantes= calcularDiasRestantes(convertirCadenaAFecha(MainActivity.datosEmpresa.proximo_pago)!!)

            binding.textViewDiasRestantes.text = diasRestantes.first
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vista=view
    }


}