/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.components.ceno.appstate

import ie.equalit.ceno.components.ceno.AppStore

/** CENO: Ported from Fenix, significantly stripped down
 *  since TopSites is the only currently supported AppState
 *
 * Reducer for [AppStore].
 */
internal object AppStoreReducer {
    fun reduce(state: AppState, action: AppAction): AppState = when (action) {

        is AppAction.Change -> state.copy(
            topSites = action.topSites
        )

        is AppAction.TopSitesChange -> state.copy(topSites = action.topSites)

        is AppAction.ModeChange -> state.copy(mode = action.mode)

        is AppAction.OuinetStatusChange -> state.copy(ouinetStatus = action.status)

        is AppAction.BridgeCardChange -> state.copy(showBridgeCard = action.showCard)

        is AppAction.SourceCountsChange -> state.copy(
            sourceCounts = action.sourceCounts
        )
        is AppAction.OuicrawlSitesChange -> state.copy(
            ouicrawlListIsHidden = action.listIsHidden
        )
    }
}
