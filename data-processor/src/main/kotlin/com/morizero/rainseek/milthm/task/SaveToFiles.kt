package com.morizero.rainseek.milthm.task

import com.morizero.rainseek.milthm.model.*
import com.morizero.rainseek.milthm.utils.MapIdObject
import com.morizero.rainseek.milthm.utils.jsonMapper
import com.morizero.rainseek.milthm.utils.yamlMapper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SaveToFiles : DefaultTask() {
    private lateinit var loadDataTask: LoadDataTask

    private val chartMap: MapIdObject<Chart>
        get() = loadDataTask.chartMap

    private val illustrationMap: MapIdObject<Illustration>
        get() = loadDataTask.illustrationMap

    private val peopleMap: MapIdObject<People>
        get() = loadDataTask.peopleMap

    private val songsMap: MapIdObject<Song>
        get() = loadDataTask.songsMap

    private val processedDocumentList: MutableList<ProcessedDocument>
        get() = loadDataTask.processedDocumentList

    @TaskAction
    fun execute() {
        loadDataTask = project.tasks.named("data-load").get() as LoadDataTask
        saveToFiles()
    }

    private fun saveToFiles() {
        val buildDir = project.layout.buildDirectory.asFile.get()
        val outputDir = buildDir.resolve("output")
        outputDir.mkdirs()

        File(outputDir, "packed_document.json").also { jsonOutputFile ->
            jsonMapper.writeValue(jsonOutputFile, processedDocumentList)
        }
        File(outputDir, "unpacked_document").also { dir ->
            dir.mkdirs()
            processedDocumentList.forEach { document ->
                val file = File(dir, "${document.fileName}.yaml")
                yamlMapper.writeValue(file, document)
            }
        }
    }
}

