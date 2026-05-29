package ie.equalit.ceno.browser.dialogs

import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.Settings

class LoadExternalUrlDialog(
    val context: Context,
    val url: String,
    val onPositiveButtonClicked: () -> Unit = {}
) {
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)

    init {
        val view = View.inflate(context, R.layout.dialog_load_external_url, null)
        val tvMessage = view.findViewById<TextView>(R.id.tv_external_url_message)
        tvMessage.text = context.getString(R.string.dialog_external_url_message, url)
        val doNotShowCheckbox = view.findViewById<CheckBox>(R.id.external_url_do_not_show_checkbox)
        builder.apply {
            setTitle(R.string.dialog_external_url_title)
            setView(view)
            setPositiveButton(R.string.ceno_on_mobile_data_dialog_continue) { _, _ ->
                Settings.setVerifyExternalUrl(context, !doNotShowCheckbox.isChecked)
                onPositiveButtonClicked.invoke()
            }
            setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
    }

    fun getDialog(): AlertDialog {
        return builder.create()
    }
}