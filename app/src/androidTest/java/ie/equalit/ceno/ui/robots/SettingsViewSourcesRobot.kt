package ie.equalit.ceno.ui.robots

import android.view.View
import android.widget.Checkable
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.hasCousin
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.Description

/**
 * Implementation of Robot Pattern for the settings privacy menu.
 */
class SettingsViewSourcesRobot {

    fun verifySourcesUpButton() = assertSourcesUpButton()
    fun verifySourcesSettings() = assertSourcesSettingsView()

    fun verifyWebsiteCheckbox(): ViewInteraction = assertWebsiteCheckbox()
    fun verifyWebsiteSummary(): ViewInteraction = assertWebsiteSummary()
    fun verifyPrivatelyCheckbox(): ViewInteraction = assertPrivatelyCheckbox()
    fun verifyPrivatelySummary(): ViewInteraction = assertPrivatelySummary()
    fun verifyPubliclyCheckbox(): ViewInteraction = assertPubliclyCheckbox()
    fun verifyPubliclySummary(): ViewInteraction = assertPubliclySummary()
    fun verifySharedCheckbox(): ViewInteraction = assertSharedCheckbox()
    fun verifySharedSummary(): ViewInteraction = assertSharedSummary()

    fun toggleWebsiteCheckbox(value : Boolean) {
        websiteCheckbox().perform(setChecked(value))
    }
    fun togglePrivatelyCheckbox(value : Boolean) {
        privatelyCheckbox().perform(setChecked(value))
    }
    fun togglePubliclyCheckbox(value : Boolean) {
        publiclyCheckbox().perform(setChecked(value))
    }
    fun toggleSharedCheckbox(value : Boolean) {
        sharedCheckbox().perform(setChecked(value))
    }

    fun setWebsiteSources(website: Boolean, private: Boolean, public: Boolean, shared : Boolean){
        verifyWebsiteCheckbox()
        toggleWebsiteCheckbox(website)
        verifyPrivatelyCheckbox()
        togglePrivatelyCheckbox(private)
        verifyPubliclyCheckbox()
        togglePubliclyCheckbox(public)
        verifySharedCheckbox()
        toggleSharedCheckbox(shared)
    }


    class Transition {
        fun settingsViewSearch(interact: SettingsViewSourcesRobot.() -> Unit): Transition {
            return Transition()
        }

        fun goBack(interact: SettingsViewRobot.() -> Unit): SettingsViewRobot.Transition {
            mDevice.pressBack()
            SettingsViewRobot().interact()
            return SettingsViewRobot.Transition()
        }
    }
}

private fun sourcesUpButton() = onView(ViewMatchers.withContentDescription("Navigate up"))

private fun sourcesSettingsView() = onView(ViewMatchers.withText(R.string.preferences_ceno_website_sources))

private fun websiteCheckbox() = onView(
    CoreMatchers.allOf(
        ViewMatchers.withId(android.R.id.checkbox),
        hasCousin(ViewMatchers.withText(R.string.preferences_ceno_sources_origin))
    )
)
private fun websiteSummary() = onView(ViewMatchers.withText(R.string.preferences_ceno_sources_origin_summary))
private fun privatelyCheckbox() = onView(
    CoreMatchers.allOf(
        ViewMatchers.withId(android.R.id.checkbox),
        hasCousin(ViewMatchers.withText(R.string.preferences_ceno_sources_private))
    )
)
private fun privatelySummary() = onView(ViewMatchers.withText(R.string.preferences_ceno_sources_private_summary))
private fun publiclyCheckbox() = onView(
    CoreMatchers.allOf(
        ViewMatchers.withId(android.R.id.checkbox),
        hasCousin(ViewMatchers.withText(R.string.preferences_ceno_sources_public))
    )
)
private fun publiclySummary() = onView(ViewMatchers.withText(R.string.preferences_ceno_sources_public_summary))
private fun sharedCheckbox() = onView(
    CoreMatchers.allOf(
        ViewMatchers.withId(android.R.id.checkbox),
        hasCousin(ViewMatchers.withText(R.string.preferences_ceno_sources_peers))
    )
)
private fun sharedSummary() = onView(ViewMatchers.withText(R.string.preferences_ceno_sources_peers_summary))

private fun assertSourcesUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertSourcesSettingsView() = sourcesSettingsView()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertWebsiteCheckbox() = websiteCheckbox()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertWebsiteSummary() = websiteSummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPrivatelyCheckbox() = privatelyCheckbox()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPrivatelySummary() = privatelySummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPubliclyCheckbox() = publiclyCheckbox()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPubliclySummary() = publiclySummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSharedCheckbox() = sharedCheckbox()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSharedSummary() = sharedSummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

fun setChecked(checked: Boolean): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): BaseMatcher<View?> {
            return object : BaseMatcher<View?>() {
                override fun matches(item: Any?): Boolean {
                    return isA(Checkable::class.java).matches(item)
                }

                override fun describeMismatch(
                    item: Any?,
                    mismatchDescription: Description?
                ) {
                }

                override fun describeTo(description: Description?) {}
            }
        }

        override fun getDescription(): String? {
            return null
        }

        override fun perform(uiController: UiController?, view: View?) {
            val checkableView = view as Checkable
            checkableView.isChecked = checked
        }
    }
}
