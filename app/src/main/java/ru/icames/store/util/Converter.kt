package ru.icames.store.util

import ru.icames.store.domain.model.DataArray
import ru.icames.store.domain.model.Item
import ru.icames.store.util.Fields.PackingTasks.CONTEXT


object Converter {

    fun convertToMap(items: List<Item>?): Map<String, Item> {
        if (items == null) {
            throw NullPointerException()
        }
        val map = HashMap<String, Item>()
        for (i in items.indices) {
            val item = items[i]
            map[item.name] = item
        }
        return map
    }

    fun convertDataToProcessRequestBody(params: Map<String, String>,
                                        context: Map<String, String>): DataArray {

        val paramItems = ArrayList<Item>()

        for ((k, v) in params) { // add processToken, name
            paramItems.add(Item(null, listOf(), k, v))
        }

        val contextItems = ArrayList<Item>()

        for ((k, v) in context) { // add Log_Operator1 etc..
            contextItems.add(Item(null, listOf(), k, v))
        }

        val contextData = DataArray(contextItems, null)

        paramItems.add(Item(contextData, listOf(), CONTEXT, ""))

        return DataArray(paramItems, null)

    }

}