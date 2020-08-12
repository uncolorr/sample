package ru.icames.store.presentation.code_reader.code_handler

import ru.icames.terminal.domain.model.Step
import ru.icames.store.presentation.code_reader.AccountingMode
import presentation.code_reader.Entity
import ru.icames.store.domain.model.Code
import java.io.Serializable

interface CodeHandler :Serializable {

    var isInfiniteReading: Boolean

    var listener: CodeHandlerListener?

    val baseParams: HashMap<String, String>
    val contextParams: HashMap<String, String>

    /**
     * Check value valid format and send callback with result
     * @param value value inside bar code
     * */
    fun handleValue(value: String)


    /**/
    fun endWork()

    /**
     * Prepare data before send it on server.
     * Set needed data to baseParams and contextParams
     * @see baseParams
     * @see contextParams
     * */
    fun prepare()

    /**
     * Split values with delimiter
     * @return list of separated data
     * */
    fun split(value: String): List<String> {
        return value.split(",")
    }

    fun isValueAlreadyScanned(value: String, objectItems: List<String?>): Boolean{
        return objectItems.contains(value)
    }



    /**
     * Check is type of scanning data is supports
     * @param entity type of data. Usually it is first value in separated data list
     * @see split
     * @param accountingMode mode of accounting data
     * @see ru.icames.store.presentation.code_reader.AccountingMode
     * @return result of check
     * */
    fun checkEntityAccepted(entity: String, accountingMode: AccountingMode): Boolean {
        return Entity.contains(entity, accountingMode)
    }

    /**
     * Interface for handling scan processes
     * @see ru.icames.store.presentation.code_reader.QrCodeReaderActivity
     * */

    interface CodeHandlerListener {
        /**
         * Callback that data format is valid add it to list
         * @param data readed data from barcode
         * */
       // fun onDataReaded(type: String, data: String)

        /**
         * Callback that data format is valid add it to list
         * @param data readed data from barcode
         * */
        fun onDataReaded(code: Code)

        /**
         * Callback that switch next step of scanning
         * @param step next step of scanning data
         * @see ru.icames.terminal.domain.model.Step
         * */
        fun onNextStep(step: Step)

        /**
         * Send callback that show Error
         * @param message error message
         * */
        fun onError(message: String)

        /**
         * Send callback that all data is ready to send on server
         * */
        fun onAllDataCollected()

        /**
         * Send callback when steps ended
         */
        fun onStepsFinished()

    }

}