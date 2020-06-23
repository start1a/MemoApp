package com.start3a.memoji.views

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import com.start3a.memoji.R

class LoadingProgressBar {

    companion object {
        var dialogInterfaceLoading: DialogInterface? = null

        fun Progress_ProcessingData(context: Context) {
            val alertDialog = AlertDialog.Builder(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_progress_bar_for_waiting, null)
            dialogInterfaceLoading = alertDialog
                .setView(view)
                .setCancelable(false)
                .show()
        }
    }

}