package com.morizero.rainseek.milthm.indexing

import com.morizero.rainseek.milthm.entity.TokenEntity
import com.morizero.rainseek.milthm.entity.DocumentTokenEntity

interface IndexRepository {
    fun findTokenByContent(indexName: String, value: String): TokenEntity?

    fun addToken(indexName: String, value: String): TokenEntity

    fun findDocumentTokenByTokenId(indexName: String, tokenId: Long): List<DocumentTokenEntity>

    fun addDocumentToken(
        indexName: String, tokenId: Long, documentId: String, startPosition: Int, endPosition: Int
    ): DocumentTokenEntity
}
