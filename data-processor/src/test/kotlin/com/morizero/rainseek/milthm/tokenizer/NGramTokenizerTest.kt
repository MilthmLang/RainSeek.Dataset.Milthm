package com.morizero.rainseek.milthm.tokenizer

import kotlin.test.Test
import kotlin.test.assertEquals

class NGramTokenizerTest {

    @Test
    fun tokenize_singleEmoji_withN1_keepsEmojiIntact() {
        val tokenizer = NGramTokenizer(n = 1)

        val result = tokenizer.tokenize("🙂")

        assertEquals(1, result.size)
        assertEquals("🙂", result[0].value)
        assertEquals(0, result[0].startPosition)
        assertEquals("🙂".length - 1, result[0].endPosition)
    }

    @Test
    fun tokenize_mixedAsciiAndEmoji_withN2_usesGraphemeBoundaries() {
        val tokenizer = NGramTokenizer(n = 2)

        val result = tokenizer.tokenize("a🙂b")

        assertEquals(2, result.size)

        assertEquals("a🙂", result[0].value)
        assertEquals(0, result[0].startPosition)
        assertEquals("a🙂".length - 1, result[0].endPosition)

        assertEquals("🙂b", result[1].value)
        assertEquals(1, result[1].startPosition)
        assertEquals("a🙂b".length - 1, result[1].endPosition)
    }

    @Test
    fun tokenize_zwjEmojiSequence_withN1_keepsFamilyEmojiAsSingleUnit() {
        val tokenizer = NGramTokenizer(n = 1)
        val family = "👨‍👩‍👧‍👦"

        val result = tokenizer.tokenize("${family}x")

        assertEquals(2, result.size)

        assertEquals(family, result[0].value)
        assertEquals(0, result[0].startPosition)
        assertEquals(family.length - 1, result[0].endPosition)

        assertEquals("x", result[1].value)
        assertEquals(family.length, result[1].startPosition)
        assertEquals(family.length, result[1].endPosition)
    }
}
