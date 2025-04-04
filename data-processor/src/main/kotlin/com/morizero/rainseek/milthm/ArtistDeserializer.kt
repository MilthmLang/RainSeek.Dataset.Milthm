package com.morizero.rainseek.milthm

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException


class ArtistDeserializer : JsonDeserializer<MutableList<String?>?>() {
    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): MutableList<String?>? {
        val artists = ArrayList<String?>()

        if (p.currentToken() == JsonToken.VALUE_STRING) {
            val value = p.valueAsString
            if (!value.isEmpty()) {
                artists.add(value)
            }
        } else if (p.currentToken() == JsonToken.START_ARRAY) {
            while (p.nextToken() != JsonToken.END_ARRAY) {
                val value = p.valueAsString
                if (!value.isEmpty()) {
                    artists.add(value)
                }
            }
        } else if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null
        }

        return if (artists.isEmpty()) null else artists
    }
}