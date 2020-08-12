package ru.icames.terminal.domain.model

import presentation.code_reader.Entity
import ru.icames.store.presentation.code_reader.AccountingMode

data class Step(
        val id: Int,
        val description: String,
        val entity: AccountingMode,
        val propertyName: String = ""
)