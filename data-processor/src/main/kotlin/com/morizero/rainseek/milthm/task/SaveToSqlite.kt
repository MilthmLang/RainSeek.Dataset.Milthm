package com.morizero.rainseek.milthm.task

import com.ibm.icu.util.ULocale
import com.morizero.rainseek.milthm.indexing.IndexService
import com.morizero.rainseek.milthm.indexing.KtormRepository
import com.morizero.rainseek.milthm.indexing.RepositoryFactory
import com.morizero.rainseek.milthm.indexing.ShadowRepository
import com.morizero.rainseek.milthm.model.*
import com.morizero.rainseek.milthm.tokenizer.*
import com.morizero.rainseek.milthm.utils.MapIdObject
import com.morizero.rainseek.milthm.utils.delimitersList
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.ktorm.database.Database
import org.sqlite.JDBC
import java.io.File

open class SaveToSqlite : DefaultTask() {
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
        saveToSqlite()
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
        val database = Database.Companion.connect(
            url = "jdbc:sqlite:$dbPath",
            driver = JDBC::class.java.name,
        )

        val repositoryFactory = RepositoryFactory() { indexName -> KtormRepository(database, indexName) }
        val shadowRepository = ShadowRepository(repositoryFactory)

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

        processedDocumentList.forEach { document ->
            val multiLanguageTokenizer = MultiLanguageTokenizer(
                cultureListStr = document.titleCulture,
                predictor = fun(tokenModel: TokenModel): Boolean {
                    return !delimitersList.contains(tokenModel.value)
                },
            )

            titleDelimiterIndexing.addDocument(document.id, document.title)
            title3GramIndexing.addDocument(document.id, document.title)
            val titleSegmentIndexing = IndexService(
                repository = shadowRepository, tokenizers = listOf(multiLanguageTokenizer), indexName = "title_segments"
            )
            titleSegmentIndexing.addDocument(document.id, document.title)

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

            val tagsSegmentsIndexing = IndexService(
                repository = shadowRepository, tokenizers = listOf(multiLanguageTokenizer), indexName = "tags_segments"
            )
            tagsSegmentsIndexing.addDocument(document.id, document.tags)
        }
    }
}