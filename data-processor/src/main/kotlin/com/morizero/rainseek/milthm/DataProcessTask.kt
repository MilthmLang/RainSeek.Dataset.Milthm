package com.morizero.rainseek.milthm

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.util.Arrays
import java.util.concurrent.ConcurrentHashMap
import org.gradle.api.DefaultTask
import org.gradle.api.NonNullApi
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@NonNullApi
@CacheableTask
open class DataProcessTask : DefaultTask() {
    @Internal
    val yamlMapper = YAMLMapper().registerKotlinModule()

    @Internal
    val resourceDirPath = File("${project.rootDir}/src/main/resources/input")
    @Internal
    val chartsDirPath: Array<out File> = resourceDirPath.resolve("charts").listFiles()
    @Internal
    val illustrationsDirPath: Array<out File> = resourceDirPath.resolve("illustrations").listFiles()
    @Internal
    val peopleDirPath: Array<out File> = resourceDirPath.resolve("people").listFiles()
    @Internal
    val songsDirPath: Array<out File> = resourceDirPath.resolve("songs").listFiles()

    @Internal
    var chartMap: ConcurrentHashMap<String, Chart> = ConcurrentHashMap()
    @Internal
    var illustrationMap: ConcurrentHashMap<String, Illustration> = ConcurrentHashMap()
    @Internal
    var peopleMap: ConcurrentHashMap<String, People> = ConcurrentHashMap()
    @Internal
    var songsMap: ConcurrentHashMap<String, Song> = ConcurrentHashMap()

    @Internal
    var processedDocumentList: MutableList<ProcessedDocument> = mutableListOf()




    @TaskAction
    fun execute() {
        loadFiles()
        chartMap.forEach { (_, chart) ->
            val processedDocument = ProcessedDocument(
                id = chart.id,
                title = songsMap[(chart.songId)]?.title?: "",
                titleCulture = songsMap[chart.songId]?.titleCulture?: "",
                latinTitle = songsMap[chart.songId]?.latinTitle?: "",
                artist = songsMap[chart.songId]?.artist?.map { peopleMap[it]?.name ?: "null" } ?: emptyList(),
                illustrator = peopleMap[illustrationMap[chart.illustration]?.illustrator ?: ""]?.name?:"",
                illustration = illustrationMap[chart.illustration]?.description ?: "",
                illustrationTag = illustrationMap[chart.illustration]?.tags ?: emptyList(),
                squareArtwork = "",
                bpmInfo = chart.bpmInfo,
                songId = "",
                difficulty = chart.difficulty,
                difficultyValue = chart.difficultyValue,
                charter = chart.charter,
                chartId = chart.id,
                tags = chart.tags
            )
            processedDocumentList.add(processedDocument)
        }
        val objectMapper = ObjectMapper()
            .registerKotlinModule()
            .enable(SerializationFeature.INDENT_OUTPUT)

        val buildDir = project.layout.buildDirectory.asFile.get()
        buildDir.mkdirs()
        val testFile = File(buildDir, "/ProcessedDocument.json")
        testFile.createNewFile()
        objectMapper.writeValue(testFile, processedDocumentList)

    }

    fun loadFiles() {
        for (chart in chartsDirPath) {
            val c = yamlMapper.readValue(chart , Chart::class.java)
            chartMap.put(c.chartId , c)
        }
        for (illustration in illustrationsDirPath) {
            val i = yamlMapper.readValue(illustration , Illustration::class.java)
            illustrationMap.put(i.id , i)
        }
        for (people in peopleDirPath) {
            val p = yamlMapper.readValue(people , People::class.java)
            peopleMap.put(p.id , p)
        }
        for (song in songsDirPath) {
            val s = yamlMapper.readValue(song , Song::class.java)
            songsMap.put(s.id , s)
        }
    }
}
