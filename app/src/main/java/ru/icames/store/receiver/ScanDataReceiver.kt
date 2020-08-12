package ru.icames.store.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.icames.store.application.App

/**
 * Receiver for catching data from Terminal broadcast scan utility
 * Receiver send data to QrCodeReaderActivity for handling scan data
 * This receiver works only on ATOL Smart.Lite
 * */

class ScanDataReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val data = intent.getStringExtra(ScanReceiverSettings.EXTRA_SCAN_DATA_NAME)
        App.log("Scan Broadcast data: $data")
        val i = Intent(ScanReceiverSettings.SCAN_BROADCAST_NAME)
        i.putExtra(ScanReceiverSettings.EXTRA_SCAN_DATA_NAME, data)
        context.sendBroadcast(i)
    }
}
