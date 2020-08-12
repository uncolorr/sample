package ru.icames.store.presentation.code_reader.config

import ru.icames.store.presentation.code_reader.QrCodeReaderMode
import ru.icames.store.presentation.code_reader.code_handler.CodeHandler
import ru.icames.store.presentation.code_reader.code_handler.PutObjectCodeHandler

class PutObjectQrCodeReaderConfig: BaseQrCodeReaderConfig {
    override val readerMode: QrCodeReaderMode = QrCodeReaderMode.ACCOUNTING

    override fun getCodeHandlerInstance(): CodeHandler {
        return PutObjectCodeHandler()
    }
}