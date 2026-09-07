package ie.equalit.ceno.settings.dialogs

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getString
import androidx.core.view.children
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.Settings
import ie.equalit.ouinet.Config

class LogLevelDialog(
    val context: Context,
    private val setLogLevelChangeListener: SetLogLevelListener?
) {

    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    private var allSupportedLogLevels = mutableListOf<Config.LogLevel>()
    private var currentLogLevel: Config.LogLevel? = null

    init {

        currentLogLevel = Config.LogLevel.valueOf(Settings.getLogLevel(context))

        val logLevelChangeDialog = View.inflate(context, R.layout.log_level_dialog, null)
        val radioGroup = logLevelChangeDialog.findViewById<RadioGroup>(R.id.radio_group)

        builder.apply {
            setTitle(getString(context, R.string.change_log_level))
            setView(logLevelChangeDialog)
            setNegativeButton(R.string.dialog_cancel) { dialog: DialogInterface, _ -> dialog.cancel() }
            setPositiveButton(R.string.update) { _, _ ->
                if (radioGroup.checkedRadioButtonId == -1) {
                    return@setPositiveButton
                }

                val checkedIndex =
                    radioGroup.children.indexOfFirst { it.id == radioGroup.checkedRadioButtonId }

                allSupportedLogLevels[checkedIndex].let { level ->
                    setLogLevelChangeListener?.onLogLevelSelected(level)
                }
            }

            // clear list
            allSupportedLogLevels.clear()

            // Add subsequent levels
            Config.LogLevel.entries.toTypedArray()
                .forEach {
                    val radioButton = LayoutInflater.from(context)
                        .inflate(
                            R.layout.item_log_level,
                            radioGroup,
                            false
                        ) as RadioButton
                    radioButton.apply {
                        isClickable = true
                        text = it.name
                    }
                    radioGroup.addView(radioButton)
                    if (it == currentLogLevel) {
                        radioButton.performClick()
                    }
                    allSupportedLogLevels.add(it)
                }
        }
    }

    fun getDialog(): AlertDialog {
        return builder.create()
    }

    interface SetLogLevelListener {
        fun onLogLevelSelected(logLevel: Config.LogLevel)
    }

    companion object {
        private const val TAG = "LanguageChangeDialog"
    }
}
