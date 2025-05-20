package com.morizero.rainseek.milthm.tokenizer

import com.github.houbb.opencc4j.util.ZhConverterUtil
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum
import com.github.houbb.pinyin.util.PinyinHelper
import com.ibm.icu.util.ULocale
import com.morizero.rainseek.milthm.model.TokenModel


class MultiLanguageTokenizer(
    cultureListStr: String,
    val predictor: (token: TokenModel) -> Boolean = { true },
    val mode: BreakIteratorMode = BreakIteratorMode.WORD,
) : Tokenizer {
    val cultureList: List<String> = cultureListStr.split(":")

    val icuTokenizer: MutableMap<String, IcuBreakIteratorTokenizer> = cultureList.associateWith { culture ->
        IcuBreakIteratorTokenizer(
            locale = ULocale.forLanguageTag(culture),
            predictor = predictor,
            mode = mode,
        )
    }.toMutableMap()

    init {
        if (!icuTokenizer.contains("ja-JP")) {
            icuTokenizer["ja-JP"] = IcuBreakIteratorTokenizer(
                locale = ULocale.forLanguageTag("ja-JP"),
                predictor = predictor,
                mode = mode,
            )
        }
    }

    override fun tokenize(input: String): List<TokenModel> {
        val ret = ArrayList<TokenModel>()

        ret += icu(input)
        ret += openCCAndPinyin(input)

        return ret.distinct()
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

        result.forEach { it ->
            val pinyin = PinyinHelper.toPinyin(it.value, PinyinStyleEnum.NORMAL).split(" ")
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

        return ret
    }
}
