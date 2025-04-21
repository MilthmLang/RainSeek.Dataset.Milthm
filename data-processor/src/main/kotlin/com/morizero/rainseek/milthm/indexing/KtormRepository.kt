package com.morizero.rainseek.milthm.indexing

import com.morizero.rainseek.milthm.entity.TokenEntity
import com.morizero.rainseek.milthm.entity.TokenEntityTable
import com.morizero.rainseek.milthm.entity.TokensDocumentsEntity
import com.morizero.rainseek.milthm.entity.TokensDocumentsEntityTable
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*

class KtormRepository(val db: Database, val indexName: String) : IndexRepository {
    val tokenEntityTableName = "${indexName}_tokens"

    val documentsTokensTableName = "${indexName}_documents_tokens"

    val createTokenEntityTable: String = """
            CREATE TABLE IF NOT EXISTS $tokenEntityTableName (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                UNIQUE(name)
            );
        """.trimIndent()

    val createDocumentsTokensEntityTable: String = """
            CREATE TABLE IF NOT EXISTS $documentsTokensTableName (
                id INTEGER PRIMARY KEY,
                token_id INTEGER NOT NULL,
                document_id TEXT NOT NULL,
                start_position INTEGER NOT NULL,
                end_position INTEGER NOT NULL,
                UNIQUE(token_id, document_id, start_position, end_position)
            );
        """.trimIndent()

    private fun tokensTable(): TokenEntityTable {
        return TokenEntity.Companion.generateTable(tokenEntityTableName)
    }

    private fun tokensDocumentsTable(): TokensDocumentsEntityTable {
        return TokensDocumentsEntity.Companion.generateTable(documentsTokensTableName)
    }

    init {
        db.useConnection {
            it.createStatement().use { stmt ->
                stmt.execute(createTokenEntityTable);
                stmt.execute(createDocumentsTokensEntityTable);
            }
        }
    }

    override fun findTokenByContent(indexName: String, value: String): TokenEntity? {
        if (indexName != this.indexName) {
            throw IllegalArgumentException("index name '$indexName' mismatch")
        }
        return db.sequenceOf(tokensTable()).firstOrNull { it.content eq value }
    }

    override fun addToken(indexName: String, value: String): TokenEntity {
        if (indexName != this.indexName) {
            throw IllegalArgumentException("index name '$indexName' mismatch")
        }
        val table = db.sequenceOf(tokensTable())

        val token = TokenEntity.Companion()
        token.content = value
        table.add(token)

        val ret = table.firstOrNull { it.content eq value } ?: throw IllegalArgumentException("failed to insert data")
        return ret
    }

    override fun findTokenDocumentByTokenId(indexName: String, tokenId: Long): List<TokensDocumentsEntity> {
        if (indexName != this.indexName) {
            throw IllegalArgumentException("index name '$indexName' mismatch")
        }
        return db.sequenceOf(tokensDocumentsTable()).filter { it.tokenId eq tokenId }.toList()
    }

    override fun addTokenDocument(
        indexName: String, tokenId: Long, documentId: String, startPosition: Int, endPosition: Int
    ): TokensDocumentsEntity {
        if (indexName != this.indexName) {
            throw IllegalArgumentException("index name '$indexName' mismatch")
        }
        val table = db.sequenceOf(tokensDocumentsTable())

        val documentToken = TokensDocumentsEntity.Companion()
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