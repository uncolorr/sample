package ru.icames.store.domain.model

import com.google.gson.annotations.SerializedName

data class AppUpdateInfo(
        @SerializedName("VersionCode")
        val versionCode: Int,
        @SerializedName("VersionName")
        val versionName: String,
        @SerializedName("CreationDate")
        val creationDate: String,
        @SerializedName("UpdateFile")
        val updateFile: UpdateFile
)
