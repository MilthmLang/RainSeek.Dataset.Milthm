package com.morizero.rainseek.milthm.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.morizero.rainseek.milthm.utils.StringOrStringList

data class ProcessedDocument(
    override var id: String,

    @JsonIgnore
    var fileName: String = "",

    /**
     * indexing: unique
     */
    @JsonIgnore
    var songId: String = "",

    /**
     * indexing: delimiter
     * boosting: 10
     *
     * indexing: segments
     * boosting: 5
     */
    var title: String,
    var titleCulture: String = "",

    /**
     * indexing: segments
     * boosting: 10
     */
    var latinTitle: String = "",

    var bpmInfo: List<BPMData> = emptyList(),

    /**
     * indexing: delimiter
     * boosting: 5
     */
    var artist: String = "",
    @JsonDeserialize(using = StringOrStringList::class)

    /**
     * indexing: delimiter
     * boosting: 5
     */
    var artistsList: List<String> = emptyList(),

    /**
     * indexing: delimiter
     * boosting: 5
     */
    var illustrator: List<String> = emptyList(),
    @JsonDeserialize(using = StringOrStringList::class)

    /**
     * indexing: delimiter
     * boosting: 5
     */
    var illustratorsList: List<String> = emptyList(),

    var difficulty: String = "",
    var difficultyValue: Double = 0.0,


    /**
     * indexing: delimiter
     * boosting: 5
     */
    var charter: String = "",

    /**
     * indexing: delimiter
     * boosting: 5
     */
    @JsonDeserialize(using = StringOrStringList::class)
    var chartersList: List<String> = emptyList(),


    /**
     * indexing: delimiter
     * boosting: 2
     *
     * indexing: ngram(3)
     * boosting: 1
     *
     * indexing: segments
     * boosting: 1
     */
    var tags: List<String> = emptyList()
) : IdInterface {
    companion object {
        fun ProcessedDocument.fullDocument(): String {
            return """${title}
                |${latinTitle}
                |
                |$artist
                |${artistsList.joinToString(" ")}
                |
                |$illustrator
                |${illustratorsList.joinToString(" ")}
                |
                |$charter
                |${chartersList.joinToString(" ")}
                |
                |${tags.joinToString(" ")}
            """.trimMargin()
        }
    }
}
