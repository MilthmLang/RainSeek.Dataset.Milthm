package com.morizero.rainseek.milthm.tokenizer

import com.ibm.icu.text.BreakIterator
import com.ibm.icu.text.CaseMap
import com.ibm.icu.text.Normalizer2
import com.ibm.icu.util.ULocale
import com.morizero.rainseek.milthm.model.TokenModel

class IcuTokenizer(val locale: ULocale, val ignoreList: List<String>) : Tokenizer {
    val normalizer = Normalizer2.getInstance(null, "nfkc", Normalizer2.Mode.COMPOSE)

    private fun normalize(input: String): String {
        val normalizedText = normalizer.normalize(input)
        return CaseMap.toLower().apply(locale.toLocale(), normalizedText)
    }

    override fun tokenize(input: String): List<TokenModel> {
        val normalizedInput = normalize(input)

        val wordIterator = BreakIterator.getWordInstance(locale)
        wordIterator.setText(normalizedInput)

        val tokenList = mutableListOf<TokenModel>()

        var start = wordIterator.first()
        while (true) {
            val end = wordIterator.next()
            if (end == BreakIterator.DONE) break

            val word = normalizedInput.substring(start, end)

            if (word.isNotBlank() && !ignoreList.contains(word)) {
                tokenList.add(TokenModel(word, start, end))
            }

            start = end
        }
        return tokenList
    }
}
