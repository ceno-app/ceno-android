package ie.equalit.ceno.share

import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.Store

/**
 * [BrowserAction] middleware reacting in response to Save to PDF related actions.
 */
class SaveToPDFMiddleware : Middleware<BrowserState, BrowserAction> {

    override fun invoke(
        store: Store<BrowserState, BrowserAction>,
        next: (BrowserAction) -> Unit,
        action: BrowserAction,
    ) {
        when (action) {
            is EngineAction.SaveToPdfAction -> {
                next(action)
            }

            is EngineAction.SaveToPdfCompleteAction -> {
                // TODO: not yet implemented
            }

            is EngineAction.SaveToPdfExceptionAction -> {
                // TODO: not yet implemented
            }

            is EngineAction.PrintContentAction -> {
                next(action)
            }

            is EngineAction.PrintContentCompletedAction -> {
                // TODO: not yet implemented
            }

            is EngineAction.PrintContentExceptionAction -> {
                // TODO: not yet implemented
            }

            else -> {
                next(action)
            }
        }
    }
}
