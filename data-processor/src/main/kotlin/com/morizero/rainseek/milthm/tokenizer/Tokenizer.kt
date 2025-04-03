package com.morizero.rainseek.milthm.tokenizer

interface Tokenizer {
    fun tokenize(input: String): List<TokenModel>
}
