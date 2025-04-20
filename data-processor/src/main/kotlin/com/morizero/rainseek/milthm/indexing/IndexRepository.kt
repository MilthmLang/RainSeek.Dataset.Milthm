package com.morizero.rainseek.milthm.indexing

import com.morizero.rainseek.milthm.entity.TokenEntity
import com.morizero.rainseek.milthm.entity.TokenEntityTable
import com.morizero.rainseek.milthm.entity.TokensDocumentsEntity
import com.morizero.rainseek.milthm.entity.TokensDocumentsEntityTable
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*

class IndexRepository(val db: Database) {
    private fun tokensTable(indexName: String): TokenEntityTable {
        return TokenEntity.generateTable("${indexName}_tokens")
    }

    private fun tokensDocumentsTable(indexName: String): TokensDocumentsEntityTable {
        return TokensDocumentsEntity.generateTable("${indexName}_documents_tokens")
    }

    fun findTokenByContent(indexName: String, value: String): TokenEntity? {
        return db.sequenceOf(tokensTable(indexName)).firstOrNull { it.content eq value }
    }

    fun addToken(indexName: String, value: String): TokenEntity {
        val table = db.sequenceOf(tokensTable(indexName))

        val token = TokenEntity()
        token.content = value
        table.add(token)

        val ret = table.firstOrNull { it.content eq value } ?: throw IllegalArgumentException("failed to insert data")
        return ret
    }

    fun findTokenDocumentByTokenId(indexName: String, tokenId: Long): List<TokensDocumentsEntity> {
        return db.sequenceOf(tokensDocumentsTable(indexName)).filter { it.tokenId eq tokenId }.toList()
    }

    fun addTokenDocument(
        indexName: String, tokenId: Long, documentId: String, startPosition: Int, endPosition: Int
    ): TokensDocumentsEntity {
        val table = db.sequenceOf(tokensDocumentsTable(indexName))

        val documentToken = TokensDocumentsEntity()
        documentToken.tokenId = tokenId
        documentToken.documentId = documentId
        documentToken.startPosition = startPosition
        documentToken.endPosition = endPosition

        table.add(documentToken)
        val ret = table.firstOrNull {
            (it.tokenId eq tokenId) and (it.documentId eq documentId) and (it.startPosition eq startPosition) and (it.endPosition eq endPosition)
        } ?: throw IllegalArgumentException("failed to insert data")
        return ret
    }

}