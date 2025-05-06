package com.morizero.rainseek.milthm.tokenizer

import com.ibm.icu.text.BreakIterator
import com.ibm.icu.util.ULocale

enum class BreakIteratorMode(val factory: (ULocale) -> BreakIterator) {
    CHARACTER(fun(locale: ULocale): BreakIterator {
        return BreakIterator.getCharacterInstance(locale);
    }),

    WORD(fun(locale: ULocale): BreakIterator {
        return BreakIterator.getWordInstance(locale);
    });

    operator fun invoke(locale: ULocale): BreakIterator = factory(locale)
}
