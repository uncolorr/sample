package ru.icames.store.presentation.code_reader.code_handler

import presentation.code_reader.Entity
import ru.icames.store.presentation.code_reader.AccountingMode
import ru.icames.store.application.AppSettings
import ru.icames.store.domain.model.Code
import ru.icames.store.util.Fields
import ru.icames.store.util.Fields.PackingTasks.MAC
import ru.icames.store.util.Fields.PackingTasks.OBJECT
import ru.icames.store.util.Fields.PackingTasks.OPERATOR
import ru.icames.store.util.Fields.PackingTasks.PROCESS_NAME
import ru.icames.store.util.Fields.PackingTasks.PROCESS_TOKEN
import ru.icames.store.util.Mac
import ru.icames.store.util.ProcessToken

class AuthEmployeeCodeHandler : CodeHandler {


    override val baseParams: HashMap<String, String> = hashMapOf()
    override val contextParams: HashMap<String, String> = hashMapOf()

    override var isInfiniteReading: Boolean = false

    override var listener: CodeHandler.CodeHandlerListener? = null

    private var currentUserData = ""

    override fun handleValue(value: String) {
        if(value.isEmpty()){
            return
        }
        val items = split(value)
        if(items.isEmpty()){
            return
        }
        if (listener == null) {
            return
        }
        val isEntityAccepted = checkEntityAccepted(items[0], AccountingMode.AUTH)
        if(!isEntityAccepted){
            listener!!.onError("Неверный формат данных: неверное ключевое слово")
            return
        }

        val entity = Entity.find(items[0]) ?: return

        val isValueCountAccepted = items.size >= entity.valuesCount

        if (!isValueCountAccepted) {
            listener!!.onError("Неверный формат данных: неверное количество cвойств объекта")
            return
        }

        currentUserData = value
        //listener!!.onDataReaded(entity.description, value)

        listener!!.onDataReaded(Code(entity.description, value))
        listener!!.onAllDataCollected()
    }

    override fun endWork() {
        AppSettings.save(AppSettings.KEY_OPERATOR, currentUserData)
        AppSettings.save(AppSettings.KEY_IS_EMPLOYEE_AUTH, true)
    }

    override fun prepare() {
        baseParams[PROCESS_TOKEN] = ProcessToken.AUTH_EMPLOYEE.value
        baseParams[PROCESS_NAME] = "Auth"
        contextParams[MAC] = Mac.getMacAddr()
        contextParams[OPERATOR] = currentUserData
    }

}