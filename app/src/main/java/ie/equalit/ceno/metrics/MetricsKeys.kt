package ie.equalit.ceno.metrics

enum class MetricsKeys (val key:String) {
    NETWORK_COUNTRY("network_country"),
    NETWORK_OPERATOR ("network_operator"),
    NETWORK_TYPE("network_type"),
    NETWORK_VPN_ENABLED("network_vpn_enabled"),

    TEST("test"),

    TIMEZONE("timezone")
}