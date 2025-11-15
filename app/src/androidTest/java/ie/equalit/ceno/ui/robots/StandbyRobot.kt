package ie.equalit.ceno.ui.robots

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.helpers.TestHelper

class StandbyRobot {

    class Transition {
        val mDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun waitForStandbyIfNeeded() {
            waitForStandbyLogo()
            waitForStandbyLogoGone()
            openAnywayIfNeeded()
        }
    }
}

fun standby(interact: StandbyRobot.() -> Unit): StandbyRobot.Transition {
    StandbyRobot().interact()
    return StandbyRobot.Transition()
}

fun waitForStandbyLogo() {
    standbyLogo().waitForExists(TestAssetHelper.waitingTime)
}

fun waitForStandbyLogoGone() {
    standbyLogo().waitUntilGone(TestAssetHelper.waitingTime)
}

fun openAnywayIfNeeded() {
    if (openAnywayButton().exists()) {
        openAnywayButton().click()
    }
}

private fun standbyLogo() = mDevice.findObject(
    UiSelector().resourceId("${TestHelper.packageName}:id/iv_standby_logo"),
)

private fun openAnywayButton() = mDevice.findObject(
    UiSelector().resourceId("${TestHelper.packageName}:id/btn_take_me_anyway"),
)
