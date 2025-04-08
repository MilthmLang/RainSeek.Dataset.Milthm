package com.morizero.rainseek.milthm.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.morizero.rainseek.milthm.utils.StringOrStringList

data class ProcessedDocument(
    override var id: String,
    var title: String,
    var titleCulture: String = "",
    var latinTitle: String = "",

    var artist: String = "",
    @JsonDeserialize(using = StringOrStringList::class)
    var artistsList: List<String> = emptyList(),

    @JsonDeserialize(using = StringOrStringList::class)
    var illustrator: List<String> = emptyList(),

    var bpmInfo: List<BPMData> = emptyList(),

    var songId: String = "",

    var difficulty: String = "",
    var difficultyValue: Double = 0.0,

    @JsonDeserialize(using = StringOrStringList::class)
    var charter: List<String> = emptyList(),

    var tags: List<String> = emptyList()
) : IdInterface
