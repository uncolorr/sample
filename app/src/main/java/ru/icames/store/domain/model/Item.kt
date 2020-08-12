package ru.icames.store.domain.model

import com.google.gson.annotations.SerializedName
import ru.icames.store.domain.model.DataArray


data class Item (

    @SerializedName("Data")
    val data: DataArray?,

    @SerializedName("DataArray")
    val dataArray: List<DataArray>,

    @SerializedName("Name")
    val name: String,

    @SerializedName("Value")
    val value: String
)