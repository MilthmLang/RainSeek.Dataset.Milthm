package com.morizero.rainseek.milthm.task

import com.morizero.rainseek.milthm.model.ProcessedDocument
import com.morizero.rainseek.milthm.model.RanksDocument
import com.morizero.rainseek.milthm.utils.jsonMapper
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class SaveRanksDocument : DefaultTask() {
    @get:Internal
    abstract val loadDataTask: Property<LoadDataTask>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    private val processedDocumentList: MutableList<ProcessedDocument>
        get() = loadDataTask.get().processedDocumentList

    @TaskAction
    fun execute() {
        val out = outputDir.get().asFile
        out.mkdirs()

        val ranksDocument = processedDocumentList.map {
            RanksDocument(
                id = it.id,
                title = it.title,
                titleCulture = it.titleCulture,
                latinTitle = it.latinTitle,
                difficulty = it.difficulty,
                illustrationId = it.illustrationId
            )
        }

        File(out, "ranks_document.json").also { jsonOutputFile ->
            jsonMapper.writeValue(jsonOutputFile, ranksDocument)
        }
    }
}

