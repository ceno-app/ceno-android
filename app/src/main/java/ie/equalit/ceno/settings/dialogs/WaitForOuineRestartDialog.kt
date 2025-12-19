package ie.equalit.ceno.settings.dialogs

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import ie.equalit.ceno.R

class WaitForOuineRestartDialog (
    context: Context,
    title: String
) {
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)

    init {
        val dialogView = View.inflate(context, R.layout.wait_for_ouinet_restart_dialog, null)
        builder.apply {
            setTitle(title)
            setView(dialogView)
            setCancelable(false)
        }
    }

    fun getDialog (): AlertDialog {
        return builder.create().apply {
            setCanceledOnTouchOutside(false)
        }
    }
}