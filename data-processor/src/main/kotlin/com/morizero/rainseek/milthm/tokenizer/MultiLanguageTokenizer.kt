package com.morizero.rainseek.milthm.tokenizer

import com.github.houbb.opencc4j.util.ZhConverterUtil
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum
import com.github.houbb.pinyin.util.PinyinHelper
import com.ibm.icu.text.CaseMap
import com.ibm.icu.text.Transliterator
import com.ibm.icu.util.ULocale
import com.morizero.rainseek.milthm.model.TokenModel
import org.apache.lucene.analysis.ja.JapaneseTokenizer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute
import java.io.StringReader


class MultiLanguageTokenizer(
    cultureListStr: String,
    val predictor: (token: TokenModel) -> Boolean = { true },
    val mode: BreakIteratorMode = BreakIteratorMode.WORD,
) : Tokenizer {
    companion object {
        val transliterator =
            Transliterator.getInstance("Han-Latin; Latin-ASCII; NFD; [:Nonspacing Mark:] Remove; NFC")

        val pinyinProviders = listOf<(String) -> String>(
            { s -> transliterator.transliterate(s) },
            { s -> PinyinHelper.toPinyin(s, PinyinStyleEnum.NORMAL) },
        )

        val jpRomanTrans =
            Transliterator.getInstance("Han-Latin; Hiragana-Latin; Katakana-Latin; Latin-ASCII; NFD; [:Nonspacing Mark:] Remove; NFC")
    }

    val cultureList: List<String> = cultureListStr.split(":")

    val icuTokenizer: MutableMap<String, IcuBreakIteratorTokenizer> = cultureList.associateWith { culture ->
        IcuBreakIteratorTokenizer(
            locale = ULocale.forLanguageTag(culture),
            predictor = predictor,
            mode = mode,
        )
    }.toMutableMap()

    init {
//        if (!icuTokenizer.contains("ja-JP")) {
//            icuTokenizer["ja-JP"] = IcuBreakIteratorTokenizer(
//                locale = ULocale.forLanguageTag("ja-JP"),
//                predictor = predictor,
//                mode = mode,
//            )
//        }
    }

    override fun tokenize(input: String): List<TokenModel> {
        val ret = ArrayList<TokenModel>()

        ret += icu(input)
        ret += openCCAndPinyin(input)
        ret += luceneJP(input)

        return ret.map {
            it.value = CaseMap.toLower().apply(ULocale.ENGLISH.toLocale(), it.value)
            it
        }.distinct()
    }

    private fun luceneJP(input: String): List<TokenModel> {
        val reader = StringReader(input)
        val jpTokenizer: JapaneseTokenizer = JapaneseTokenizer(null, false, JapaneseTokenizer.Mode.NORMAL)
        jpTokenizer.setReader(reader)
        jpTokenizer.reset()

        val termAttr = jpTokenizer.getAttribute(CharTermAttribute::class.java)
        val offsetAttr = jpTokenizer.getAttribute(OffsetAttribute::class.java)

        val ret = mutableListOf<TokenModel>()
        while (jpTokenizer.incrementToken()) {
            val value = termAttr.toString()
            if (!isJapanese(value)) {
                continue
            }
            val start = offsetAttr.startOffset()
            val end = offsetAttr.endOffset()
            val baseToken = TokenModel(value = value, startPosition = start, endPosition = end)
            if (predictor(baseToken)) {
                ret += baseToken
                val romaji = jpRomanTrans.transliterate(value)
                ret += TokenModel(
                    value = romaji,
                    startPosition = start,
                    endPosition = end
                )
            }
        }
        jpTokenizer.end()
        jpTokenizer.close()
        reader.close()

        return ret
    }

    private fun icu(input: String): List<TokenModel> {
        val ret = ArrayList<TokenModel>()
        cultureList.forEach { culture ->
            val t = icuTokenizer[culture]!!
            ret.addAll(t.tokenize(input))
        }
        return ret
    }

    private fun openCCAndPinyin(input: String): List<TokenModel> {
        val o = ArrayList<TokenModel>()

        if (ZhConverterUtil.containsSimple(input)) {
            val newInput = ZhConverterUtil.toTraditional(input)
            val result = icu(newInput).filter { ZhConverterUtil.containsChinese(it.value) }
            o += result
        }
        if (ZhConverterUtil.containsTraditional(input)) {
            val newInput = ZhConverterUtil.toSimple(input)
            val result = icu(newInput).filter { ZhConverterUtil.containsChinese(it.value) }
            o += result
        }

        val result = o.distinct()
        val ret = mutableListOf<TokenModel>().apply { addAll(result) }.filter { ZhConverterUtil.isChinese(it.value) }
            .toMutableList()

        pinyinProviders.forEach { provider ->
            result.forEach { it ->
                val pinyin = provider(it.value).split(" ")

                if (!isAllEnglishLettersAndDigits(pinyin)) {
                    return@forEach
                }

                var i = it.startPosition
                while (i < it.endPosition) {
                    val m = TokenModel(
                        value = pinyin[i - it.startPosition],
                        startPosition = i,
                        endPosition = i + 1,
                    )
                    ret.add(m)
                    i++
                }

                ret.add(
                    TokenModel(
                        value = pinyin.joinToString(separator = ""),
                        startPosition = it.startPosition,
                        endPosition = it.endPosition,
                    )
                )
            }
        }

        return ret.distinct()
    }

    private fun isJapanese(text: String): Boolean {
        return text.all { ch ->
            when (Character.UnicodeBlock.of(ch)) {
                Character.UnicodeBlock.HIRAGANA,
                Character.UnicodeBlock.KATAKANA,
                Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
                Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
                Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B -> true

                else -> false
            }
        }
    }

    fun isAllEnglishLettersAndDigits(pinyin: List<String>): Boolean {
        return pinyin.all { token ->
            token.all { ch ->
                (ch in 'a'..'z') || (ch in 'A'..'Z') || (ch in '0'..'9')
            }
        }
    }
}
