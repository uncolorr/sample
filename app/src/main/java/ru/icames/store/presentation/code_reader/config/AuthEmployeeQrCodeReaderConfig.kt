package ru.icames.store.presentation.code_reader.config

import ru.icames.store.presentation.code_reader.QrCodeReaderMode
import ru.icames.store.presentation.code_reader.code_handler.CodeHandler
import ru.icames.store.presentation.code_reader.code_handler.AuthEmployeeCodeHandler

class AuthEmployeeQrCodeReaderConfig: BaseQrCodeReaderConfig {
    override val readerMode: QrCodeReaderMode = QrCodeReaderMode.AUTH_EMPLOYEE
    override fun getCodeHandlerInstance(): CodeHandler {
        return AuthEmployeeCodeHandler()
    }


}