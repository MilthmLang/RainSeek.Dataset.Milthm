package com.morizero.rainseek.milthm

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import org.gradle.api.DefaultTask
import org.gradle.api.NonNullApi
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

@NonNullApi
@CacheableTask
open class DataProcessTask : DefaultTask() {
    @Internal
    val yamlMapper = YAMLMapper().registerKotlinModule()

    @Internal
    val resourceDirPath = File("${project.rootDir}/src/main/resources/input")

    @Internal
    val chartsDirPath: Array<out File> = resourceDirPath.resolve("charts").listFiles() ?: emptyArray()

    @Internal
    val illustrationsDirPath: Array<out File> = resourceDirPath.resolve("illustrations").listFiles() ?: emptyArray()

    @Internal
    val peopleDirPath: Array<out File> = resourceDirPath.resolve("people").listFiles() ?: emptyArray()

    @Internal
    val songsDirPath: Array<out File> = resourceDirPath.resolve("songs").listFiles() ?: emptyArray()

    @Internal
    var chartMap: ConcurrentHashMap<String, Chart> = ConcurrentHashMap()

    @Internal
    var illustrationMap: ConcurrentHashMap<String, Illustration> = ConcurrentHashMap()

    @Internal
    var peopleMap: ConcurrentHashMap<String, People> = ConcurrentHashMap()

    @Internal
    var songsMap: ConcurrentHashMap<String, Song> = ConcurrentHashMap()

    @Internal
    var keyUsedList: ConcurrentHashMap<String, Boolean> = ConcurrentHashMap()

    @Internal
    var processedDocumentList: MutableList<ProcessedDocument> = mutableListOf()

    @TaskAction
    fun execute() {
        loadFiles()
        chartMap.forEach { (_, chart) ->
            val song = songsMap[chart.songId]
            val illustration = illustrationMap[chart.illustration]
            val artistNames = songsMap[chart.songId]?.artist?.mapNotNull { peopleMap[it]?.name } ?: emptyList()
            val processedDocument = ProcessedDocument(
                id = chart.id,
                title = song?.title ?: "",
                titleCulture = song?.titleCulture ?: "",
                latinTitle = song?.latinTitle ?: "",
                artist = artistNames,
                illustrator = peopleMap[illustration?.illustrator ?: ""]?.name ?: "",
                illustration = illustration?.description ?: "",
                illustrationTag = illustration?.tags ?: emptyList(),
                squareArtwork = "",
                bpmInfo = chart.bpmInfo,
                songId = "",
                difficulty = chart.difficulty,
                difficultyValue = chart.difficultyValue,
                charter = chart.charter,
                chartId = chart.id,
                tags = chart.tags +
                        (song?.tags ?: emptyList()) +
                        (illustration?.tags ?: emptyList()) +
                        (peopleMap[illustration?.illustrator ?: ""]?.tags ?: emptyList())
            )

            // 移除已经使用的 key
            chart.charterRefs.forEach { ref ->
                peopleMap[ref]?.id?.let { keyUsedList.remove(it) }
            }
            illustration?.id?.let { keyUsedList.remove(it) }
            peopleMap[chart.illustration]?.id?.let { keyUsedList.remove(it) }
            peopleMap[chart.chartId]?.id?.let { keyUsedList.remove(it) }
            songsMap[chart.songId]?.id?.let { keyUsedList.remove(it) }

            processedDocumentList.add(processedDocument)
        }

        val objectMapper = ObjectMapper()
            .registerKotlinModule()
            .enable(SerializationFeature.INDENT_OUTPUT)

        val buildDir = project.layout.buildDirectory.asFile.get()
        buildDir.mkdirs()
        val outputFile = File(buildDir, "ProcessedDocument.json")
        outputFile.createNewFile()
        objectMapper.writeValue(outputFile, processedDocumentList)

        if (keyUsedList.isNotEmpty()) {
            keyUsedList.keys.forEach { document ->
                val parts = document.split("_")
                if (parts.size >= 2) {
                    logger.warn("Key ${parts[0]} but not used: ${parts[1]}")
                }
            }
        }
    }

    fun loadFiles() {
        val errors = mutableListOf<String>()

        chartsDirPath.forEach { file ->
            val chart = yamlMapper.readValue(file, Chart::class.java)
            if (chartMap.containsKey(chart.chartId)) {
                errors.add("Duplicate chart id: ${chart.chartId}")
            } else {
                chartMap[chart.chartId] = chart
                keyUsedList[chart.chartId] = true
            }
        }

        illustrationsDirPath.forEach { file ->
            val illustration = yamlMapper.readValue(file, Illustration::class.java)
            if (illustrationMap.containsKey(illustration.id)) {
                errors.add("Duplicate illustration id: ${illustration.id}")
            } else {
                illustrationMap[illustration.id] = illustration
                keyUsedList[illustration.id] = true
            }
        }

        peopleDirPath.forEach { file ->
            val person = yamlMapper.readValue(file, People::class.java)
            if (peopleMap.containsKey(person.id)) {
                errors.add("Duplicate people id: ${person.id}")
            } else {
                peopleMap[person.id] = person
                keyUsedList[person.id] = true
            }
        }

        songsDirPath.forEach { file ->
            val song = yamlMapper.readValue(file, Song::class.java)
            if (songsMap.containsKey(song.id)) {
                errors.add("Duplicate song id: ${song.id}")
            } else {
                songsMap[song.id] = song
                keyUsedList[song.id] = true
            }
        }

        if (errors.isNotEmpty()) {
            throw IllegalStateException("加载文件时出现以下错误：\n" + errors.joinToString(separator = "\n"))
        }
    }

}
