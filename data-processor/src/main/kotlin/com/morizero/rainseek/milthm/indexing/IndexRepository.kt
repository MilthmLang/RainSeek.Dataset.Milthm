package com.morizero.rainseek.milthm.indexing

import com.morizero.rainseek.milthm.entity.TokenEntity
import com.morizero.rainseek.milthm.entity.TokensDocumentsEntity

interface IndexRepository {
    fun findTokenByContent(indexName: String, value: String): TokenEntity?

    fun addToken(indexName: String, value: String): TokenEntity

    fun findTokenDocumentByTokenId(indexName: String, tokenId: Long): List<TokensDocumentsEntity>

    fun addTokenDocument(
        indexName: String, tokenId: Long, documentId: String, startPosition: Int, endPosition: Int
    ): TokensDocumentsEntity
}
