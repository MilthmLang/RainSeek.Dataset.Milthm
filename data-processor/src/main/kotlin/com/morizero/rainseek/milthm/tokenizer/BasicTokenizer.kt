package com.morizero.rainseek.milthm.tokenizer

import com.morizero.rainseek.milthm.model.TokenModel

class BasicTokenizer(
    val delimiters: List<String> = listOf(" "),
    val caseSensitive: Boolean = false,
    val predictor: (token: TokenModel) -> Boolean = { true },
) : Tokenizer {
    override fun tokenize(input: String): List<TokenModel> {
        val tokens = mutableListOf<TokenModel>()
        var index = 0

        while (index < input.length) {
            var isDelimiter = false
            for (delimiter in delimiters) {
                if (input.substring(index).startsWith(delimiter)) {
                    index += delimiter.length
                    isDelimiter = true
                    break
                }
            }

            if (isDelimiter) continue

            val start = index
            while (index < input.length) {
                var atDelimiter = false
                for (delimiter in delimiters) {
                    if (input.substring(index).startsWith(delimiter)) {
                        atDelimiter = true
                        break
                    }
                }

                if (atDelimiter) break
                index++
            }

            val end = index - 1
            var value = input.substring(start, index)

            if (!caseSensitive) {
                value = value.lowercase()
            }

            val tokenModel = TokenModel(
                value = value, startPosition = start, endPosition = end
            )
            if (predictor(tokenModel)) {
                tokens.add(tokenModel)
            }
        }

        return tokens
    }
}
