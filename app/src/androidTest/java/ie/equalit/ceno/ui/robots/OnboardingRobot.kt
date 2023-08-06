package ie.equalit.ceno.ui.robots

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.helpers.TestHelper
import ie.equalit.ceno.settings.CustomPreferenceManager

class OnboardingRobot {

    class Transition {
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun skipOnboardingIfNeeded() {
            if (CustomPreferenceManager.getBoolean(TestHelper.appContext, R.string.pref_key_show_onboarding)) {
                skipOnboardingButton().waitForExists(TestAssetHelper.waitingTime)
                skipOnboardingButton().click()
            }
        }
    }
}

fun onboarding(interact: OnboardingRobot.() -> Unit): OnboardingRobot.Transition {
    OnboardingRobot().interact()
    return OnboardingRobot.Transition()
}

private fun skipOnboardingButton() = mDevice.findObject(
    UiSelector().resourceId("${TestHelper.packageName}:id/button2"),
)
