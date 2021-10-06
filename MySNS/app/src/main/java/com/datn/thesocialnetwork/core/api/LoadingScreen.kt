package com.datn.thesocialnetwork.core.api

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.datn.thesocialnetwork.R.layout.layout_loading

object LoadingScreen {

    private var dialogLoading: AlertDialog? = null

    fun show(context: Context?) {
        hide()
        context?.let { contextNN ->
            val builder = AlertDialog.Builder(contextNN)
            builder.setView(layout_loading)
            builder.setCancelable(false)
            dialogLoading = builder.create()
            dialogLoading!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogLoading!!.show()
        }
    }

    fun hide() {
        if (dialogLoading != null && dialogLoading!!.isShowing) dialogLoading!!.dismiss()
    }
}