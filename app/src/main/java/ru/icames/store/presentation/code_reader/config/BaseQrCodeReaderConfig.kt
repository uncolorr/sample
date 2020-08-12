package ru.icames.store.presentation.code_reader.config

import ru.icames.store.presentation.code_reader.QrCodeReaderMode
import ru.icames.store.presentation.code_reader.code_handler.CodeHandler
import java.io.Serializable

/**
 * Interface for set config for QrCodeReaderActivity
 * */

interface BaseQrCodeReaderConfig: Serializable {
    val readerMode: QrCodeReaderMode
    val canDeleteItems: Boolean
        get() = false
    fun getCodeHandlerInstance(): CodeHandler
}