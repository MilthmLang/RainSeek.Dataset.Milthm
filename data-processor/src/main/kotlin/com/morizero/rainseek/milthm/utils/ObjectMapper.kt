package com.morizero.rainseek.milthm.utils

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.dataformat.yaml.YAMLFactory
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule

val yamlMapper: ObjectMapper by lazy {
    YAMLMapper(
        YAMLMapper.Builder(YAMLFactory())
            .addModule(kotlinModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
    )
}

val jsonMapper: ObjectMapper by lazy {
    jsonMapper {
        addModule(kotlinModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        enable(SerializationFeature.INDENT_OUTPUT)
    }
}
