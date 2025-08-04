package ie.equalit.ceno.ext

import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView

// Based on https://stackoverflow.com/a/45727769
fun TextView.makeLinks(link: String, onClickListener: () -> Unit) {
    val spannableString = SpannableString(this.text)
    val clickableSpan = object : ClickableSpan() {
        override fun updateDrawState(textPaint: TextPaint) {
            textPaint.color = textPaint.linkColor
            textPaint.isUnderlineText = true
        }

        override fun onClick(view: View) {
            Selection.setSelection((view as TextView).text as Spannable, 0)
            view.invalidate()
            onClickListener()
        }
    }
    // Find index of substring, case-insensitive
    val startIndexOfLink: Int = this.text.toString().lowercase().indexOf(link.lowercase())
    if(startIndexOfLink == -1) return // do not add link if substring not found
    spannableString.setSpan(
        clickableSpan, startIndexOfLink, startIndexOfLink + link.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    this.movementMethod = LinkMovementMethod.getInstance()
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}