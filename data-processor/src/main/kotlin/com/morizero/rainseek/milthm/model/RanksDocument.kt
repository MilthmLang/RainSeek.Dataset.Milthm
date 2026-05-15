package com.morizero.rainseek.milthm.model

import com.fasterxml.jackson.annotation.JsonProperty

data class RanksDocument(
    var id: String,
    var title: String,
    var titleCulture: String,
    var latinTitle: String,
    var difficulty: String,
    @JsonProperty("illustration_id")
    var illustrationId: String
)

