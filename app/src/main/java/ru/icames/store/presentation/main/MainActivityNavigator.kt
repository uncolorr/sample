package ru.icames.store.presentation.main

import android.content.Context
import ru.icames.store.presentation.code_reader.QrCodeReaderActivity
import ru.icames.store.presentation.code_reader.AccountingMode
import ru.icames.store.presentation.code_reader.config.PutObjectQrCodeReaderConfig
import ru.icames.store.presentation.code_reader.config.TakeObjectQrCodeReaderConfig

class MainActivityNavigator constructor(private val context: Context) {

    fun openPutObject() {
        val config = PutObjectQrCodeReaderConfig()
        context.startActivity(QrCodeReaderActivity.getInstance(context, config))
    }

    fun openTakeObject(){
        val config = TakeObjectQrCodeReaderConfig()
        context.startActivity(QrCodeReaderActivity.getInstance(context, config))
    }

}