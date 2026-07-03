package ie.equalit.ceno.components.ceno
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import mozilla.components.concept.engine.webextension.WebExtensionRuntime
import mozilla.components.support.base.log.logger.Logger

/**
 * Feature to enable uBlock Origin extension
 */
object UblockOriginWebExt {
    private val logger = Logger("uBlock_Origin")

    internal const val UBLOCK_ORIGIN_EXTENSION_ID = "uBlock0@raymondhill.net"
    internal const val UBLOCK_ORIGIN_EXTENSION_URL = "resource://android/assets/addons/uBlock/"

    fun install(runtime: WebExtensionRuntime) {
        runtime.installBuiltInWebExtension(
            UBLOCK_ORIGIN_EXTENSION_ID, UBLOCK_ORIGIN_EXTENSION_URL,
            onSuccess = {
                logger.debug("Installed uBlock Origin WebExtension: ")
            },
            onError = { throwable ->
                logger.error("Failed to install uBlock Origin WebExtension:", throwable)
            }
        )
    }
}
