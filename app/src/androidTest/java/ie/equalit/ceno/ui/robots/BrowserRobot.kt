/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.allOf
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.Constants.LONG_CLICK_DURATION
import ie.equalit.ceno.helpers.TestAssetHelper.waitingTime
import ie.equalit.ceno.helpers.TestHelper.packageName
import ie.equalit.ceno.helpers.TestHelper.waitForObjects
import ie.equalit.ceno.helpers.click

/**
 * Implementation of Robot Pattern for browser action.
 */
class BrowserRobot {
    /* Asserts that the text within DOM element with ID="testContent" has the given text, i.e.
    * document.querySelector('#testContent').innerText == expectedText
    */
    fun verifyPageContent(expectedText: String) {
        mDevice.waitForObjects(mDevice.findObject(UiSelector().resourceId("android.webkit.WebView")))
        assertTrue(
            mDevice.findObject(
                UiSelector()
                    .textContains(expectedText),
            ).waitForExists(waitingTime),
        )
    }

    fun verifyPageLoaded() {
        mDevice.waitForObjects(mDevice.findObject(UiSelector().resourceId("android.webkit.WebView")))
        assertTrue(
            mDevice.findObject(
                UiSelector().resourceId("$packageName:id/mozac_browser_toolbar_progress")
            ).waitUntilGone(waitingTime),
        )
    }

    fun verifyGithubUrl() {
        verifyUrl("https://github.com/login")
    }

    fun verifyUrl(expectedUrl: String) {
        mDevice.findObject(
            UiSelector()
                .resourceId("$packageName:id/toolbar"),
        )
            .waitForExists(waitingTime)
        mDevice.findObject(
            UiSelector()
                .resourceId("$packageName:id/mozac_browser_toolbar_url_view"),
        )
            .waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains(expectedUrl))
            .waitForExists(waitingTime)
        onView(
            allOf(
                withSubstring(expectedUrl),
                withId(R.id.mozac_browser_toolbar_url_view),
                isDescendantOfA(withId(R.id.mozac_browser_toolbar_origin_view)),
            ),
        ).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    fun longClickMatchingText(expectedText: String) {
        mDevice.waitForWindowUpdate(packageName, waitingTime)
        mDevice.findObject(UiSelector().resourceId("$packageName:id/engineView"))
            .waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains(expectedText)).waitForExists(waitingTime)
        val link = mDevice.findObject(By.textContains(expectedText))
        link.click(LONG_CLICK_DURATION)
    }

    fun longClickAndCopyText(expectedText: String, selectAll: Boolean = false) {
        try {
            // Long click desired text
            mDevice.waitForWindowUpdate(packageName, waitingTime)
            mDevice.findObject(UiSelector().resourceId("$packageName:id/engineView"))
                .waitForExists(waitingTime)
            mDevice.findObject(UiSelector().textContains(expectedText)).waitForExists(waitingTime)
            val link = mDevice.findObject(By.textContains(expectedText))
            link.click(LONG_CLICK_DURATION)

            // Click Select all from the text selection toolbar
            if (selectAll) {
                mDevice.findObject(UiSelector().textContains("Select all")).waitForExists(waitingTime)
                val selectAllText = mDevice.findObject(By.textContains("Select all"))
                selectAllText.click()
            }

            // Click Copy from the text selection toolbar
            mDevice.findObject(UiSelector().textContains("Copy")).waitForExists(waitingTime)
            val copyText = mDevice.findObject(By.textContains("Copy"))
            copyText.click()
        } catch (e: NullPointerException) {
            println("Failed to long click desired text: ${e.localizedMessage}")

            // Refresh the page in case the first long click didn't succeed
            navigationToolbar {
            }.openThreeDotMenu {
            }.refreshPage {
                mDevice.waitForIdle()
            }

            // Long click again the desired text
            mDevice.waitForWindowUpdate(packageName, waitingTime)
            mDevice.findObject(UiSelector().resourceId("$packageName:id/engineView"))
                .waitForExists(waitingTime)
            mDevice.findObject(UiSelector().textContains(expectedText)).waitForExists(waitingTime)
            val link = mDevice.findObject(By.textContains(expectedText))
            link.click(LONG_CLICK_DURATION)

            // Click again Select all from the text selection toolbar
            if (selectAll) {
                mDevice.findObject(UiSelector().textContains("Select all")).waitForExists(waitingTime)
                val selectAllText = mDevice.findObject(By.textContains("Select all"))
                selectAllText.click()
            }

            // Click again Copy from the text selection toolbar
            mDevice.findObject(UiSelector().textContains("Copy")).waitForExists(waitingTime)
            val copyText = mDevice.findObject(By.textContains("Copy"))
            copyText.click()
        }
    }

    fun verifyLinkContextMenuItems() {
        mDevice.waitForWindowUpdate(packageName, waitingTime)
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
            .waitForExists(waitingTime)
        assertTrue(
            mDevice.findObject(UiSelector().resourceId("$packageName:id/titleView"))
                .waitForExists(waitingTime),
        )
        assertContextMenuOpenInNewTab()
        assertContextMenuOpenInNewPrivateTab()
        assertContextMenuCopyLink()
        assertContextMenuShareLink()
    }

    fun verifyNoControlsVideoContextMenuItems() {
        mDevice.waitForWindowUpdate(packageName, waitingTime)
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
            .waitForExists(waitingTime)
        assertTrue(
            mDevice.findObject(UiSelector().resourceId("$packageName:id/titleView"))
                .waitForExists(waitingTime),
        )
        assertContextMenuCopyLink()
        assertContextMenuShareLink()
        assertContextMenuSaveFileToDevice()
    }

    fun clickContextOpenLinkInNewTab() {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
            .waitForExists(waitingTime)
        mDevice.waitForIdle()
        assertContextMenuOpenInNewTab()
        contextMenuOpenInNewTab().click()
    }

    fun clickContextOpenLinkInPrivateTab() {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
            .waitForExists(waitingTime)
        mDevice.waitForIdle()
        assertContextMenuOpenInNewPrivateTab()
        contextMenuOpenInNewPrivateTab().click()
    }

    fun clickContextCopyLink() {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
            .waitForExists(waitingTime)
        mDevice.waitForIdle()
        assertContextMenuCopyLink()
        contextMenuCopyLink().click()
    }

    fun waitUntilCopyLinkSnackbarIsGone() =
        mDevice.findObject(
            UiSelector()
                .resourceId("$packageName:id/snackbar_text")
                .textContains("Link copied to clipboard"),
        ).waitUntilGone(waitingTime)

    fun verifyMediaPlayerControlButtonState(state: String) {
        mDevice.findObject(UiSelector().textContains("Audio_Test_Page")).waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains("audio player")).waitForExists(waitingTime)
        assertTrue(mediaPlayerPlayButton(state).waitForExists(waitingTime))
    }

    fun clickMediaPlayerControlButton(state: String) {
        mediaPlayerPlayButton(state).waitForExists(waitingTime)
        mediaPlayerPlayButton(state).click()
        mDevice.waitForIdle()
    }

    fun clickOpenInAppPromptButton() =
        mDevice.findObject(
            UiSelector()
                .resourceId("android:id/button1")
                .textContains("OPEN"),
        ).also {
            it.waitForExists(waitingTime)
            it.click()
        }

    fun clickSnackbarSwitchButton() =
        mDevice.findObject(
            UiSelector()
                .resourceId("$packageName:id/snackbar_action")
                .textContains("SWITCH"),
        ).also {
            it.waitForExists(waitingTime)
            it.click()
        }

    class Transition {
        private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun checkExternalApps(interact: ExternalAppsRobot.() -> Unit): ExternalAppsRobot.Transition {
            mDevice.waitForWindowUpdate(packageName, waitingTime)
            ExternalAppsRobot().interact()
            return ExternalAppsRobot.Transition()
        }

        fun clickContextShareLink(interact: ContentPanelRobot.() -> Unit): ContentPanelRobot.Transition {
            mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
                .waitForExists(waitingTime)

            mDevice.waitForIdle()
            assertContextMenuShareLink()
            contextMenuShareLink().click()

            ContentPanelRobot().interact()
            return ContentPanelRobot.Transition()
        }

        fun goBack(interact: BrowserRobot.() -> Unit): Transition {
            mDevice.pressBack()
            mDevice.findObject(
                UiSelector()
                    .resourceId("$packageName:id/mozac_browser_toolbar_progress"),
            ).waitUntilGone(waitingTime)
            BrowserRobot().interact()
            return Transition()
        }
    }
}

fun browser(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
    BrowserRobot().interact()
    return BrowserRobot.Transition()
}

private fun mediaPlayerPlayButton(state: String) =
    mDevice.findObject(
        UiSelector()
            .className("android.widget.Button")
            .text(state),
    )

private fun contextMenuOpenInNewTab() = onView(withText(R.string.mozac_feature_contextmenu_open_link_in_new_tab))
private fun contextMenuOpenInNewPrivateTab() = onView(withText(R.string.mozac_feature_contextmenu_open_link_in_private_tab))
private fun contextMenuCopyLink() = onView(withText(R.string.mozac_feature_contextmenu_copy_link))
private fun contextMenuShareLink() = onView(withText(R.string.mozac_feature_contextmenu_share_link))
private fun contextMenuSaveFileToDevice() = onView(withText(R.string.mozac_feature_contextmenu_save_file_to_device))

private fun assertContextMenuOpenInNewTab() = contextMenuOpenInNewTab()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertContextMenuOpenInNewPrivateTab() = contextMenuOpenInNewPrivateTab()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertContextMenuCopyLink() = contextMenuCopyLink()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertContextMenuShareLink() = contextMenuShareLink()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertContextMenuSaveFileToDevice() = contextMenuSaveFileToDevice()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
