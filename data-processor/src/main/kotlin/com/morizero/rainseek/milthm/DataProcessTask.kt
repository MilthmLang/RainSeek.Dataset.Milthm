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

    // 用来记录各类型 ID 是否被使用，保存时添加前缀以区分类型
    @Internal
    var keyUsedList: ConcurrentHashMap<String, Boolean> = ConcurrentHashMap()

    @Internal
    var processedDocumentList: MutableList<ProcessedDocument> = mutableListOf()

    @TaskAction
    fun execute() {
        loadFiles()

        // 遍历每个图表生成 ProcessedDocument
        chartMap.forEach { (_, chart) ->
            val song = songsMap[chart.songId]
            val illustration = illustrationMap[chart.illustration]

            // 合并 tags：图表、歌曲、插画，chart.charterRefs 关联人物，以及插画指定的 illustrator
            val allTags = mutableSetOf<String>()
            allTags.addAll(chart.tags)
            song?.let { allTags.addAll(it.tags) }
            illustration?.let { allTags.addAll(it.tags) }
            chart.charterRefs.forEach { ref ->
                peopleMap[ref]?.let { allTags.addAll(it.tags) }
            }
            illustration?.illustrator?.let { illustratorId ->
                peopleMap[illustratorId]?.let { allTags.addAll(it.tags) }
            }

            // 移除使用过的 ID（按照保存时添加的前缀）
            // 图表作为入口，不计入未使用检查，因此不移除其 keyUsedList 中的 chart_ 前缀项
            song?.id?.let { keyUsedList.remove("song_${it}") }
            illustration?.id?.let { keyUsedList.remove("illustration_${it}") }
            illustration?.illustrator?.let { keyUsedList.remove("people_${it}") }
            chart.charterRefs.forEach { ref ->
                peopleMap[ref]?.id?.let { keyUsedList.remove("people_${it}") }
            }
            // 如果图表中涉及到的人物，比如 chart.chartId 对应的人物，也移除掉
            peopleMap[chart.chartId]?.id?.let { keyUsedList.remove("people_${it}") }

            val processedDocument = ProcessedDocument(
                id = chart.id,
                title = song?.title ?: "",
                titleCulture = song?.titleCulture ?: "",
                latinTitle = song?.latinTitle ?: "",
                artist = song?.artist?.mapNotNull { peopleMap[it]?.name } ?: emptyList(),
                illustrator = illustration?.illustrator?.let { peopleMap[it]?.name } ?: "",
                illustration = illustration?.description ?: "",
                illustrationTag = illustration?.tags ?: emptyList(),
                squareArtwork = "",
                bpmInfo = chart.bpmInfo,
                songId = song?.id ?: "",
                difficulty = chart.difficulty,
                difficultyValue = chart.difficultyValue,
                charter = chart.charter,
                chartId = chart.id,
                tags = allTags.toList()
            )

            processedDocumentList.add(processedDocument)
        }

        // 输出最终结果
        val objectMapper = ObjectMapper()
            .registerKotlinModule()
            .enable(SerializationFeature.INDENT_OUTPUT)
        val buildDir = project.layout.buildDirectory.asFile.get()
        buildDir.mkdirs()
        val outputFile = File(buildDir, "ProcessedDocument.json")
        outputFile.createNewFile()
        objectMapper.writeValue(outputFile, processedDocumentList)

        // 警告：输出未被使用的 ID，格式为 "Key $k : $v not used"
        // 跳过图表相关的 key，因为图表作为入口，不计入未使用检查
        if (keyUsedList.isNotEmpty()) {
            keyUsedList.keys.sorted().forEach { key ->
                if (!key.startsWith("chart_")) {
                    logger.warn("Key ${key.split("_")[0]} : ${key.split("_")[1]} not used")

                }
            }
        }
    }

    fun loadFiles() {
        val errors = mutableListOf<String>()

        // 加载图表
        chartsDirPath.forEach { file ->
            val chart = yamlMapper.readValue(file, Chart::class.java)
            val key = chart.chartId
            if (chartMap.containsKey(chart.chartId)) {
                errors.add("Duplicate chart id: ${chart.chartId}")
            } else {
                chartMap[chart.chartId] = chart
                keyUsedList[key] = true
            }
        }

        // 加载插画
        illustrationsDirPath.forEach { file ->
            val illustration = yamlMapper.readValue(file, Illustration::class.java)
            val key = illustration.id
            if (illustrationMap.containsKey(illustration.id)) {
                errors.add("Duplicate illustration id: ${illustration.id}")
            } else {
                illustrationMap[illustration.id] = illustration
                keyUsedList[key] = true
            }
        }

        // 加载人物
        peopleDirPath.forEach { file ->
            val person = yamlMapper.readValue(file, People::class.java)
            val key = person.id
            if (peopleMap.containsKey(person.id)) {
                errors.add("Duplicate people id: ${person.id}")
            } else {
                peopleMap[person.id] = person
                keyUsedList[key] = true
            }
        }

        // 加载歌曲
        songsDirPath.forEach { file ->
            val song = yamlMapper.readValue(file, Song::class.java)
            val key = song.id
            if (songsMap.containsKey(song.id)) {
                errors.add("Duplicate song id: ${song.id}")
            } else {
                songsMap[song.id] = song
                keyUsedList[key] = true
            }
        }

        if (errors.isNotEmpty()) {
            //throw IllegalStateException("加载文件时出现以下错误：\n" + errors.joinToString(separator = "\n"))
        }
    }
}
