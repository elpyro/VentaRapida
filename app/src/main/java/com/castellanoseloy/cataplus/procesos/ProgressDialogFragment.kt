package com.castellanoseloy.cataplus.procesos

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.castellanoseloy.cataplus.R

class ProgressDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val progressDialog = ProgressDialog(activity)
        progressDialog?.setIcon(R.drawable.logo2_compra_rapidita)
        progressDialog.setMessage("Guardando...")
        progressDialog.setCancelable(false)
        return progressDialog
    }
}