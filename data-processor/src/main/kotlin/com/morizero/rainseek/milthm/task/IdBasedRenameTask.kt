package com.morizero.rainseek.milthm.task

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.morizero.rainseek.milthm.model.IdModel
import org.gradle.api.DefaultTask
import org.gradle.api.NonNullApi
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

@NonNullApi
@CacheableTask
open class IdBasedRenameTask @Inject constructor(
    @Inject val subDir: String,
    @Inject val type: String,
) : DefaultTask() {
    @Internal
    val resourceDirPath = File("${project.rootDir}/src/main/resources/input")

    @Internal
    val files: Array<out File> = resourceDirPath.resolve(subDir).listFiles() ?: emptyArray()

    @Internal
    val yamlMapper = YAMLMapper().registerKotlinModule()

    @Internal
    val idPrefix = type + "_"

    @TaskAction
    fun execute() {
        val idRegex = Regex("${idPrefix}[0-9a-fA-F]{32}")

        var p = false
        for (file in files) {
            val idModel = yamlMapper.readValue(file, IdModel::class.java)
            val id = idModel.id
            if (idRegex.matches(id)) {
                continue
            }
            val expectedName = id.substring(idPrefix.length)
            val expectedFileName = "$expectedName.yaml"
            if (expectedFileName == file.name) {
                continue
            }
            logger.warn("{} -> {}", file.name, expectedFileName)

            val destFile = file.parentFile.resolve(expectedFileName)
            file.renameTo(destFile)
            p = true
        }
        if (!p) {
            logger.warn("no file renamed")
        }
    }
}
