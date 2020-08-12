package ru.icames.store.util

import android.annotation.SuppressLint
import java.net.NetworkInterface
import java.util.*

object Mac {

    private var macAddress = ""

    @SuppressLint("DefaultLocale")
    fun getMacAddr(): String {
        if(macAddress.isNotEmpty()){
            return macAddress
        }
        try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (nif.name.toLowerCase() != "wlan0") continue
                val macBytes: ByteArray = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                macAddress = res1.toString()
                return macAddress
            }
        } catch (ex: Exception) {
        }
        return ""
    }
}