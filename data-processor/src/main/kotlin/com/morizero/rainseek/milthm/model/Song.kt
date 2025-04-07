package com.morizero.rainseek.milthm.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.morizero.rainseek.milthm.utils.StringOrStringList

data class Song(
    var id: String = "",
    var title: String = "",
    var titleCulture: String = "",
    val latinTitle: String = "",
    @JsonDeserialize(using = StringOrStringList::class)
    var artistsRef: List<String> = emptyList(),
    val artist: String,
    var tags: List<String> = emptyList()

)