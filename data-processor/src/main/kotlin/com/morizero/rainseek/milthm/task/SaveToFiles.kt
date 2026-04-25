package com.morizero.rainseek.milthm.task

import com.morizero.rainseek.milthm.model.ProcessedDocument
import com.morizero.rainseek.milthm.utils.jsonMapper
import com.morizero.rainseek.milthm.utils.yamlMapper
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class SaveToFiles : DefaultTask() {
    @get:Internal
    abstract val loadDataTask: Property<LoadDataTask>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    private val processedDocumentList: MutableList<ProcessedDocument>
        get() = loadDataTask.get().processedDocumentList

    @TaskAction
    fun execute() {
        saveToFiles()
    }

    private fun saveToFiles() {
        val out = outputDir.get().asFile
        out.mkdirs()

        File(out, "packed_document.json").also { jsonOutputFile ->
            jsonMapper.writeValue(jsonOutputFile, processedDocumentList)
        }
        File(out, "unpacked_document").also { dir ->
            dir.mkdirs()
            processedDocumentList.forEach { document ->
                val file = File(dir, "${document.fileName}.yaml")
                yamlMapper.writeValue(file, document)
            }
        }
    }
}

