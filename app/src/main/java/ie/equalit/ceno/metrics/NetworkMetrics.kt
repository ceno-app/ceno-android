package ie.equalit.ceno.metrics

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import ie.equalit.ceno.components.ceno.CenoLocationUtils
import ie.equalit.ceno.ext.application
import ie.equalit.ceno.settings.CenoSettings
import ie.equalit.ceno.settings.OuinetKey
import ie.equalit.ceno.settings.OuinetResponseListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.TimeZone

class NetworkMetrics(
    val context:Context,
    val lifecycleScope: CoroutineScope
) {
    private val metricsRecordId:Flow<String> = flow {
        var previousRecordid = ""
        while(true) {
            CenoSettings.ouinetClientRequest(context, OuinetKey.API_STATUS, forMetrics = true)
            if (CenoSettings.currentMetricsRecordId != previousRecordid)  {
                emit(CenoSettings.currentMetricsRecordId)
                previousRecordid = CenoSettings.currentMetricsRecordId
            }
            if (isUsingVPN() != vpnEnabled)
                emit(CenoSettings.currentMetricsRecordId)
            delay(RECORD_ID_REFRESH_INTERVAL)
        }
    }

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var vpnEnabled:Boolean

    init {
        vpnEnabled = isUsingVPN() == true
    }

    fun collectNetworkMetrics() {
        lifecycleScope.launch {
            metricsRecordId.collect { recordId ->
                //send metrics
                //network country
                addMetricToRecord(recordId, MetricsKeys.NETWORK_COUNTRY, CenoLocationUtils(context.application).currentCountry)
                //network operator
                addMetricToRecord(recordId, MetricsKeys.NETWORK_OPERATOR, getNetworkOperator())
                //network type
                addMetricToRecord(recordId, MetricsKeys.NETWORK_TYPE, getNetworkType())
                //vpn usage
                addMetricToRecord(recordId, MetricsKeys.NETWORK_VPN_ENABLED, isUsingVPN().toString())
                //timezone
                addMetricToRecord(recordId, MetricsKeys.TIMEZONE, getTimeZone())
            }
        }
    }

    private fun addMetricToRecord(recordId:String, key : MetricsKeys, value:String) {
        CenoSettings.ouinetClientRequest(
            context,
            OuinetKey.ADD_METRICS,
            stringValue = value,
            ouinetResponseListener = object : OuinetResponseListener {
                override fun onSuccess(message: String, data: Any?) {
                    Log.d(TAG, "Successfully set metrics for record: $recordId")
                }

                override fun onError() {
                    Log.e(TAG, "Failed to set metrics for record: $recordId")
                }
            },
            forMetrics = true,
            metricsKey = key.name
        )
    }

    private fun getNetworkOperator(): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.networkOperatorName
    }

    private fun getNetworkType(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI) == true)
                return "wifi"
            if (connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR) == true)
                return "cellular"
        } else {
            if(connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI)
                return "wifi"
            if(connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE)
                return "cellular"
        }
        return "unknown"
    }

    private fun isUsingVPN() : Boolean? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        }  else {
            val networks: Array<Network> = connectivityManager.allNetworks
            for (n in networks) {
                if (connectivityManager.getNetworkCapabilities(n)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true)
                    return true
            }
        }
        return false
    }

    private fun getTimeZone():String {
        return TimeZone.getDefault().getDisplayName(TimeZone.getDefault().inDaylightTime(Date()), TimeZone.SHORT)
    }

    companion object {
        const val TAG = "Ceno-Metrics"
        const val RECORD_ID_REFRESH_INTERVAL = 15000L
    }
}