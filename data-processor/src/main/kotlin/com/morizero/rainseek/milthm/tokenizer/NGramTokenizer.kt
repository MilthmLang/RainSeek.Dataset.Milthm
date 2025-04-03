package com.morizero.rainseek.milthm.tokenizer

class NGramTokenizer(
    private val n: Int = 3,
    private val caseSensitive: Boolean = false,
    private val delimiters: List<String> = listOf(" ")
) : Tokenizer {

    override fun tokenize(input: String): List<TokenModel> {
        val basicTokenizer = BasicTokenizer(delimiters, caseSensitive)
        val basicTokens = basicTokenizer.tokenize(input)
        val nGrams = mutableListOf<TokenModel>()

        for (token in basicTokens) {
            val tokenValue = token.value
            for (i in 0..(tokenValue.length - n)) {
                val nGramValue = tokenValue.substring(i, i + n)
                nGrams.add(
                    TokenModel(
                        value = nGramValue,
                        startPosition = token.startPosition + i,
                        endPosition = token.startPosition + i + n - 1
                    )
                )
            }
        }

        return nGrams
    }
}