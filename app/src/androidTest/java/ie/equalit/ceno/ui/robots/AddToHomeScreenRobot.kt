/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.ui.robots

import android.os.Build
import androidx.test.uiautomator.UiSelector
import ie.equalit.ceno.helpers.TestAssetHelper.waitingTime

/**
 * Implementation of Robot Pattern for the Add to homescreen feature.
 */
class AddToHomeScreenRobot {

    fun clickCancelAddToHomeScreenButton() {
        cancelAddToHomeScreenButton().waitForExists(waitingTime)
        cancelAddToHomeScreenButton().click()
    }

    fun clickAddAutomaticallyToHomeScreenButton() {
        addAutomaticallyToHomeScreenButton().waitForExists(waitingTime)
        addAutomaticallyToHomeScreenButton().click()
    }

    class Transition {
        fun openHomeScreenShortcut(
            title: String,
            interact: BrowserRobot.() -> Unit
        ): BrowserRobot.Transition {
            mDevice.findObject(UiSelector().textContains(title))
                .waitForExists(waitingTime)
            mDevice.findObject((UiSelector().textContains(title)))
                .clickAndWaitForNewWindow(waitingTime)

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }

    private fun cancelAddToHomeScreenButton() = mDevice.findObject(
        UiSelector().text("Cancel")
    )

    private fun addAutomaticallyToHomeScreenButton() = mDevice.findObject(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            UiSelector().text("Add to home screen")
        } else {
            UiSelector().textContains("Add")
                .clickable(true)
        }
    )
}
