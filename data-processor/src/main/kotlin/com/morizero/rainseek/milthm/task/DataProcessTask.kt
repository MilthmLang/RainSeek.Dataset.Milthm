package com.morizero.rainseek.milthm.task

import com.morizero.rainseek.milthm.model.*
import com.morizero.rainseek.milthm.utils.MapIdObject
import com.morizero.rainseek.milthm.utils.jsonMapper
import com.morizero.rainseek.milthm.utils.yamlMapper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2

open class DataProcessTask : DefaultTask() {
    @Internal
    var chartMap: MapIdObject<Chart> = MapIdObject()

    @Internal
    var illustrationMap: MapIdObject<Illustration> = MapIdObject()

    @Internal
    var peopleMap: MapIdObject<People> = MapIdObject()

    @Internal
    var songsMap: MapIdObject<Song> = MapIdObject()

    @Internal
    var processedDocumentList: MutableList<ProcessedDocument> = mutableListOf()

    @TaskAction
    fun execute() {
        loadFiles()
        processDocument()

        saveToFiles()
        saveToSqlite()
    }

    private fun saveToFiles() {
        val buildDir = project.layout.buildDirectory.asFile.get()
        val outputDir = buildDir.resolve("output")
        outputDir.mkdirs()

        File(outputDir, "packed-document.json").also { jsonOutputFile ->
            jsonMapper.writeValue(jsonOutputFile, processedDocumentList)
        }
        File(outputDir, "unpacked-document").also { dir ->
            dir.mkdirs()
            processedDocumentList.forEach { document ->
                val file = File(dir, "${document.fileName}.yaml")
                yamlMapper.writeValue(file, document)
            }
        }

    }

    private fun saveToSqlite() {

    }

    private fun processDocument() {

        chartMap.forEach { (_, chart) ->
            val song = songsMap[chart.songId] ?: throw IllegalArgumentException("Unknown song ID: ${chart.songId}")

            var allTags = mutableSetOf<String>()
            allTags.addAll(chart.tags)
            song.let { allTags.addAll(it.tags) }
            chart.chartersRef.forEach { ref ->
                peopleMap[ref]?.let { allTags.addAll(it.tags) }
            }

            val landscapeIllustrations = chart.illustration.map { illustrationId ->
                illustrationMap[illustrationId]
                    ?: throw IllegalArgumentException("Unknown illustration ID: $illustrationId")
            }.toList()
            val squareIllustrations = chart.illustrationSquare.map { illustrationId ->
                illustrationMap[illustrationId]
                    ?: throw IllegalArgumentException("Unknown illustration ID: $illustrationId")
            }.toList()
            val illustrations = landscapeIllustrations + squareIllustrations

            illustrations.forEach { illustration ->
                illustration.let { allTags.addAll(illustration.tags) }
                illustration.illustratorsList.forEach { illustratorId ->
                    peopleMap[illustratorId]?.let { people -> allTags.addAll(people.tags) }
                }
            }

            song.artistsRef.forEach { artistId ->
                peopleMap[artistId]?.let { allTags.addAll(it.tags) }
            }

            allTags = allTags.filter { it.isNotBlank() }.distinct().toSortedSet()

            val processedDocument = ProcessedDocument(
                id = chart.id,
                fileName = chart.songId.let { songId ->
                    if (!songId.startsWith("song_")) {
                        throw IllegalStateException("unacceptable songId: $songId")
                    }

                    val songName = songId.substring("song_".length)
                    val newFileName = "$songName-${chart.difficulty.lowercase()}"
                    newFileName
                },

                title = song.title,
                titleCulture = song.titleCulture,
                latinTitle = song.latinTitle,
                bpmInfo = chart.bpmInfo,
                songId = song.id,
                difficulty = chart.difficulty,
                difficultyValue = chart.difficultyValue,

                artist = song.artist,
                artistsList = song.artistsRef.map {
                    (peopleMap[it] ?: throw IllegalArgumentException("Unknown people ID: $it")).name
                },

                illustrator = illustrations.map { it.illustrator }.distinct(),
                illustratorsList = illustrations.flatMap { it.illustratorsList }.distinct()
                    .mapNotNull { peopleMap[it]?.name },

                charter = chart.charter,
                chartersList = chart.chartersRef.map { ref ->
                    (peopleMap[ref] ?: throw IllegalArgumentException("Unknown people ID: $ref")).name
                },

                tags = allTags.toList()
            )

            processedDocumentList.add(processedDocument)
        }

        processedDocumentList.sortBy { it.latinTitle }

        (illustrationMap.notAccessedKeys + songsMap.notAccessedKeys + peopleMap.notAccessedKeys).forEach { key ->
            logger.warn("no reference to $key")
        }
    }

    private fun loadFiles() {
        val resourceDirPath = project.rootDir.resolve("src/main/resources/input")

        resourceDirPath.resolve("charts").listFiles()?.let {
            chartMap.addAll(it.map { file ->
                try {
                    yamlMapper.readValue(file, Chart::class.java)
                } catch (ex: Exception) {
                    logger.error("fail to load {}", file.absoluteFile, ex)
                    throw ex
                }
            })
        }

        resourceDirPath.resolve("illustrations").listFiles()?.let {
            illustrationMap.addAll(it.map { file ->
                try {
                    yamlMapper.readValue(file, Illustration::class.java)
                } catch (ex: Exception) {
                    logger.error("fail to load {}", file.absoluteFile, ex)
                    throw ex
                }
            })
        }

        resourceDirPath.resolve("songs").listFiles()?.let {
            songsMap.addAll(it.map { file ->
                try {
                    yamlMapper.readValue(file, Song::class.java)
                } catch (ex: Exception) {
                    logger.error("fail to load {}", file.absoluteFile, ex)
                    throw ex
                }
            })
        }

        resourceDirPath.resolve("people").listFiles()?.let {
            peopleMap.addAll(it.map { file ->
                try {
                    yamlMapper.readValue(file, People::class.java)
                } catch (ex: Exception) {
                    logger.error("fail to load {}", file.absoluteFile, ex)
                    throw ex
                }
            })
        }
    }
}