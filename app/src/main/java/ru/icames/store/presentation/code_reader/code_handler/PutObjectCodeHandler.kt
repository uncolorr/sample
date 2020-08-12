package ru.icames.store.presentation.code_reader.code_handler

import presentation.code_reader.Entity
import ru.icames.store.application.App
import ru.icames.store.application.AppSettings
import ru.icames.store.domain.model.Code
import ru.icames.store.presentation.code_reader.AccountingMode
import ru.icames.store.util.Fields
import ru.icames.store.util.Mac
import ru.icames.store.util.ProcessToken
import ru.icames.terminal.domain.model.Step

class PutObjectCodeHandler: CodeHandler, StepHandler {
    override val baseParams: HashMap<String, String> = hashMapOf()

    override val contextParams: HashMap<String, String> = hashMapOf()

    override var isInfiniteReading: Boolean = true

    override var listener: CodeHandler.CodeHandlerListener? = null

    override var stepsCount: Int = 2

    override var currentStep: Int = 1


    override fun handleValue(value: String) {
        if (listener == null) {
            return
        }
        App.log("handle value")
        if (value.isEmpty()) {
            return
        }
        val items = split(value)
        if (items.isEmpty()) {
            return
        }

        val step = getSteps()[currentStep - 1]

        val isStepEntityAccepted=  checkEntityAccepted(items[0], step.entity)
        if(!isStepEntityAccepted){
            listener!!.onError("Неверный формат данных: неверное ключевое слово")
            return
        }

        val entity = Entity.find(items[0]) ?: return

        val isValueCountAccepted = items.size >= entity.valuesCount
        if (!isValueCountAccepted) {
            listener!!.onError("Неверный формат данных: неверное количество cвойств объекта")
            return
        }

        contextParams[step.propertyName] = value
        listener!!.onDataReaded(Code(entity.description, value))
        if(currentStep == stepsCount){
            listener!!.onStepsFinished()
            return
        }

        currentStep++
        val nextStep = getSteps()[currentStep - 1]
        listener!!.onNextStep(nextStep)
    }

    override fun endWork() {

    }

    override fun prepare() {

        val operator: String = AppSettings.get<String>(AppSettings.KEY_OPERATOR) ?: ""

        baseParams[Fields.PackingTasks.PROCESS_TOKEN] = ProcessToken.PUT_OBJECT.value
        baseParams[Fields.PackingTasks.PROCESS_NAME] = "Put object"
        contextParams[Fields.PackingTasks.OPERATOR] = operator
        contextParams[Fields.PackingTasks.MAC] = Mac.getMacAddr()
    }

    override fun getSteps(): List<Step> {
        val result = ArrayList<Step>()

        val stepCell = Step(
            1,
            "Отсканируйте ячейку",
            AccountingMode.STORE_CELL,
            Fields.PackingTasks.CELL
        )
        result.add(stepCell)

        val stepObject = Step(
            2,
            "Отсканируйте объект",
            AccountingMode.OBJECT,
            Fields.PackingTasks.OBJECT
        )
        result.add(stepObject)

        return result
    }

    override fun initFirstStep() {
        listener!!.onNextStep(getSteps()[0])
    }

    override fun resetSteps() {
        currentStep = 1
        for (step in getSteps()) {
            contextParams.remove(step.propertyName)
        }
        initFirstStep()
    }
}