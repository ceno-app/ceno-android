/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.components

import mozilla.components.feature.intent.processing.TabIntentProcessor
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.tabs.TabsUseCases

/**
 * Component group for miscellaneous components.
 */
class Utilities(
    private val searchUseCases: SearchUseCases,
    private val tabsUseCases: TabsUseCases,
) {
    /**
     * Provides intent processing functionality for ACTION_VIEW and ACTION_SEND intents,
     * along with external intent processors.
     */
    val intentProcessor by lazy {
        TabIntentProcessor(tabsUseCases, searchUseCases.newTabSearch)
    }
}
