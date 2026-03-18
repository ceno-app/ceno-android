package ie.equalit.ceno.ext

import android.view.Window
import android.view.WindowManager

fun Window.setSecureScreen(enabled : Boolean) {
    if(enabled) {
        this.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
    else {
        this.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}