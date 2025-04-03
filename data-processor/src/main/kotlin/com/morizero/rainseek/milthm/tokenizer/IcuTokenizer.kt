package com.morizero.rainseek.milthm.tokenizer

import com.ibm.icu.text.BreakIterator
import com.ibm.icu.util.ULocale

class IcuTokenizer(val locale: ULocale) : Tokenizer {
    override fun tokenize(input: String): List<TokenModel> {
        val wordIterator = BreakIterator.getWordInstance(locale)
        wordIterator.setText(input)

        val tokenList = mutableListOf<TokenModel>()

        var start = wordIterator.first()
        while (true) {
            val end = wordIterator.next()
            if (end == BreakIterator.DONE) break

            val word = input.substring(start, end)

            if (word.isNotBlank()) {
                tokenList.add(TokenModel(word, start, end))
            }

            start = end
        }
        return tokenList
    }
}
