package com.morizero.rainseek.milthm.tokenizer

import com.morizero.rainseek.milthm.model.TokenModel

interface Tokenizer {
    fun tokenize(input: String): List<TokenModel>
}
