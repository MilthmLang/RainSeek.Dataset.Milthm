package com.morizero.rainseek.milthm.task

import com.morizero.rainseek.milthm.model.IdModel
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import tools.jackson.dataformat.yaml.YAMLFactory
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.kotlinModule
import javax.inject.Inject

@CacheableTask
abstract class IdBasedRenameTask @Inject constructor(
    @get:Input val subDir: String,
    @get:Input val type: String,
) : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDir: DirectoryProperty

    @Internal
    val yamlMapper = YAMLMapper(
        YAMLMapper.Builder(YAMLFactory()).addModule(kotlinModule())
    )

    @Internal
    val idPrefix = type + "_"

    @TaskAction
    fun execute() {
        val files = inputDir.get().asFile.resolve(subDir).listFiles() ?: emptyArray()
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
