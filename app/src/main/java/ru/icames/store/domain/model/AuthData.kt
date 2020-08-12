package ru.icames.store.domain.model

import com.google.gson.annotations.SerializedName

data class AuthData(
        @SerializedName("AuthToken")
        val authToken: String,
        @SerializedName("CurrentUserId")
        val currentUserId: String,
        @SerializedName("Lang")
        val lang: String,
        @SerializedName("SessionToken")
        val sessionToken: String
)