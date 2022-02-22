package org.mozilla.reference.browser.ui

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.ui.robots.downloadRobot
import org.mozilla.reference.browser.ui.robots.navigationToolbar
import org.mozilla.reference.browser.ui.robots.notificationShade

class DownloadTest {

    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val activityTestRule = BrowserActivityTestRule()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun cancelFileDownloadTest() {
        val downloadPage = TestAssetHelper.getDownloadAsset(mockWebServer)
        val downloadFileName = "web_icon.png"

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(downloadPage.url) {}

        downloadRobot {
            clickCancelDownloadButton()
        }

        notificationShade {
            verifyDownloadNotificationDoesNotExist("Download completed", downloadFileName)
        }.closeNotification {}
    }

    @Test
    fun fileDownloadTest() {
        val downloadPage = TestAssetHelper.getDownloadAsset(mockWebServer)
        val downloadFileName = "web_icon.png"

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(downloadPage.url) {}

        downloadRobot {
            clickDownloadButton()
        }

        notificationShade {
            verifyDownloadNotificationExist("Download completed", downloadFileName)
        }.closeNotification {}
    }
}
