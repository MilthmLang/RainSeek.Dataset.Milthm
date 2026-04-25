package com.morizero.rainseek.milthm.indexing

import com.morizero.rainseek.milthm.entity.DocumentTokenEntity
import com.morizero.rainseek.milthm.entity.TokenEntity

data class DocumentTokenInsert(
    val tokenId: Long,
    val documentId: String,
    val startPosition: Int,
    val endPosition: Int,
)

interface IndexRepository {
    fun findTokenByContent(indexName: String, value: String): TokenEntity?

    fun addToken(indexName: String, value: String): TokenEntity

    fun findDocumentTokenByTokenId(indexName: String, tokenId: Long): List<DocumentTokenEntity>

    fun addDocumentToken(
        indexName: String, tokenId: Long, documentId: String, startPosition: Int, endPosition: Int
    ): DocumentTokenEntity

    fun ensureTokens(indexName: String, values: Set<String>): Map<String, TokenEntity> {
        val result = LinkedHashMap<String, TokenEntity>(values.size)
        for (value in values) {
            val token = findTokenByContent(indexName, value) ?: addToken(indexName, value)
            result[value] = token
        }
        return result
    }

    fun addDocumentTokens(indexName: String, values: List<DocumentTokenInsert>) {
        for (value in values) {
            try {
                addDocumentToken(indexName, value.tokenId, value.documentId, value.startPosition, value.endPosition)
            } catch (e: org.sqlite.SQLiteException) {
                if (e.resultCode.code != 2067) {
                    throw e
                }
            }
        }
    }
}
