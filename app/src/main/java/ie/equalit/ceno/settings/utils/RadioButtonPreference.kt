package ie.equalit.ceno.settings.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.widget.RadioButton
import androidx.core.content.withStyledAttributes
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import ie.equalit.ceno.R

interface GroupableRadioButton {
    fun updateRadioValue(isChecked: Boolean)

    fun addToRadioGroup(radioButton: GroupableRadioButton)
}

/**
 * Connect all the given radio buttons into a group,
 * so that when one radio is checked the others are unchecked.
 */
fun addToRadioGroup(vararg radios: GroupableRadioButton) {
    for (i in 0..radios.lastIndex) {
        for (j in (i + 1)..radios.lastIndex) {
            radios[i].addToRadioGroup(radios[j])
            radios[j].addToRadioGroup(radios[i])
        }
    }
}

fun Iterable<GroupableRadioButton>.uncheckAll() {
    forEach { it.updateRadioValue(isChecked = false) }
}

class RadioButtonPreference(
    context: Context,
    attrs: AttributeSet? = null
) : Preference(context, attrs), GroupableRadioButton {
    private val radioGroups = mutableListOf<GroupableRadioButton>()
    private var radioButton: RadioButton? = null
    private var defaultValue: Boolean = false
    private var clickListener: (() -> Unit)? = null


    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    val isChecked: Boolean
        get() = radioButton?.isChecked == true

    init {
        layoutResource = R.layout.preference_radio_button
        context.withStyledAttributes(
            attrs,
            R.styleable.RadioButtonPreference
        ) {
            defaultValue = when {
                hasValue(R.styleable.RadioButtonPreference_defaultValue) ->
                    getBoolean(R.styleable.RadioButtonPreference_defaultValue, false)

                hasValue(R.styleable.RadioButtonPreference_android_defaultValue) ->
                    getBoolean(R.styleable.RadioButtonPreference_android_defaultValue, false)

                else -> false
            }
        }
    }

    override fun updateRadioValue(isChecked: Boolean) {
        persistBoolean(isChecked)
        radioButton?.isChecked = isChecked
        onPreferenceChangeListener?.onPreferenceChange(this, isChecked)
    }

    override fun addToRadioGroup(radioButton: GroupableRadioButton) {
        radioGroups.add(radioButton)
    }

    fun onClickListener(listener: (() -> Unit)) {
        clickListener = listener
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        radioButton = holder.findViewById(R.id.pref_radio_button) as RadioButton
        radioButton?.isChecked = preferences.getBoolean(key, defaultValue)
        setOnPreferenceClickListener {
            updateRadioValue(true)

            toggleRadioGroups()
            clickListener?.invoke()
            true
        }
    }

    private fun toggleRadioGroups() {
        if (radioButton?.isChecked == true) {
            radioGroups.uncheckAll()
        }
    }
}