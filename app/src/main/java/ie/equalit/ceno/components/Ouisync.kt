package ie.equalit.ceno.components

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.equalitie.ouisync.session.AccessMode
import org.equalitie.ouisync.session.Repository
import org.equalitie.ouisync.session.Session
import org.equalitie.ouisync.session.ShareToken
import org.equalitie.ouisync.session.create
import java.io.File
import java.nio.charset.Charset


class Ouisync (
    context : Context
) {
    lateinit var session : Session
    var storeDir : String? = null
    var writeToken : ShareToken? = null
    private var sessionError by mutableStateOf<String?>(null)
    private var protocolVersion: Long by mutableLongStateOf(0)
    private val rootDir : File? = context.filesDir
    private var configDir : String = "$rootDir/config"
    //TODO: allow storeDir to be chosen by user
    private var repositories by mutableStateOf<Map<String, Repository>>(mapOf())

    suspend fun createSession() {
        try {
            session = Session.create(configDir)
            sessionError = null
        } catch (e: Exception) {
            Log.e(TAG, "Session.create failed", e)
            sessionError = e.toString()
        } catch (e: java.lang.Error) {
            Log.e(TAG, "Session.create failed", e)
            sessionError = e.toString()
        }
    }

    suspend fun getProtocolVersion() : Long {
        session.let {
            protocolVersion = it.getCurrentProtocolVersion()
        }
        return protocolVersion
    }

    private suspend fun createOrOpenRepository(name: String, token: String = "") : Repository {
        val session = this.session
        Log.d(TAG, "creating repository named \"$name\" in $storeDir")

        if (repositories.containsKey(name)) {
            Log.e(TAG, "repository named \"$name\" already exists")
            return openRepository(name)
        }

        var shareToken: ShareToken? = null

        if (token.isNotEmpty()) {
            shareToken =  ShareToken(token)
        }

        val repo = session.createRepository(
            "$storeDir/$name.$DB_EXTENSION",
            readSecret = null,
            writeSecret = null,
            token = shareToken,
        )

        writeToken = repo.share(accessMode = AccessMode.WRITE)

        Log.d(TAG, writeToken.toString())

        repositories = repositories + (name to repo)
        return repo
    }

    private suspend fun openRepository(name: String) : Repository {
        val session = this.session
        val file = File("$storeDir/$name.$DB_EXTENSION")
        val repo = session.openRepository(file.path)
        Log.i(TAG, "Opened repository $name")
        return repo
    }

    suspend fun openRepositories() {
        val session = this.session
        val files = File(storeDir!!).listFiles() ?: arrayOf()

        for (file in files) {
            if (file.name.endsWith(".$DB_EXTENSION")) {
                try {
                    val name = file
                        .name
                        .substring(0, file.name.length - DB_EXTENSION.length - 1)
                    val repo = session.openRepository(file.path)

                    Log.i(TAG, "Opened repository $name")

                    repositories = repositories + (name to repo)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open repository at ${file.path}")
                    continue
                }
            }
        }
    }

    suspend fun createAndWriteToRepo(name: String, contentW : String, charset: Charset = Charsets.UTF_8) {
        val repo = createOrOpenRepository(name)
        Log.i(TAG, "Opened repository $name")
        val fileW = repo.createFile("prefs.txt")
        fileW.write(0, contentW.toByteArray(charset))
        fileW.flush()
        fileW.close()
    }

    suspend fun openAndReadFromRepo(name: String) : String {
        Log.d(TAG, "Opening repository $storeDir/$name")
        val session = this.session
        val repo = session.openRepository("$storeDir/$name.$DB_EXTENSION")
        Log.i(TAG, "Opened repository $name")
        val fileR = repo.openFile("prefs.txt")
        val len = fileR.getLength()
        Log.d(TAG, "prefs.txt is $len bytes long")
        val contentR = fileR.read(0, len).toString(Charsets.UTF_8)
        Log.d(TAG, "Got content: $contentR")
        fileR.close()
        return contentR
    }


    companion object {
        private const val TAG = "OUISYNC"
        private val DB_EXTENSION = "ouisyncdb"
    }
}