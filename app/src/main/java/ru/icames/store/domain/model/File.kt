package ru.icames.store.domain.model

import com.google.gson.annotations.SerializedName

data class File(
        @SerializedName("Name")
        val name: String,
        @SerializedName("Uid")
        val uid: String,
        @SerializedName("FileSize")
        val fileSize: String
) {
}