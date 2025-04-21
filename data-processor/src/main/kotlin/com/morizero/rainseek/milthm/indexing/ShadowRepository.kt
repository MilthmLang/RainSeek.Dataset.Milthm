package com.morizero.rainseek.milthm.indexing

import com.morizero.rainseek.milthm.entity.TokenEntity
import com.morizero.rainseek.milthm.entity.DocumentTokenEntity

class ShadowRepository(val repository: RepositoryFactory) : IndexRepository {

    override fun findTokenByContent(
        indexName: String, value: String
    ): TokenEntity? {
        return repository(indexName).findTokenByContent(indexName, value)
    }

    override fun addToken(
        indexName: String, value: String
    ): TokenEntity {
        return repository(indexName).addToken(indexName, value)
    }

    override fun findDocumentTokenByTokenId(
        indexName: String, tokenId: Long
    ): List<DocumentTokenEntity> {
        return repository(indexName).findDocumentTokenByTokenId(indexName, tokenId)
    }

    override fun addDocumentToken(
        indexName: String, tokenId: Long, documentId: String, startPosition: Int, endPosition: Int
    ): DocumentTokenEntity {
        return repository(indexName).addDocumentToken(indexName, tokenId, documentId, startPosition, endPosition)
    }
}
