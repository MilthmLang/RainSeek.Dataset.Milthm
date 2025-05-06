package com.morizero.rainseek.milthm.tokenizer

import com.ibm.icu.text.BreakIterator
import com.ibm.icu.text.CaseMap
import com.ibm.icu.text.Normalizer2
import com.ibm.icu.util.ULocale
import com.morizero.rainseek.milthm.model.TokenModel

class IcuBreakIteratorTokenizer(
    val locale: ULocale,
    val predictor: (token: TokenModel) -> Boolean = { true },
    val mode: BreakIteratorMode = BreakIteratorMode.WORD,
) : Tokenizer {
    val normalizer = Normalizer2.getInstance(null, "nfkc", Normalizer2.Mode.COMPOSE)

    private fun normalize(input: String): String {
        val normalizedText = normalizer.normalize(input)
        return CaseMap.toLower().apply(locale.toLocale(), normalizedText)
    }

    override fun tokenize(input: String): List<TokenModel> {
        val normalizedInput = normalize(input)

        val wordIterator = mode(locale)
        wordIterator.setText(normalizedInput)

        val tokenList = mutableListOf<TokenModel>()

        var start = wordIterator.first()
        while (true) {
            val end = wordIterator.next()
            if (end == BreakIterator.DONE) break

            val word = normalizedInput.substring(start, end)

            val tokenModel = TokenModel(word, start, end)
            if (word.isNotBlank() && predictor(tokenModel)) {
                tokenList.add(tokenModel)
            }

            start = end
        }
        return tokenList
    }
}
