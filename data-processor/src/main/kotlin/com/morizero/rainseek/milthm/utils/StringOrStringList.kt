package com.morizero.rainseek.milthm.utils

import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import java.io.IOException

class StringOrStringList : ValueDeserializer<List<String?>?>() {
    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): List<String?>? {
        val artists = ArrayList<String?>()

        if (p.currentToken() == JsonToken.VALUE_STRING) {
            val value = p.valueAsString
            if (!value.isEmpty()) {
                artists.add(value)
            }
        } else if (p.currentToken() == JsonToken.START_ARRAY) {
            while (p.nextToken() != JsonToken.END_ARRAY) {
                val value = p.valueAsString
                if (value.isNotEmpty()) {
                    artists.add(value)
                }
            }
        } else if (p.currentToken() == JsonToken.VALUE_NULL) {
            return artists
        }

        return artists
    }
}
