package ie.equalit.ceno.components

import android.content.Context
import ie.equalit.ceno.BuildConfig
import ie.equalit.ceno.EngineProvider
import ie.equalit.ceno.R
import ie.equalit.ceno.components.ceno.CenoLocationUtils
import ie.equalit.ceno.ext.application
import ie.equalit.ceno.ext.assetToString
import ie.equalit.ceno.settings.CenoSettings
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.tabs.FailedToRetrieveResource
import ie.equalit.ouinet.Config
import ie.equalit.ouinet.OuinetBackground
import mozilla.components.support.base.log.logger.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.SecureRandom

// Extension functions for Ouinet 1.6 backwards compatibility
import ie.equalit.ceno.ext.ouinet.*

class Ouinet(
    private val context: Context
) {

    lateinit var config: Config

    lateinit var background: OuinetBackground

    val metricsFrontendToken = generateRandomToken()
    val proxyAccessPassword = generateRandomToken()

    fun setConfig() {
        val dnsProtocols = if (!isDohDisabledForLocale())
            Settings.getDnsProtocols(context)
        else
            mutableSetOf("plain")
        writeErrorPageToFile(
            FailedToRetrieveResource.createErrorPage(context),
            context
        )
        val cacheType = CenoSettings.getCacheType(context)
        val injectorTlsCert = BuildConfig.INJECTOR_TLS_CERT.assetToString(context)
        val metricsPubKey =
            "-----BEGIN PUBLIC KEY-----\n" + BuildConfig.METRICS_PUB_KEY + "\n-----END PUBLIC KEY-----"
        val configBuilder = Config.ConfigBuilder(context)
            .setCacheHttpPubKey(BuildConfig.CACHE_PUB_KEY)
            .setInjectorTlsCert(injectorTlsCert)
            .setTlsCaCertStoreDir(context.resources.getString(R.string.cacert_file_path))
            .setCacheType(cacheType)
            .setBtBootstrapExtras(getBtBootstrapExtras())
            .setListenOnTcp(context.getString(R.string.loopback_ip) + ":" + BuildConfig.PROXY_PORT)
            .setFrontEndEp(context.getString(R.string.loopback_ip) + ":" + BuildConfig.FRONTEND_PORT)
            .setErrorPagePath(getErrorPagePath())
            .setDisableBridgeAnnouncement(!CenoSettings.isBridgeAnnouncementEnabled(context))
            .setMetricsEnableOnStart(Settings.isOuinetMetricsEnabled(context))
            .setMetricsEncryptionKey(metricsPubKey)
            .setMetricsDeleteAfter(BuildConfig.METRICS_DELETE_AFTER.toString())
            .setFrontEndAccessToken(metricsFrontendToken)
            .setClientCredentials("$PROXY_ACCESS_USER:$proxyAccessPassword")
            .setDnsProtocols(dnsProtocols)
            .setEnableLogFile(CenoSettings.isCenoLogEnabled(context))
            .setLogLevel(Config.LogLevel.valueOf(Settings.getLogLevel(context)))
            .setDropSavedOpts(true)

        BuildConfig.METRICS_SERVERS.forEach { server ->
            configBuilder.addMetricsServer(
                server[0],
                server[1],
                server[2].assetToString(context),
                server[3].toInt(),
                )
        }

        if (BuildConfig.INJECTOR_CREDENTIALS != "") {
            configBuilder.setInjectorCredentials(BuildConfig.INJECTOR_CREDENTIALS)
        }

        if (BuildConfig.INJECTOR_EP != "") {
            configBuilder.setInjectorEp(BuildConfig.INJECTOR_EP)
        }

        if (cacheType == "ouisync") {
            configBuilder.setOuisyncPageIndex(BuildConfig.OUISYNC_PAGE_INDEX)
        }

        if (cacheType == "bep3-http-over-i2p") {
            configBuilder.setI2pBep3Tracker(BuildConfig.I2P_BEP3_TRACKER)
                .setEnableI2pServiceLib(true)
                .addRequestField("X-Ouinet-Route: PublicInjector Bep3HTTPOverI2P")
                .setDisableOriginAccess(true)
        }
        config = configBuilder.build()
    }

    fun setBackground(ctx: Context) {
        background = OuinetBackground.Builder(ctx)
            .setOuinetConfig(config)
            .build()
    }

    fun updateEndpoints() {
        background.getProxyEndpoint()
            ?.also {
                EngineProvider.getOrCreateRuntime(context).settings.setProxyConfig(
                    "manual",
                    it.toString(),
                    it.toString(),
                )
                CenoSettings.setProxyEndpoint(context, it.toString())
            } ?: Logger.error("Failed to set proxyEndpoint in CenoSettings")
        background.getFrontendEndpoint()
            ?.also {
                CenoSettings.setFrontendEndpoint(context, it.toString())
            } ?: Logger.error("Failed to set frontendEndpoint in CenoSettings")
    }

    fun isDohDisabledForLocale(): Boolean {
        var countryIsoCode: String
        val locationUtils = CenoLocationUtils(context.application)
        countryIsoCode = locationUtils.currentCountry
        Logger.debug("Got country code: $countryIsoCode")
        if (countryIsoCode.isNotEmpty()) {
            // Country code found, check if DoH should be disabled for this country
            for (entry in BuildConfig.DOH_DISABLED_LOCALES) {
                Logger.debug("Entry country code: $entry")
                if (countryIsoCode == entry) {
                    return true
                }
            }
        }
        return false
    }

    private fun getBtBootstrapExtras(): Set<String>? {
        var countryIsoCode: String
        val locationUtils = CenoLocationUtils(context.application)
        countryIsoCode = locationUtils.currentCountry

        // Attempt getting country-specific `BT_BOOTSTRAP_EXTRAS` entry from BuildConfig,
        // fall back to empty BT bootstrap extras otherwise.
        var btbsxsStr = ""
        if (countryIsoCode.isNotEmpty()) {
            // Country code found, try getting bootstrap extras resource for this country
            for (entry in BuildConfig.BT_BOOTSTRAP_EXTRAS) {
                if (countryIsoCode == entry[0].split("_")[0]) {
                    btbsxsStr += entry[1]
                    btbsxsStr += " "
                }
            }
        }

        if (btbsxsStr != "") {
            // Bootstrap extras resource found
            val btbsxs: HashSet<String> = HashSet()
            for (x in btbsxsStr.split(" ")
                .toTypedArray()) {
                if (x.isNotEmpty()) {
                    btbsxs.add(x)
                }
            }
            if (btbsxs.isNotEmpty()) {
                Logger.debug("Extra BT bootstraps: $btbsxs")
                return btbsxs
            }
        }
        // else no bootstrap extras included, leave null
        Logger.debug("No extra BT bootstraps required")
        return null
    }

    private fun getErrorPagePath(): String {
        return "file://${File(context.filesDir, ERROR_PAGE).absolutePath}"
    }

    private fun writeErrorPageToFile(fileContent: String, context: Context) {
        val file = File(context.filesDir, ERROR_PAGE)
        try {
            val outputStream = FileOutputStream(file)
            outputStream.write(fileContent.toByteArray())
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun generateRandomToken(): String {
        return buildString {
            SecureRandom().ints(0, CHAR_POOL.size)
                .limit(TOKEN_LENGTH)
                .forEach {
                    append(CHAR_POOL[it])
                }
        }
    }

    companion object {
        private val CHAR_POOL = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        const val PROXY_ACCESS_USER = "user"
        private const val TOKEN_LENGTH: Long = 27
        private const val ERROR_PAGE = "server500.html"
    }
}
