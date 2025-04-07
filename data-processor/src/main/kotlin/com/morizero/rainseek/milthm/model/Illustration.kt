package com.morizero.rainseek.milthm.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.morizero.rainseek.milthm.utils.StringOrStringList

data class Illustration(
    var id: String = "",
    @JsonDeserialize(using = StringOrStringList::class)
    var illustrator: List<String> = emptyList(),
    var squareArtwork: String = "",
    var description: String = "",
    var tags: List<String> = emptyList()
)