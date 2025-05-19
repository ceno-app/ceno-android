package ie.equalit.ceno.share

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.compose.material.Snackbar
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.concept.engine.prompt.ShareData
import mozilla.components.concept.sync.TabData
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.share.RecentAppsStorage
import mozilla.components.support.ktx.kotlin.isExtensionUrl
import androidx.core.net.toUri

/**
 * [ShareFragment] controller.
 *
 * Delegated by View Interactors, handles container business logic and operates changes on it.
 */
interface ShareController {
    fun handleShareClosed()
    fun handleShareToApp(app: AppShareOption)
    /**
     * Handles when a save to PDF action was requested.
     */
    fun handleSaveToPDF(tabId: String?)
    /**
     * Handles when a print action was requested.
     */
    fun handlePrint(tabId: String?)

    enum class Result {
        DISMISSED, SHARE_ERROR, SUCCESS
    }
}

/**
 * Default behavior of [ShareController]. Other implementations are possible.
 *
 * @param context [Context] used for various Android interactions.
 * @param shareSubject Desired message subject used when sharing through 3rd party apps, like email clients.
 * @param shareData The list of [ShareData]s that can be shared.
 * @param saveToPdfUseCase Instance of [SessionUseCases.SaveToPdfUseCase] to generate a PDF of a given tab.
 * @param printUseCase Instance of [SessionUseCases.PrintContentUseCase] to print content of a given tab.
 * @param snackbar Instance of [FenixSnackbar] for displaying styled snackbars.
 * @param navController [NavController] used for navigation.
 * @param recentAppsStorage Instance of [RecentAppsStorage] for storing and retrieving the most recent apps.
 * @param viewLifecycleScope [CoroutineScope] used for retrieving the most recent apps in the background.
 * @param dispatcher Dispatcher used to execute suspending functions.
 * @param fxaEntrypoint The entrypoint if we need to authenticate, it will be reported in telemetry.
 * @param dismiss Callback signalling sharing can be closed.
 */
@Suppress("TooManyFunctions", "LongParameterList")
class DefaultShareController (
    private val context: Context,
    private val shareSubject: String?,
    private val shareData: List<ShareData>,
    private val saveToPdfUseCase: SessionUseCases.SaveToPdfUseCase,
    private val printUseCase: SessionUseCases.PrintContentUseCase,
    private val navController: NavController,
    private val recentAppsStorage: RecentAppsStorage,
    private val viewLifecycleScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val dismiss: (ShareController.Result) -> Unit,
) : ShareController {
    override fun handleShareClosed() {
        dismiss(ShareController.Result.DISMISSED)
    }

    override fun handleShareToApp(app: AppShareOption) {
        if (app.packageName == ACTION_COPY_LINK_TO_CLIPBOARD) {
            copyClipboard()
            dismiss(ShareController.Result.SUCCESS)

            return
        }
        viewLifecycleScope.launch(dispatcher) {
            recentAppsStorage.updateRecentApp(app.activityName)
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, getShareText())
            putExtra(Intent.EXTRA_SUBJECT, getShareSubject())
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT + Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            setClassName(app.packageName, app.activityName)
        }

        @Suppress("TooGenericExceptionCaught")
        val result = try {
            context.startActivity(intent)
            ShareController.Result.SUCCESS
        } catch (e: Exception) {
            when (e) {
                is SecurityException, is ActivityNotFoundException -> {
                    //todo
//                    snackbar.setText(context.getString(R.string.share_error_snackbar))
//                    snackbar.setLength(FenixSnackbar.LENGTH_LONG)
//                    snackbar.show()
                    ShareController.Result.SHARE_ERROR
                }
                else -> throw e
            }
        }
        dismiss(result)
    }

    override fun handleSaveToPDF(tabId: String?) {
        handleShareClosed()
        saveToPdfUseCase.invoke(tabId)
    }

    override fun handlePrint(tabId: String?) {
        handleShareClosed()
        printUseCase.invoke(tabId)
    }

    @VisibleForTesting
    fun getShareText() = shareData.joinToString("\n\n") { data ->
        val url = data.url.orEmpty()
        if (url.isExtensionUrl()) {
            // Sharing moz-extension:// URLs is not practical in general, as
            // they will only work on the current device.

            // We solve this for URLs from our reader extension as they contain
            // the original URL as a query parameter. This is a workaround for
            // now and needs a clean fix once we have a reader specific protocol
            // e.g. ext+reader://
            // https://github.com/mozilla-mobile/android-components/issues/2879
            url.toUri().getQueryParameter("url") ?: url
        } else {
            url
        }
    }

    @VisibleForTesting
    internal fun getShareSubject() =
        shareSubject ?: shareData.filterNot { it.title.isNullOrEmpty() }
            .joinToString(", ") { it.title.toString() }

    // Navigation between app fragments uses ShareTab as arguments. SendTabUseCases uses TabData.
    @VisibleForTesting
    internal fun List<ShareData>.toTabData() = map { data ->
        TabData(title = data.title.orEmpty(), url = data.url ?: data.text?.toDataUri().orEmpty())
    }

    private fun String.toDataUri(): String {
        return "data:,${Uri.encode(this)}"
    }

    private fun copyClipboard() {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(getShareSubject(), getShareText())

        clipboardManager.setPrimaryClip(clipData)
        //todo
//        snackbar.setText(context.getString(R.string.toast_copy_link_to_clipboard))
//        snackbar.setLength(FenixSnackbar.LENGTH_SHORT)
//        snackbar.show()
    }

    companion object {
        const val ACTION_COPY_LINK_TO_CLIPBOARD = "org.mozilla.fenix.COPY_LINK_TO_CLIPBOARD"
    }
}
