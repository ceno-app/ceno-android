## Ceno Metrics

By default, Ceno collects anonymized application and network metrics to help understand issues, monitor performance and measure install audience. 
The metrics records are encrypted-at-rest on your device and in-transit to eQualitie's self-hosted metrics endpoint. 
Metrics records are only identified by a randomly generated ID that changes weekly and they are submitted at least once per-hour and are deleted from your device after submission. 
Users can opt-out of metrics collection at any time via the `Settings > Background metrics > Ceno Metrics` toggle.

The mechanism for metrics collection and submission is built-into the Ouinet library that is used by Ceno. 
Some lower-level data points are automatically collected by the Ouinet client whenever metrics is enabled.
Find more about these and the general Ouinet metrics implementation in [rust/record_format.md](https://gitlab.com/equalitie/ouinet/-/blob/main/rust/record_format.md) included with its source code.

Additionally, there are application metrics collected by Ceno and submitted to the Ouinet client via its frontend API. 
The metrics listed below have been implemented in Ceno for Android, with notes of which are implemented in Ceno for Windows as well.

- **APP_VERSION**
    - X.Y.Z string
    - Version of Ceno from which metric was reported (or version of whatever application is reporting Ouinet metrics)
    - Available on Windows.
- **BRIDGE_OPT_IN**
    - true/false
    - Whether the user has enabled bridge mode setting.
- **NETWORK_COUNTRY**
    - two-letter ISO country code
    - Inferred country of device, based on cell network (from `TelephonyManager.networkCountryIso`), falls back to SIM card country (from `TelephonyManager.simCountryIso`), then device language (from `java.util.Locale.getDefault().country`).
    - Available on Windows, from system region setting.
- **NETWORK_OPERATOR**
    - string, e.g. `T-Mobile`
    - Cell provider, from Android `TelephoneyManager.networkOperatorName`
- **NETWORK_TYPE**
    - cellular/wifi/unknown
    - Primary network connection, from Android ConnectivityManager `NetworkCapabilities.TRANSPORT_CELLULAR` and `NetworkCapabilities.TRANSPORT_WIFI`
- **NETWORK_VPN_ENABLED**
    - true/false
    - Whether a VPN is enabled, from Android ConnectivityManager `NetworkCapabilities.TRANSPORT_VPN`
- **TIMEZONE**
    - short code for timezone, e.g. UTC-08:00
    - On Android, from `java.util.TimeZone`, 
    - Available on Windows, from the system settings
      
Find the source code where these are implemented in [NetworkMetrics.kt](./app/src/main/java/ie/equalit/ceno/metrics/NetworkMetrics.kt).
