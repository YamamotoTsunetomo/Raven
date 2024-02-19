package com.tsunetomo.raven.data

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_BLUETOOTH
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import com.tsunetomo.raven.model.NetworkException
import okhttp3.Interceptor
import okhttp3.Response

class NetworkInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (isConnected()) {
            chain.proceed(chain.request())
        } else throw NetworkException()
    }

    private fun isConnected(): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val nw = cm.activeNetwork ?: return false
        val actNw = cm.getNetworkCapabilities(nw) ?: return false

        return actNw.hasTransport(TRANSPORT_WIFI) ||
                actNw.hasTransport(TRANSPORT_CELLULAR) ||
                actNw.hasTransport(TRANSPORT_ETHERNET) ||
                actNw.hasTransport(TRANSPORT_BLUETOOTH)
    }
}