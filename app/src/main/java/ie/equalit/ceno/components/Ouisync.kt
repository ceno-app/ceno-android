package ie.equalit.ceno.components

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.equalitie.ouisync.service.Service
import org.equalitie.ouisync.session.AccessMode
import org.equalitie.ouisync.session.OuisyncException
import org.equalitie.ouisync.session.Repository
import org.equalitie.ouisync.session.Session
import org.equalitie.ouisync.session.ShareToken
import org.equalitie.ouisync.session.create
import java.io.File
import java.nio.charset.Charset

/**
 * Single repo, single file implementation of Ouisync
 */
class Ouisync (
    context: Context
) {
    var service: Service? = null
    var session: Session? = null
    var writeToken: ShareToken? = null
    private var sessionError by mutableStateOf<String?>(null)
    private var protocolVersion: Long by mutableLongStateOf(0)
    private val rootDir: File? = context.filesDir
    private var configDir: String = "$rootDir/config"
    private var storeDir: String = "$rootDir/store"

    /**
     * Starts Ouisync service and creates a new session.
     * Binds to network interface and adds cache server from settings.
     */
    suspend fun createSession() {
        try {
            service = Service.start(configDir)
        } catch (e: OuisyncException.ServiceAlreadyRunning) {
            Log.d(TAG, "Service already running")
        } catch (e: Exception) {
            Log.e(TAG, "Service.start failed", e)
            sessionError = e.toString()
        }

        try {
            session = Session.create(configDir)
            sessionError = null
            session?.let {
                it.bindNetwork(listOf("quic/0.0.0.0:0", "quic/[::]:0"))
                it.addUserProvidedPeers(listOf("quic/51.79.21.142:20209"))
                protocolVersion = it.getCurrentProtocolVersion()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Session.create failed", e)
            sessionError = e.toString()
        } catch (e: java.lang.Error) {
            Log.e(TAG, "Session.create failed", e)
            sessionError = e.toString()
        }
    }

    /**
     * Open repository
     */
    private suspend fun openRepository() : Repository? {
        val session = this.session
        if (session == null)
            return null
        val file = File("$storeDir/$REPO_NAME.$DB_EXTENSION")
        if (file.exists()) {
            Log.i(TAG, "Opening repository $REPO_NAME")
            return session.openRepository(file.path)
        }
        return null
    }

    /**
     * Creates or imports new repo depending on parameters provided
     *
     * @param token Optional, token of repo you to import, if left blank, new repo is created.
     * @param contentW Optional, content to write into the new repo, if left blank, no file is written.
     * @param charset Optional, defaults to UTF_8, charset for the content to be written.
     */
    suspend fun createOrImportRepo(token: String = "", contentW: String = "", charset: Charset = Charsets.UTF_8) {
        val session = this.session
        var repo = openRepository()
        if (repo != null) {
            Log.e(TAG, "repository named \"$REPO_NAME\" already exists")
            return
        }
        if (session == null)
            return
        Log.d(TAG, "creating repository named \"$REPO_NAME\" in $storeDir")
        var shareToken: ShareToken? = null
        if (token.isNotEmpty()) {
            shareToken =  ShareToken(token)
        }

        repo = session.createRepository(
            "$storeDir/$REPO_NAME.$DB_EXTENSION",
            readSecret = null,
            writeSecret = null,
            token = shareToken,
        )

        // Syncing is initially disabled, need to enable it.
        repo.setSyncEnabled(true)

        // Enable DHT and PEX for discovering peers. These settings are persisted so it's not
        // necessary to set them again when opening the repository later.
        repo.setDhtEnabled(true)
        repo.setPexEnabled(true)

        // TODO: implement options for access modes
        writeToken = repo.share(accessMode = AccessMode.WRITE)

        if (contentW == "") {
           return
        }
        val fileW = repo.createFile(FILENAME)
        fileW.write(0, contentW.toByteArray(charset))
        fileW.flush()
        fileW.close()
    }

    /**
     * Open repository and returns contents of profile.txt
     */
    suspend fun openAndReadFromRepo() : String? {
        val repo = openRepository()
        if (repo == null) {
            Log.e(TAG, "repository does not not exist")
            return null
        }
        val fileR = repo.openFile(FILENAME)
        val len = fileR.getLength()
        val contentR = fileR.read(0, len).toString(Charsets.UTF_8)
        fileR.close()
        return contentR
    }

    /**
     * Delete repository
     */
    suspend fun deleteRepository() {
        val session = this.session
        session?.deleteRepositoryByName(REPO_NAME)
    }

    companion object {
        private const val TAG = "OUISYNC"
        private const val DB_EXTENSION = "ouisyncdb"
        private const val REPO_NAME = "cenoProfile"
        private const val FILENAME = "profile.txt"
    }
}