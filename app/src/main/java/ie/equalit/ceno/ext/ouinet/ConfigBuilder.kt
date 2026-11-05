package ie.equalit.ceno.ext.ouinet

import ie.equalit.ouinet.Config

/**
 * Temporary helper functions to make Ceno backwards compatible with Ouinet v1.6.x
 */

fun Config.ConfigBuilder.setTlsCaCertStoreDir(path: String): Config.ConfigBuilder {
    return this.setTlsCaCertStorePath(path)
}

fun Config.ConfigBuilder.addMetricsServer(
    url: String,
    token: String,
    caCert: String?,
    priority: Int?
): Config.ConfigBuilder {
    return this.setMetricsServerUrl(url)
        .setMetricsServerToken(token)
        .setMetricsServerTlsCaCert(caCert)
}

fun Config.ConfigBuilder.setDropSavedOpts(p0: Boolean): Config.ConfigBuilder {
    return this
}

fun Config.ConfigBuilder.setInjectorEp(p0: String): Config.ConfigBuilder {
    return this
}

fun Config.ConfigBuilder.setOuisyncPageIndex(p0: String): Config.ConfigBuilder {
    return this
}

fun Config.ConfigBuilder.setI2pBep3Tracker(p0: String): Config.ConfigBuilder {
    return this
}

fun Config.ConfigBuilder.setEnableI2pServiceLib(p0: Boolean): Config.ConfigBuilder {
    return this
}

fun Config.ConfigBuilder.addRequestField(p0: String): Config.ConfigBuilder {
    return this
}
