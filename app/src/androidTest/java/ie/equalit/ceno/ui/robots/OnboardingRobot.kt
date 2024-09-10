package ie.equalit.ceno.ui.robots

import android.os.Build
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.helpers.TestAssetHelper.waitingTime
import ie.equalit.ceno.helpers.TestHelper
import ie.equalit.ceno.helpers.click
import ie.equalit.ceno.settings.Settings

class OnboardingRobot {

    fun verifyStartTooltipExists() = startTooltipExists()

    fun verifyStartTooltipText() = assertStartTooltipText()

    fun verifyStartTooltipButtons() = assertStartTooltipButtons()

    fun verifyPublicPersonalTooltip() = assertPublicPersonalTooltip()

    fun beginTooltipsTour() = getStartedButton().click()
    fun verifyExitButton() = assertExitButton()
    fun clickExit() = exitButton().click()
    fun verifyPermissionsTooltip() = assertPermissionsTooltip()
    fun clickPermissions() = getStartedButton().click()

    class Transition {
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun givePermissionsIfNeeded(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                continueOnboardingButton().waitForExists(TestAssetHelper.waitingTime)
                continueOnboardingButton().click()
                givePermissions()
            }
        }
        fun skipOnboardingIfNeeded() {
            if (Settings.shouldShowOnboarding(TestHelper.appContext)) {
                skipCenoTourButton().waitForExists(TestAssetHelper.waitingTime)
                skipCenoTourButton().click()
                continuePermissionsButton().waitForExists(TestAssetHelper.waitingTime)
                continuePermissionsButton().click()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    givePermissions()
                }
            }
        }
    }
}

fun startTooltipExists() {
    onView(withId(R.id.tooltip_overlay_start_layout)).check(matches(isDisplayed()))
}
fun assertExitButton() {
    exitButton().check(matches(isDisplayed()))
}
private fun assertStartTooltipText() {
    onView(withId(R.id.tv_start_tooltip_description)).check(matches(withText(R.string.start_tooltip_description)))
}

private fun assertPermissionsTooltip() {
    onView(withId(R.id.tooltip_overlay_start_layout)).check(matches(isDisplayed()))
    onView(withId(R.id.tv_start_tooltip_title)).check(matches(withText(R.string.onboarding_permissions_title)))
    getStartedButton().check(matches(withText(R.string.onboarding_battery_button)))
}

private fun assertStartTooltipButtons() {
    getStartedButton().check(matches(withText(R.string.onboarding_btn_get_started)))
    skipTourButton().check(matches(isDisplayed()))
}

private fun assertPublicPersonalTooltip() {
    onView(withId(R.id.material_target_prompt_view)).check(matches(isDisplayed()))
    onView(withId(R.id.btn_skip_tour)).check(matches(isDisplayed()))
}
fun onboarding(interact: OnboardingRobot.() -> Unit): OnboardingRobot.Transition {
    OnboardingRobot().interact()
    return OnboardingRobot.Transition()
}

private fun getStartedButton() = onView(ViewMatchers.withId(R.id.btn_start_ceno_tour))
private fun skipTourButton() = onView(ViewMatchers.withText(R.string.skip_the_tour_button))
private fun exitButton() = onView(withId(R.id.btn_skip_tour))

private fun skipCenoTourButton() = mDevice.findObject(
    UiSelector().resourceId("${TestHelper.packageName}:id/btn_skip_all_ceno_tour"),
)

fun waitForContinueButton() {
    continueOnboardingButton().waitForExists(TestAssetHelper.waitingTime)
}

private fun continueOnboardingButton() = mDevice.findObject(
    UiSelector().resourceId("${TestHelper.packageName}:id/btn_onboarding_continue"),
)

private fun skipOnboardingButton() = mDevice.findObject(
    UiSelector().resourceId("${TestHelper.packageName}:id/btn_onboarding_skip"),
)

private fun permissionAllowButton() = mDevice.findObject(
    UiSelector().resourceId("com.android.permissioncontroller:id/permission_allow_button")
)

private fun permissionDenyButton() = mDevice.findObject(
    UiSelector().resourceId("com.android.permissioncontroller:id/permission_deny_button")
)

private fun backgroundAllowButton() = mDevice.findObject(
UiSelector().resourceId("android:id/button1")
)

private fun backgroundDenyButton() = mDevice.findObject(
    UiSelector().resourceId("android:id/button2")
)


fun givePermissions() {
    //for allowing notifications
    permissionAllowButton().waitForExists(waitingTime)
    permissionAllowButton().click()
    if(backgroundAllowButton().waitForExists(waitingTime)) {
        backgroundAllowButton().click()
    }
}

fun denyPermissions() {
    //for allowing notifications
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionDenyButton().waitForExists(waitingTime)
        permissionDenyButton().click()
    }
    //for battery optimizations
    if(backgroundDenyButton().waitForExists(waitingTime)) {
        backgroundDenyButton().clickAndWaitForNewWindow(waitingTime)
    }
}
