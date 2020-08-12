package ru.icames.store.presentation.code_reader.code_handler

import ru.icames.terminal.domain.model.Step
import java.io.Serializable

interface StepHandler :Serializable {

    var stepsCount: Int

    var currentStep: Int

    /**
     * Return steps of scanning data.
     * @see ru.icames.store.presentation.code_reader.code_handler.AuthEmployeeCodeHandler
     * @return list of steps
     * */
    fun getSteps(): List<Step>

    /**
     * Starts scanning data by steps
     * @see ru.icames.store.presentation.code_reader.code_handler.AuthEmployeeCodeHandler
     * */
    fun initFirstStep()


    fun resetSteps()


}