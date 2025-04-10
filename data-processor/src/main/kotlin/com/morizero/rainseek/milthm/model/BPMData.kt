package com.morizero.rainseek.milthm.model

import com.fasterxml.jackson.annotation.JsonIgnore

data class BPMData(
    var start: Float,
    var bpm: Float,
    @JsonIgnore
    var beatsPerBar: Int
)