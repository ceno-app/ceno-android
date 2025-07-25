package ie.equalit.ceno.bookmarks

import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.lib.state.Action
import mozilla.components.lib.state.State
import mozilla.components.lib.state.Store

class BookmarkFragmentStore(
    initialState: BookmarkFragmentState,
) : Store<BookmarkFragmentState, BookmarkFragmentAction>(
    initialState,
    ::bookmarkFragmentStateReducer,
)

/**
 * The complete state of the bookmarks tree and multi-selection mode
 * @property tree The current tree of bookmarks, if one is loaded
 * @property mode The current bookmark multi-selection mode
 * @property guidBackstack A set of guids for bookmark nodes we have visited. Used to traverse back
 *                  up the tree after a sync.
 * @property isLoading true if bookmarks are still being loaded from disk
 */
data class BookmarkFragmentState(
    val tree: BookmarkNode?,
    val mode: Mode = Mode.Normal(),
    val guidBackstack: List<String> = emptyList(),
    val isLoading: Boolean = true,
) : State {
    sealed class Mode : SelectionHolder<BookmarkNode> {
        override val selectedItems = emptySet<BookmarkNode>()

        data class Normal(val showMenu: Boolean = true) : Mode()
        data class Selecting(override val selectedItems: Set<BookmarkNode>) : Mode()
    }
}
/**
 * Actions to dispatch through the `BookmarkStore` to modify `BookmarkState` through the reducer.
 */
sealed class BookmarkFragmentAction : Action {
    data class Change(val tree: BookmarkNode) : BookmarkFragmentAction()
    data class Select(val item: BookmarkNode) : BookmarkFragmentAction()
    data class Deselect(val item: BookmarkNode) : BookmarkFragmentAction()
    object DeselectAll : BookmarkFragmentAction()
}

/**
 * Reduces the bookmarks state from the current state and an action performed on it.
 * @param state the current bookmarks state
 * @param action the action to perform
 * @return the new bookmarks state
 */
private fun bookmarkFragmentStateReducer(
    state: BookmarkFragmentState,
    action: BookmarkFragmentAction
):BookmarkFragmentState {
    return when (action) {
        is BookmarkFragmentAction.Change -> {
            // If we change to a node we have already visited, we pop the backstack until the node
            // is the last item. If we haven't visited the node yet, we just add it to the end of the
            // backstack
            val backstack = state.guidBackstack.takeWhile { guid ->
                guid != action.tree.guid
            } + action.tree.guid

            val items = state.mode.selectedItems.filter { it in action.tree }
            val mode = when {
                items.isEmpty() -> {
                    BookmarkFragmentState.Mode.Normal(shouldShowMenu(action.tree.guid))
                }
                else -> BookmarkFragmentState.Mode.Selecting(items.toSet())
            }
            state.copy(
                tree = action.tree,
                mode = mode,
                guidBackstack = backstack,
                isLoading = false,
            )
        }
        is BookmarkFragmentAction.Deselect -> {
            val items = state.mode.selectedItems - action.item
            val mode = if (items.isEmpty()) {
                BookmarkFragmentState.Mode.Normal()
            } else {
                BookmarkFragmentState.Mode.Selecting(items)
            }
            state.copy(
                mode = mode,
            )
        }
        BookmarkFragmentAction.DeselectAll -> state.copy(
            mode = BookmarkFragmentState.Mode.Normal()
        )
        is BookmarkFragmentAction.Select -> state.copy(
            mode = BookmarkFragmentState.Mode.Selecting(state.mode.selectedItems + action.item),
        )
    }
}


private fun shouldShowMenu(currentGuid: String?): Boolean =
    BookmarkRoot.Root.id != currentGuid

operator fun BookmarkNode.contains(item: BookmarkNode): Boolean {
    return children?.contains(item) ?: false
}

/**
 * Contains the selection of items added or removed using the [SelectionInteractor].
 */
interface SelectionHolder<T> {
    val selectedItems: Set<T>
}
