package ie.equalit.ceno

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.core.text.buildSpannedString
import ie.equalit.ceno.settings.Settings
import org.cleaninsights.sdk.Feature

class ConsentRequestUi(private val context: Context) {

    fun show(complete: (Boolean) -> Unit) {

        val dialogView = View.inflate(context, R.layout.clean_insights_nudge_dialog, null)
        dialogView.findViewById<TextView>(R.id.explainer).text = buildSpannedString {

            append(context.getString(R.string.clean_insights_explainer))

//            color(ContextCompat.getColor(context, R.color.accent)) {
//                click(false, onClick = {
//                    // todo: open popup?
//                }) {
//                    append(" ")
//                    append(context.getString(R.string.learn_more_title))
//                    append(".")
//                }
//            }
        }
        val deviceType = dialogView.findViewById<View>(R.id.checkbox) as CheckBox
        deviceType.isChecked = Settings.isCleanInsightsDeviceTypeIncluded(context)
        deviceType.setOnCheckedChangeListener { _, isChecked ->
            // Save to shared preferences
            Settings.setCleanInsightsDeviceType(context, isChecked)
        }

        val deviceLocale = dialogView.findViewById<View>(R.id.checkbox2) as CheckBox
        deviceLocale.isChecked = Settings.isCleanInsightsDeviceLocaleIncluded(context)
        deviceLocale.setOnCheckedChangeListener { _, isChecked ->
            // Save to shared preferences
            Settings.setCleanInsightsDeviceLocale(context, isChecked)
        }

        AlertDialog.Builder(context)
            .setView(dialogView)
            .setNegativeButton(R.string.clean_insights_maybe_later) { _, _ ->
                complete(false)
            }
            .setPositiveButton(R.string.clean_insights_opt_in) { _, _ ->
                complete(true)
            }
            /* Do nothing when dismissed
            .setOnDismissListener {
            }
            */
            .create()
            .show()
    }
}
