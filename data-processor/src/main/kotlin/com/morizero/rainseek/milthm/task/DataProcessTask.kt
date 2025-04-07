package com.morizero.rainseek.milthm.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.morizero.rainseek.milthm.model.*
import org.gradle.api.DefaultTask
import org.gradle.api.NonNullApi
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * 数据处理任务，用于处理图表、歌曲、插画和人物数据，并生成最终的ProcessedDocument列表。
 * 该任务会检查数据的完整性，并输出未被使用的ID作为警告。
 */
@NonNullApi  // 表示该API不接受null参数
@CacheableTask  // 表示该任务支持增量构建和缓存
open class DataProcessTask : DefaultTask() {
    /**
     * YAML映射器，用于读取和解析YAML格式的文件。
     * 注册了Kotlin模块以支持Kotlin特有的特性。
     */
    @Internal  // 表示该属性不是任务的输入或输出
    val yamlMapper = YAMLMapper().registerKotlinModule()

    @Internal
    var enableOnExceptionToThrow: Boolean = true

    /**
     * 资源目录路径，包含输入数据文件。
     */
    @Internal
    val resourceDirPath = File("${project.rootDir}/src/main/resources/input")

    /**
     * 图表目录下的所有文件。
     */
    @Internal
    val chartsDirPath: Array<out File> = resourceDirPath.resolve("charts").listFiles() ?: emptyArray()

    /**
     * 插画目录下的所有文件。
     */
    @Internal
    val illustrationsDirPath: Array<out File> = resourceDirPath.resolve("illustrations").listFiles() ?: emptyArray()

    /**
     * 人物目录下的所有文件。
     */
    @Internal
    val peopleDirPath: Array<out File> = resourceDirPath.resolve("people").listFiles() ?: emptyArray()

    /**
     * 歌曲目录下的所有文件。
     */
    @Internal
    val songsDirPath: Array<out File> = resourceDirPath.resolve("songs").listFiles() ?: emptyArray()

    /**
     * 图表数据的映射表，键为图表ID，值为图表对象。
     */
    @Internal
    var chartMap: ConcurrentHashMap<String, Chart> = ConcurrentHashMap()

    /**
     * 插画数据的映射表，键为插画ID，值为插画对象。
     */
    @Internal
    var illustrationMap: ConcurrentHashMap<String, Illustration> = ConcurrentHashMap()

    /**
     * 人物数据的映射表，键为人物ID，值为人物对象。
     */
    @Internal
    var peopleMap: ConcurrentHashMap<String, People> = ConcurrentHashMap()

    /**
     * 歌曲数据的映射表，键为歌曲ID，值为歌曲对象。
     */
    @Internal
    var songsMap: ConcurrentHashMap<String, Song> = ConcurrentHashMap()

    /**
     * 用于记录各类型ID是否被使用的集合。
     * 保存时添加前缀以区分类型。
     */
    @Internal
    var keyUsedSet = mutableSetOf<String>()

    /**
     * 处理后的文档列表，包含所有合并后的数据。
     */
    @Internal
    var processedDocumentList: MutableList<ProcessedDocument> = mutableListOf()

    /**
     * 任务执行的主方法。
     * 加载所有数据文件，处理并生成最终的ProcessedDocument列表。
     * 输出未被使用的ID作为警告。
     */
    @TaskAction
    fun execute() {
        loadFiles()

        chartMap.forEach { (_, chart) ->
            val song = songsMap[chart.songId] ?: throw IllegalArgumentException("Unknown song ID: ${chart.songId}")

            var allTags = mutableSetOf<String>()
            allTags.addAll(chart.tags)
            song.let { allTags.addAll(it.tags) }
            chart.charterRefs.forEach { ref ->
                peopleMap[ref]?.let { allTags.addAll(it.tags) }
            }


            val landscapeIllustrations = chart.illustration.mapNotNull { illustrationId ->
                illustrationMap[illustrationId]
            }.toList()
            val squareIllustrations = chart.illustrationSquare.mapNotNull { illustrationId ->
                illustrationMap[illustrationId]
            }.toList()
            val illustrations = landscapeIllustrations + squareIllustrations

            illustrations.forEach {
                it.let { allTags.addAll(it.tags) }
                it.illustrator.forEach { illustratorId ->
                    peopleMap[illustratorId]?.let { allTags.addAll(it.tags) }
                }
            }

            song.artistsRef.forEach { artistId ->
                peopleMap[artistId]?.let { allTags.addAll(it.tags) }
            }

            // 对集合进行排序
            keyUsedSet = keyUsedSet.toSortedSet()
            allTags = allTags.filter { it.isNotBlank() }.distinct().toSortedSet()

            // 创建处理后的文档对象
            val processedDocument = ProcessedDocument(
                id = chart.id,
                title = song.title ?: "",
                titleCulture = song.titleCulture ?: "",
                latinTitle = song.latinTitle ?: "",
                artist = song.artist ?: "",
                artistsList = song.artistsRef.mapNotNull { peopleMap[it]?.name } ?: emptyList(),
                illustrator = illustrations.flatMap { it.illustrator }.distinct().mapNotNull { peopleMap[it]?.name },
                bpmInfo = chart.bpmInfo,
                songId = song.id ?: "",
                difficulty = chart.difficulty,
                difficultyValue = chart.difficultyValue,
                charter = chart.charterRefs.map { ref -> peopleMap[ref]?.name ?: "" },
                chartId = chart.chartId,
                tags = allTags.toList()
            )

            // 添加到处理后的文档列表
            processedDocumentList.add(processedDocument)

            keyUsedSet.remove(chart.chartId)
            illustrations.forEach { keyUsedSet.remove(it.id) }
            illustrations.forEach { illustration ->
                illustration.illustrator.forEach {
                    keyUsedSet.remove(it)
                }
            }

            keyUsedSet.remove(chart.songId)
            chart.charterRefs.forEach { ref ->
                keyUsedSet.remove(ref)
            }
            song.artistsRef.forEach { id ->
                keyUsedSet.remove(id)
            }
            keyUsedSet = keyUsedSet.toSortedSet()
        }

        // 配置JSON映射器并输出最终结果
        val objectMapper = ObjectMapper().registerKotlinModule().enable(SerializationFeature.INDENT_OUTPUT)
        val buildDir = project.layout.buildDirectory.asFile.get()
        buildDir.mkdirs()
        val outputFile = File(buildDir, "ProcessedDocument.json")
        outputFile.createNewFile()
        objectMapper.writeValue(outputFile, processedDocumentList)

        // 输出未被使用的ID作为警告（跳过图表相关的key）
        if (keyUsedSet.isNotEmpty()) {
            keyUsedSet.forEach { key ->
                if (!key.startsWith("chart_")) {
                    val parts = key.split("_", limit = 2)
                    logger.warn("Key ${parts[0]} : ${parts[1]} not used")
                }
            }
        }
    }

    /**
     * 加载所有数据文件到对应的映射表中。
     * 检查重复的ID并在发现错误时抛出异常。
     */
    fun loadFiles() {
        val errors = mutableListOf<String>()

        // 加载谱面数据
        chartsDirPath.forEach { file ->
            val chart = yamlMapper.readValue(file, Chart::class.java)
            if (chartMap.containsKey(chart.chartId)) {
                errors.add("重复的谱面 ID: ${chart.chartId}")
            } else {
                chartMap[chart.chartId] = chart
            }
        }

        // 加载插画数据
        illustrationsDirPath.forEach { file ->
            val illustration = yamlMapper.readValue(file, Illustration::class.java)
            if (illustrationMap.containsKey(illustration.id)) {
                errors.add("重复的插画 ID: ${illustration.id}")
            } else {
                illustrationMap[illustration.id] = illustration
                keyUsedSet.add(illustration.id)
            }
        }

        // 加载人物数据
        peopleDirPath.forEach { file ->
            val person = yamlMapper.readValue(file, People::class.java)
            if (peopleMap.containsKey(person.id)) {
                errors.add("重复的人物 ID: ${person.id}")
            } else {
                peopleMap[person.id] = person
                keyUsedSet.add(person.id)
            }
        }

        // 加载歌曲数据
        songsDirPath.forEach { file ->
            val song = yamlMapper.readValue(file, Song::class.java)
            if (songsMap.containsKey(song.id)) {
                errors.add("重复的歌曲 ID: ${song.id}")
            } else {
                songsMap[song.id] = song
                keyUsedSet.add(song.id)
            }
        }

        // 如果有错误，抛出异常
        if (errors.isNotEmpty() and enableOnExceptionToThrow) {
            throw IllegalStateException("加载文件时出现以下错误：\n" + errors.joinToString(separator = "\n"))
        }
    }
}