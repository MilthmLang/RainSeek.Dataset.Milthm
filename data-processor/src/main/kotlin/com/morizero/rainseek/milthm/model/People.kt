package com.morizero.rainseek.milthm.model

data class People(
    var id: String,
    var name: String = "",
    var description: String = "",
    var tags: List<String> = emptyList()
)