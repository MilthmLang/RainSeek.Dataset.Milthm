package com.morizero.rainseek.milthm.task

import com.ibm.icu.util.ULocale
import com.morizero.rainseek.milthm.indexing.IndexService
import com.morizero.rainseek.milthm.indexing.KtormRepository
import com.morizero.rainseek.milthm.indexing.RepositoryFactory
import com.morizero.rainseek.milthm.indexing.ShadowRepository
import com.morizero.rainseek.milthm.model.*
import com.morizero.rainseek.milthm.tokenizer.BasicTokenizer
import com.morizero.rainseek.milthm.tokenizer.IcuBreakIteratorTokenizer
import com.morizero.rainseek.milthm.tokenizer.LineTokenizer
import com.morizero.rainseek.milthm.tokenizer.NGramTokenizer
import com.morizero.rainseek.milthm.utils.MapIdObject
import com.morizero.rainseek.milthm.utils.jsonMapper
import com.morizero.rainseek.milthm.utils.yamlMapper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.ktorm.database.Database
import java.io.File

open class DataProcessTask : DefaultTask() {
    @Internal
    var chartMap: MapIdObject<Chart> = MapIdObject()

    @Internal
    var illustrationMap: MapIdObject<Illustration> = MapIdObject("illustration")

    @Internal
    var peopleMap: MapIdObject<People> = MapIdObject("people")

    @Internal
    var songsMap: MapIdObject<Song> = MapIdObject("song")

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

    private fun saveToSqlite() {
        val buildDir = project.layout.buildDirectory.asFile.get()
        val outputDir = buildDir.resolve("output")
        outputDir.mkdirs()

        val dbFile = File(outputDir, "chart_documents_indexing.db")
        if (dbFile.exists()) {
            dbFile.delete()
        }

        val dbPath = dbFile.absolutePath
        val database = Database.connect(
            url = "jdbc:sqlite:$dbPath",
            driver = org.sqlite.JDBC::class.java.name,
        )

        val repositoryFactory = RepositoryFactory() { indexName -> KtormRepository(database, indexName) }
        val shadowRepository = ShadowRepository(repositoryFactory)

        val delimitersList = listOf(
            "#", "~", "-", "(", ")", "?", ".",
            "!", ",", "+", ".", "_", "†", "/",
            "\"", "\r", "\n",
            "（", "）", "・",

            "\u0020", "\u00A0",
            "\u1680",
            "\u2000", "\u2001", "\u2002", "\u2003",
            "\u2004", "\u2005", "\u2006", "\u2007",
            "\u2008", "\u2009", "\u200A",
            "\u202F", "\u205F", "\u3000",
        )
        val basicDelimitersTokenizer = BasicTokenizer(
            delimiters = delimitersList,
            predictor = fun(tokenModel: TokenModel): Boolean {
                return tokenModel.value.length >= 3
            },
        )
        val nameDelimitersTokenizer = BasicTokenizer(
            delimiters = delimitersList,
            predictor = fun(tokenModel: TokenModel): Boolean {
                return tokenModel.value.isNotEmpty()
            },
        )

        val titleDelimiterIndexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                nameDelimitersTokenizer
            ), indexName = "title_delimiter"
        )
        val title3GramIndexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                NGramTokenizer(3, delimiters = delimitersList),
                NGramTokenizer(2, delimiters = delimitersList),
                NGramTokenizer(1, delimiters = delimitersList),
            ), indexName = "title_ngram3"
        )

        val latinTitleIndexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                IcuBreakIteratorTokenizer(
                    locale = ULocale.ENGLISH,
                    predictor = fun(tokenModel: TokenModel): Boolean {
                        return !delimitersList.contains(tokenModel.value)
                    },
                )
            ), indexName = "latin_title_segments"
        )
        val latinTitle3GramIndexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                NGramTokenizer(3, delimiters = delimitersList),
                NGramTokenizer(2, delimiters = delimitersList),
                NGramTokenizer(1, delimiters = delimitersList),
            ), indexName = "latin_title_ngram3"
        )

        val artistDelimiterIndexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                nameDelimitersTokenizer
            ), indexName = "artist_delimiter"
        )

        val artistsListDelimiterIndexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                nameDelimitersTokenizer
            ), indexName = "artists_list_delimiter"
        )

        val illustratorDelimiterIndexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                nameDelimitersTokenizer
            ), indexName = "illustrator_delimiter"
        )

        val illustratorsListDelimiterIndexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                nameDelimitersTokenizer
            ), indexName = "illustrators_list_delimiter"
        )

        val charterDelimiterIndexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                nameDelimitersTokenizer
            ), indexName = "charter_delimiter"
        )

        val chartersListDelimiterIndexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                nameDelimitersTokenizer
            ), indexName = "charters_list_delimiter"
        )

        val tagsDelimiterIndexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                basicDelimitersTokenizer,
                LineTokenizer(delimitersList),
            ), indexName = "tags_delimiter"
        )
        val tagsNgram3Indexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                NGramTokenizer(3, delimiters = delimitersList),
            ), indexName = "tags_ngram3"
        )
        val tagsSegmentsIndexing = IndexService(
            repository = shadowRepository, tokenizers = listOf(
                IcuBreakIteratorTokenizer(
                    locale = ULocale.CHINA, predictor = fun(tokenModel: TokenModel): Boolean {
                        return !delimitersList.contains(tokenModel.value)
                    })
            ), indexName = "tags_segments"
        )

        processedDocumentList.forEach { document ->
            titleDelimiterIndexing.addDocument(document.id, document.title)
            title3GramIndexing.addDocument(document.id, document.title)
            document.titleCulture.split(":").forEach { culture ->
                val titleSegmentIndexing = IndexService(
                    repository = shadowRepository, tokenizers = listOf(
                        IcuBreakIteratorTokenizer(
                            locale = ULocale.forLanguageTag(culture),
                            predictor = fun(tokenModel: TokenModel): Boolean {
                                return !delimitersList.contains(tokenModel.value)
                            },
                        )
                    ), indexName = "title_segments"
                )
                titleSegmentIndexing.addDocument(document.id, document.title)
            }

            latinTitleIndexing.addDocument(document.id, document.latinTitle)
            latinTitle3GramIndexing.addDocument(document.id, document.latinTitle)

            artistDelimiterIndexing.addDocument(document.id, listOf(document.artist))
            artistsListDelimiterIndexing.addDocument(document.id, document.artistsList)

            illustratorDelimiterIndexing.addDocument(document.id, document.illustrator)
            illustratorsListDelimiterIndexing.addDocument(document.id, document.illustratorsList)

            charterDelimiterIndexing.addDocument(document.id, document.charter)
            chartersListDelimiterIndexing.addDocument(document.id, document.chartersList)

            tagsDelimiterIndexing.addDocument(document.id, document.tags)
            tagsNgram3Indexing.addDocument(document.id, document.tags)
            tagsSegmentsIndexing.addDocument(document.id, document.tags)
        }
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
