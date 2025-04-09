package com.morizero.rainseek.milthm.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.morizero.rainseek.milthm.utils.StringOrStringList

data class Illustration(
    override var id: String = "",
    var illustrator: String = "",
    @JsonDeserialize(using = StringOrStringList::class)
    var illustratorsList: List<String> = emptyList(),
    var description: String = "",
    var tags: List<String> = emptyList(),
) : IdInterface