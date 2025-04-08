package com.morizero.rainseek.milthm.model


import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class IdModel(
    override var id: String = "",
) : IdInterface
