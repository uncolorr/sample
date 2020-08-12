package ru.icames.store.util

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Enumerating of codecs in which date may come
 * Codec sets in settings.
 * @see ru.icames.terminal.presentation.settings.SettingsActivity
 * */

enum class Codec(private val description: String) {
    UTF_8("UTF-8"),
    WINDOWS_1251("Windows-1251");


    override fun toString(): String {
        return description
    }



    companion object {

        /**
         * Decode data with current codec
         * @param data decoding value
         * @param codec current codec. If codec is null return data
         * */
        fun decode(data: String, codec: Codec?): String {
            if(data.isEmpty()){
                return data
            }
            return when(codec){
                UTF_8 -> {
                    data
                }
                WINDOWS_1251 -> {
                    val encoded = URLEncoder.encode(data, "cp1252")
                    URLDecoder.decode(encoded, "cp1251")
                }
                null -> {
                    data
                }
            }
        }
    }

}

