package ru.icames.store.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import ru.icames.store.R


object LoadingDialog {
    fun newInstance(context: Context): AlertDialog {
        val dialog: AlertDialog
        val dialogBuilder =
                AlertDialog.Builder(context)
        val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.dialog_processing, null)
        dialogBuilder.setView(view)
        dialog = dialogBuilder.create()
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    fun newInstanceWithoutCancelable(context: Context): AlertDialog {
        val dialog: AlertDialog
        val dialogBuilder =
                AlertDialog.Builder(context)
        val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.dialog_processing, null)
        dialogBuilder.setView(view)
        dialog = dialogBuilder.create()
        dialog.setCancelable(false)
        return dialog
    }
}
