package ie.equalit.ceno.metrics

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.makeLinks
import ie.equalit.ceno.settings.dialogs.WebViewPopupPanel

class ConsentRequestDialog(val context: Context) {

    fun show(complete: (Boolean) -> Unit, openMetricsSettings: () -> Unit) {

        val dialogView = View.inflate(context, R.layout.dialog_metrics_campaign001, null)
        dialogView.findViewById<TextView>(R.id.description).makeLinks(
            context.getString(R.string.metrics_consent_dialog_description_link)
        ) {
            WebViewPopupPanel(
                context,
                context as LifecycleOwner,
                context.getString(R.string.privacy_policy_url)
            ).show()
        }
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton(R.string.metrics_consent_accept) { _, _ ->
                complete(true)
            }
            .setOnDismissListener {
                /* No selection was made, but dialog was dismissed
                    grant consent */
                complete(true)

            }
            .create()

        dialogView.findViewById<TextView>(R.id.modify_privacy_settings).setOnClickListener {
            dialog.dismiss()
            complete(true)
            openMetricsSettings()
        }
        dialog.show()
    }
}
