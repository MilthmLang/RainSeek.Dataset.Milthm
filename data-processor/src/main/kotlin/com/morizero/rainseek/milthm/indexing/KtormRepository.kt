package com.morizero.rainseek.milthm.indexing

import com.morizero.rainseek.milthm.entity.*
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.entity.*
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class KtormRepository(val db: Database, val indexName: String) : IndexRepository {
    val tokenEntityTableName = "${indexName}_tokens"

    val documentsTokensTableName = "${indexName}_documents_tokens"
    private val tokenCache = ConcurrentHashMap<String, TokenEntity>()
    private val documentTokensByTokenIdCache = ConcurrentHashMap<Long, List<DocumentTokenEntity>>()

    val createTokenEntityTable: String = """
            CREATE TABLE IF NOT EXISTS $tokenEntityTableName (
                id INTEGER PRIMARY KEY,
                content TEXT NOT NULL,
                UNIQUE(content)
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

    val createMigrationTable: String = """
            CREATE TABLE IF NOT EXISTS migrations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                key TEXT UNIQUE NOT NULL,
                value TEXT NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """.trimIndent()

    private fun tokensTable(): TokensTable {
        return TokenEntity.Companion.generateTable(tokenEntityTableName)
    }

    private fun documentsTokensTable(): DocumentsTokensTable {
        return DocumentTokenEntity.Companion.generateTable(documentsTokensTableName)
    }

    init {
        db.useConnection {
            it.createStatement().use { stmt ->
                stmt.execute(createMigrationTable)
            }
        }

        val versionRecord = db.sequenceOf(MigrationsTable).firstOrNull { it.key eq "version" }
        if (versionRecord == null) {
            val now = LocalDateTime.now()
            val versionRecordToInsert = MigrationEntity().apply {
                key = "version"
                value = "2"
                createdAt = now
                updatedAt = now
            }
            db.sequenceOf(MigrationsTable).add(versionRecordToInsert)
        } else {
            if (versionRecord.value.toInt() > 2) {
                throw IllegalArgumentException("database downgrade is not allowed")
            }
        }

        db.useConnection {
            it.createStatement().use { stmt ->
                stmt.execute(createTokenEntityTable)
                stmt.execute(createDocumentsTokensEntityTable)
            }
        }
    }

    override fun findTokenByContent(indexName: String, value: String): TokenEntity? {
        if (indexName != this.indexName) {
            throw IllegalArgumentException("index name '$indexName' mismatch")
        }
        tokenCache[value]?.let { return it }
        val token = db.sequenceOf(tokensTable()).firstOrNull { it.content eq value }
        if (token != null) {
            tokenCache[value] = token
        }
        return token
    }

    override fun addToken(indexName: String, value: String): TokenEntity {
        if (indexName != this.indexName) {
            throw IllegalArgumentException("index name '$indexName' mismatch")
        }
        tokenCache[value]?.let { return it }

        db.useConnection {
            it.prepareStatement("INSERT OR IGNORE INTO $tokenEntityTableName (content) VALUES (?)").use { stmt ->
                stmt.setString(1, value)
                stmt.executeUpdate()
            }
        }

        val ret = db.sequenceOf(tokensTable()).firstOrNull { it.content eq value }
            ?: throw IllegalArgumentException("failed to insert data")
        tokenCache[value] = ret
        return ret
    }

    override fun findDocumentTokenByTokenId(indexName: String, tokenId: Long): List<DocumentTokenEntity> {
        if (indexName != this.indexName) {
            throw IllegalArgumentException("index name '$indexName' mismatch")
        }
        documentTokensByTokenIdCache[tokenId]?.let { return it }
        val list = db.sequenceOf(documentsTokensTable()).filter { it.tokenId eq tokenId }.toList()
        documentTokensByTokenIdCache[tokenId] = list
        return list
    }

    override fun addDocumentToken(
        indexName: String, tokenId: Long, documentId: String, startPosition: Int, endPosition: Int
    ): DocumentTokenEntity {
        if (indexName != this.indexName) {
            throw IllegalArgumentException("index name '$indexName' mismatch")
        }
        db.useConnection {
            it.prepareStatement(
                "INSERT OR IGNORE INTO $documentsTokensTableName (token_id, document_id, start_position, end_position) VALUES (?, ?, ?, ?)"
            ).use { stmt ->
                stmt.setLong(1, tokenId)
                stmt.setString(2, documentId)
                stmt.setInt(3, startPosition)
                stmt.setInt(4, endPosition)
                stmt.executeUpdate()
            }
        }

        documentTokensByTokenIdCache.remove(tokenId)
        val ret = db.sequenceOf(documentsTokensTable()).firstOrNull {
            (it.tokenId eq tokenId) and (it.documentId eq documentId) and (it.startPosition eq startPosition) and (it.endPosition eq endPosition)
        } ?: throw IllegalArgumentException("failed to insert data")
        return ret
    }

    override fun ensureTokens(indexName: String, values: Set<String>): Map<String, TokenEntity> {
        if (indexName != this.indexName) {
            throw IllegalArgumentException("index name '$indexName' mismatch")
        }
        if (values.isEmpty()) {
            return emptyMap()
        }

        val result = LinkedHashMap<String, TokenEntity>(values.size)
        val missing = ArrayList<String>(values.size)
        for (value in values) {
            val cached = tokenCache[value]
            if (cached == null) {
                missing += value
            } else {
                result[value] = cached
            }
        }

        if (missing.isEmpty()) {
            return result
        }

        db.useConnection { connection ->
            connection.prepareStatement("INSERT OR IGNORE INTO $tokenEntityTableName (content) VALUES (?)").use { stmt ->
                for (value in missing) {
                    stmt.setString(1, value)
                    stmt.addBatch()
                }
                stmt.executeBatch()
            }
        }

        val table = tokensTable()
        for (chunk in missing.chunked(500)) {
            val entities = db.sequenceOf(table).filter { row -> row.content inList chunk }.toList()
            for (entity in entities) {
                tokenCache[entity.content] = entity
                result[entity.content] = entity
            }
        }

        return result
    }

    override fun addDocumentTokens(indexName: String, values: List<DocumentTokenInsert>) {
        if (indexName != this.indexName) {
            throw IllegalArgumentException("index name '$indexName' mismatch")
        }
        if (values.isEmpty()) {
            return
        }

        db.useConnection { connection ->
            connection.prepareStatement(
                "INSERT OR IGNORE INTO $documentsTokensTableName (token_id, document_id, start_position, end_position) VALUES (?, ?, ?, ?)"
            ).use { stmt ->
                for (value in values) {
                    stmt.setLong(1, value.tokenId)
                    stmt.setString(2, value.documentId)
                    stmt.setInt(3, value.startPosition)
                    stmt.setInt(4, value.endPosition)
                    stmt.addBatch()
                }
                stmt.executeBatch()
            }
        }

        values.mapTo(HashSet(values.size)) { it.tokenId }.forEach { tokenId ->
            documentTokensByTokenIdCache.remove(tokenId)
        }
    }
}
