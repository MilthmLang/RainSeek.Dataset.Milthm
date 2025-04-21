package com.morizero.rainseek.milthm.tokenizer

import com.morizero.rainseek.milthm.model.TokenModel

class LineTokenizer(
    caseSensitive: Boolean = false,
    predictor: (token: TokenModel) -> Boolean = { true },
) : Tokenizer {
    private val tokenizer = BasicTokenizer(
        delimiters = listOf("\r", "\n", "\r\n"),
        caseSensitive = caseSensitive,
        predictor = predictor,
    )

    override fun tokenize(input: String): List<TokenModel> {
        return tokenizer.tokenize(input)
    }
}