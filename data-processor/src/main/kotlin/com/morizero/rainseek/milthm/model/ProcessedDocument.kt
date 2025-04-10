package com.morizero.rainseek.milthm.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.morizero.rainseek.milthm.utils.StringOrStringList

data class ProcessedDocument(
    override var id: String,

    @JsonIgnore
    var fileName: String = "",

    @JsonIgnore
    var songId: String = "",
    var title: String,
    var titleCulture: String = "",
    var latinTitle: String = "",
    var bpmInfo: List<BPMData> = emptyList(),

    var artist: String = "",
    @JsonDeserialize(using = StringOrStringList::class)
    var artistsList: List<String> = emptyList(),

    var illustrator: List<String> = emptyList(),
    @JsonDeserialize(using = StringOrStringList::class)
    var illustratorsList: List<String> = emptyList(),

    var difficulty: String = "",
    var difficultyValue: Double = 0.0,

    var charter: String = "",
    @JsonDeserialize(using = StringOrStringList::class)
    var chartersList: List<String> = emptyList(),

    var tags: List<String> = emptyList()
) : IdInterface
