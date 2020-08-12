package ru.icames.store.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty


class Error {
    @JsonProperty("InnerException")
    var InnerException: String = ""
    @JsonProperty("Message")
    var Message: String = ""
    @JsonProperty("StatusCode")
    var StatusCode: Int = 0

}