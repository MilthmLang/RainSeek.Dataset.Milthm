package com.morizero.rainseek.milthm.task

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.ibm.icu.util.ULocale
import com.morizero.rainseek.milthm.model.BenchmarkResult
import com.morizero.rainseek.milthm.model.BenchmarkResult.Companion.print
import com.morizero.rainseek.milthm.model.ProcessedDocument
import com.morizero.rainseek.milthm.model.ProcessedDocument.Companion.fullDocument
import com.morizero.rainseek.milthm.model.TokenModel
import com.morizero.rainseek.milthm.tokenizer.IcuBreakIteratorTokenizer
import com.morizero.rainseek.milthm.tokenizer.MultiLanguageTokenizer
import com.morizero.rainseek.milthm.utils.delimitersList
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.lang3.RandomStringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.min
import kotlin.random.Random

@CacheableTask
open class BenchmarkTask : DefaultTask() {

    @Input
    lateinit var endPoint: String

    private lateinit var loadDataTask: LoadDataTask

    private val jsonMapper =
        ObjectMapper().registerKotlinModule().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val processedDocumentList: MutableList<ProcessedDocument>
        get() = loadDataTask.processedDocumentList

    private val client = OkHttpClient()

    @TaskAction
    fun execute() {
        loadDataTask = project.tasks.named("data-load").get() as LoadDataTask

        BenchmarkResult.printHeading()
        val result = tasksOf {
            task(title = "title", relevantDocs = makeTitleRelevance())
            task(title = "artist", relevantDocs = makeArtistRelevance())
            task(title = "artists_list", relevantDocs = makeArtistsListRelevance())
            task(title = "illustrators_list", relevantDocs = makeIllustratorsListRelevance())
            task(title = "charters_list", relevantDocs = makeChartersListRelevance())
            task(title = "people_list", relevantDocs = makePeopleListRelevance())
            task(title = "fuzz_zero", relevantDocs = makeFuzzZeroRelevance())
            task(title = "fuzz_segments", relevantDocs = makeFuzzSegmentsRelevance())
            task(title = "fuzz_latn", relevantDocs = makeFuzzLatnRelevance())
        }

        val totalQueryCount = result.sumOf { it.queryCount }
        val totalPrecision =
            if (totalQueryCount == 0) 0.0 else result.sumOf { it.precision * it.queryCount } / totalQueryCount
        val totalRecall =
            if (totalQueryCount == 0) 0.0 else result.sumOf { it.recall * it.queryCount } / totalQueryCount
        BenchmarkResult(
            title = "total",
            precision = totalPrecision,
            recall = totalRecall,
            queryCount = totalQueryCount
        ).print()
    }

    class TaskBuilder(val gradleTask: BenchmarkTask) {
        val result: MutableList<BenchmarkResult> = mutableListOf()

        fun task(title: String, relevantDocs: Map<String, List<String>>): BenchmarkResult {
            val ret = gradleTask.benchmarkTask(title, relevantDocs)
            ret.print()
            result.add(ret)
            return ret
        }
    }

    private fun tasksOf(closure: BenchmarkTask.TaskBuilder.() -> Unit): List<BenchmarkResult> {
        val builder = TaskBuilder(this)
        builder.apply(closure)
        return builder.result
    }

    private fun makeTitleRelevance(): Map<String, List<String>> {
        return makeRelevanceForListField { listOf(it.title, it.latinTitle) }
    }

    private fun makeArtistRelevance(): Map<String, List<String>> {
        return processedDocumentList.groupBy { it.artist }.mapValues { entry -> entry.value.map { it.id } }
    }

    private fun makeArtistsListRelevance(): Map<String, List<String>> {
        return makeRelevanceForListField { it.artistsList }
    }

    private fun makePeopleListRelevance(): Map<String, List<String>> {
        return makeRelevanceForListField { it.artistsList + it.illustratorsList + it.chartersList }
    }

    private fun makeIllustratorsListRelevance(): Map<String, List<String>> {
        return makeRelevanceForListField { it.illustratorsList }
    }

    private fun makeChartersListRelevance(): Map<String, List<String>> {
        return makeRelevanceForListField { it.chartersList }
    }

    private fun makeRelevanceForListField(fieldListExtractor: (ProcessedDocument) -> List<String>): Map<String, List<String>> {
        return processedDocumentList.flatMap { document ->
            fieldListExtractor(document).map { people ->
                people to document.id
            }
        }.groupBy({ it.first }, { it.second }).mapValues { entry -> entry.value.distinct() }
    }

    private fun makeFuzzZeroRelevance(
        count: Int = 1000, mu: Double = 2.2, sigma: Double = 0.5
    ): Map<String, List<String>> {
        return (1..count).associate {
            val length = generateLogNormalLength(mu, sigma).coerceIn(3, 20)
            val randomString = RandomStringUtils.insecure().next(length, true, true)
            randomString to emptyList<String>()
        }
    }

    private fun generateLogNormalLength(mu: Double, sigma: Double): Int {
        val randomValue = Random.nextDouble()
        val logNormalValue = exp(mu + sigma * ln(randomValue))
        return logNormalValue.toInt()
    }

    private fun makeFuzzSegmentsRelevance(): Map<String, List<String>> {
        val getTokenizer = ConcurrentHashMap<String, IcuBreakIteratorTokenizer>()
        val ret = ConcurrentHashMap<String, MutableList<String>>()

        processedDocumentList.forEach { document ->
            val cultureListStr = document.titleCulture
            cultureListStr.split(":").forEach { culture ->
                val tokenizer = getTokenizer.getOrPut(culture) {
                    IcuBreakIteratorTokenizer(
                        locale = ULocale.forLanguageTag(culture),
                        predictor = fun(tokenModel: TokenModel): Boolean {
                            return !delimitersList.contains(tokenModel.value)
                        },
                    )
                }

                val tokens = tokenizer.tokenize(document.fullDocument())
                addTokensToRelevance(document, ret, tokens)
            }
        }

        return ret
    }

    private fun makeFuzzLatnRelevance(): Map<String, List<String>> {
        val ret = ConcurrentHashMap<String, MutableList<String>>()

        processedDocumentList.forEach { document ->
            val tokenizer = MultiLanguageTokenizer(
                cultureListStr = document.titleCulture,
                predictor = fun(tokenModel: TokenModel): Boolean {
                    return !delimitersList.contains(tokenModel.value)
                },
            )
            val tokens = tokenizer.tokenize(document.fullDocument())
            addTokensToRelevance(document, ret, tokens)
        }

        return ret
    }


    private fun addTokensToRelevance(
        document: ProcessedDocument, ret: ConcurrentHashMap<String, MutableList<String>>, tokens: List<TokenModel>
    ) {
        val longTokens = tokens.mapNotNull { if (it.value.length > 5) it.value else null }
        if (longTokens.isEmpty()) {
            return
        }
        val numberToPick = min(longTokens.size, 10)
        val selectedTokens = longTokens.shuffled().take(numberToPick)

        selectedTokens.forEach { token ->
            ret.getOrPut(token) { mutableListOf() }.add(document.id)
        }
    }

    fun benchmarkTask(title: String, relevantDocs: Map<String, List<String>>): BenchmarkResult {
        var totalPrecision = 0.0
        var totalRecall = 0.0
        var count = 0

        for ((query, expectedIds) in relevantDocs) {
            val searchResults = search(query, client)
            val retrievedIds = searchResults.map { it.id }

            val relevantSet = expectedIds.toSet()
            val retrievedSet = retrievedIds.toSet()
            val intersection = relevantSet.intersect(retrievedSet)

            val precision = if (retrievedSet.isNotEmpty()) {
                intersection.size.toDouble() / retrievedSet.size
            } else 0.0

            val recall = if (relevantSet.isNotEmpty()) {
                intersection.size.toDouble() / relevantSet.size
            } else 0.0

            totalPrecision += precision
            totalRecall += recall
            count++
        }

        return if (count > 0) {
            BenchmarkResult(
                title = title,
                precision = totalPrecision / count,
                recall = totalRecall / count,
                queryCount = relevantDocs.size
            )
        } else {
            BenchmarkResult(title = title)
        }
    }

    private fun search(query: String, client: OkHttpClient): List<ProcessedDocument> {
        val requestUrl = endPoint.toHttpUrl().newBuilder().addQueryParameter("q", query).build()
        val request = Request.Builder().url(requestUrl).build()

        val response: Response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Unexpected code $response")
        }

        val json = response.body?.string() ?: return emptyList()
        return try {
            jsonMapper.readValue(json)
        } catch (e: Exception) {
            logger.error("Failed to parse JSON: ${e.message}")
            emptyList()
        }
    }
}
