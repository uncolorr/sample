package ru.icames.store.domain.model

import androidx.annotation.Nullable
import com.google.gson.annotations.SerializedName


data class DataArray(

        @Nullable
        @SerializedName("Items")
        val items: List<Item>,

        @Nullable
        @SerializedName("Value")
        val value: String?
)
