package ru.icames.store.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.core.content.res.ResourcesCompat
import ru.icames.store.R

object MessageReporter {
    fun showMessage(
            context: Context?,
            title: String?,
            message: String?
    ) {
        val builder =
            AlertDialog.Builder(context!!)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setNeutralButton(
            "OK"
        ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setTextColor(ResourcesCompat.getColor(context.resources, R.color.colorAccent, null))
    }
}
