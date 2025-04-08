package com.morizero.rainseek.milthm.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.morizero.rainseek.milthm.utils.StringOrStringList

data class Chart(
    override var id: String = "",

    @JsonDeserialize(using = StringOrStringList::class) var illustration: List<String> = emptyList(),

    @JsonDeserialize(using = StringOrStringList::class) var illustrationSquare: List<String> = emptyList(),

    var bpmInfo: List<BPMData> = emptyList(),
    var songId: String = "",
    var difficulty: String = "",
    var difficultyValue: Double = 0.0,
    var charter: String = "",
    var chartersRef: List<String> = emptyList(),
    var chartId: String = "",
    var tags: List<String> = emptyList()
) : IdInterface