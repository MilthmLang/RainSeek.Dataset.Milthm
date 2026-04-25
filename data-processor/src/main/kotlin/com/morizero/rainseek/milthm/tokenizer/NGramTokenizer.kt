package com.morizero.rainseek.milthm.tokenizer

import com.ibm.icu.text.BreakIterator
import com.ibm.icu.util.ULocale
import com.morizero.rainseek.milthm.model.TokenModel

class NGramTokenizer(
    private val n: Int = 3,
    private val caseSensitive: Boolean = false,
    private val delimiters: List<String> = listOf(" ")
) : Tokenizer {

    override fun tokenize(input: String): List<TokenModel> {
        val basicTokenizer = BasicTokenizer(delimiters, caseSensitive)
        val basicTokens = basicTokenizer.tokenize(input)
        val nGrams = mutableListOf<TokenModel>()
        val charIterator = BreakIterator.getCharacterInstance(ULocale.ROOT)

        for (token in basicTokens) {
            val tokenValue = token.value
            charIterator.setText(tokenValue)

            val boundaries = mutableListOf<Int>()
            var boundary = charIterator.first()
            while (boundary != BreakIterator.DONE) {
                boundaries += boundary
                boundary = charIterator.next()
            }

            val graphemeCount = boundaries.size - 1
            if (graphemeCount < n) {
                continue
            }

            for (i in 0..(graphemeCount - n)) {
                val startInToken = boundaries[i]
                val endExclusiveInToken = boundaries[i + n]
                val nGramValue = tokenValue.substring(startInToken, endExclusiveInToken)
                nGrams.add(
                    TokenModel(
                        value = nGramValue,
                        startPosition = token.startPosition + startInToken,
                        endPosition = token.startPosition + endExclusiveInToken - 1
                    )
                )
            }
        }

        return nGrams
    }
}
