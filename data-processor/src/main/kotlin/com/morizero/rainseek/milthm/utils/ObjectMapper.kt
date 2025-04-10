package com.morizero.rainseek.milthm.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

public val yamlMapper: ObjectMapper by lazy {
    YAMLMapper().registerKotlinModule().enable(SerializationFeature.INDENT_OUTPUT)
}

public val jsonMapper: ObjectMapper by lazy {
    ObjectMapper().registerKotlinModule().enable(SerializationFeature.INDENT_OUTPUT)
}
