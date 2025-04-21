package com.morizero.rainseek.milthm.indexing

import com.morizero.rainseek.milthm.entity.TokenEntity
import com.morizero.rainseek.milthm.entity.TokensDocumentsEntity

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

    override fun findTokenDocumentByTokenId(
        indexName: String, tokenId: Long
    ): List<TokensDocumentsEntity> {
        return repository(indexName).findTokenDocumentByTokenId(indexName, tokenId)
    }

    override fun addTokenDocument(
        indexName: String, tokenId: Long, documentId: String, startPosition: Int, endPosition: Int
    ): TokensDocumentsEntity {
        return repository(indexName).addTokenDocument(indexName, tokenId, documentId, startPosition, endPosition)
    }
}