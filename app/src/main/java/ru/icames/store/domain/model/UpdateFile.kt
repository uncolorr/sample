package ru.icames.store.domain.model

import com.google.gson.annotations.SerializedName

data class UpdateFile(
        @SerializedName("File")
        val file: File
)