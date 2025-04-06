package com.morizero.rainseek.milthm


import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kotlinx.serialization.Serializable

/*
 * =================================
 * /songs/XXXXXX.txt            保存歌曲及相关的信息（标题，作者，和作者相关的一些关联信息，和歌曲相关的一些关联信息）
 * /chart/XXXXXX.txt            谱的一些信息（bpm，啥的）
 * /illustration/XXXXX.txt      保存插画的信息
 * /people/XXXXXX.txt           保存作者的信息
 * =================================
 */

@Serializable
data class ProcessedDocument(
    var id: String,
    var title: String,
    var titleCulture: String = "",
    var latinTitle: String = "",
    @JsonDeserialize(using = ArtistDeserializer::class)
    var artist: List<String> =emptyList(),
    var illustrator: String,
    var illustration: String = "",
    var illustrationTag: List<String> = emptyList(),
    var squareArtwork: String = "",
    var bpmInfo: List<BPMData> = emptyList(),
    var songId: String = "",
    var difficulty: String = "",
    var difficultyValue: Double = 0.0,
    var charter: String = "",
    var chartId: String = "",
    var tags: List<String> = emptyList()
)

@Serializable
data class BPMData(
    var start: Float,
    var bpm: Float,
    var beatsPerBar: Int
)

@Serializable
data class People(
    var id: String,
    var name: String = "",
    var description: String = "",
    var tags: List<String> = emptyList()
)

data class Song(
    var id: String = "",
    var title: String = "",
    var titleCulture: String = "",
    val latinTitle: String = "",
    @JsonDeserialize(using = ArtistDeserializer::class)
    val artist: List<String> = emptyList(),
    var tags: List<String> = emptyList()

)

data class Chart(
    var id: String = "",
    var illustration: String = "",
    var illustrationSquare: String = "",
    var bpmInfo: List<BPMData> = emptyList(),
    var songId: String = "",
    var difficulty: String = "",
    var difficultyValue: Double = 0.0,
    var charter: String = "",
    var charterRefs: List<String> = emptyList(),
    var chartId: String = "",
    var tags: List<String> = emptyList()
)

data class Illustration(
    var id: String = "",
    var illustrator: String = "",
    var description: String = "",
    var tags: List<String> = emptyList()
)