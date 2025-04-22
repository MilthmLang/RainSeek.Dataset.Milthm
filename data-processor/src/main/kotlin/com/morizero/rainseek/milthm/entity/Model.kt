package com.morizero.rainseek.milthm.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.datetime
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar

interface TokenEntity : Entity<TokenEntity> {
    companion object : Entity.Factory<TokenEntity>() {
        fun generateTable(tableName: String): TokensTable {
            return TokensTable(tableName)
        }
    }

    var id: Long
    var content: String
}

class TokensTable(tableName: String) : Table<TokenEntity>(tableName) {
    val id = long("id").primaryKey().bindTo { it.id }
    val content = varchar("content").bindTo { it.content }
}

interface DocumentTokenEntity : Entity<DocumentTokenEntity> {
    companion object : Entity.Factory<DocumentTokenEntity>() {
        fun generateTable(tableName: String): DocumentsTokensTable {
            return DocumentsTokensTable(tableName)
        }
    }

    var id: Long
    var tokenId: Long
    var documentId: String
    var startPosition: Int
    var endPosition: Int
}

class DocumentsTokensTable(tableName: String) : Table<DocumentTokenEntity>(tableName) {
    val id = long("id").primaryKey().bindTo { it.id }
    val tokenId = long("token_id").bindTo { it.tokenId }
    val documentId = varchar("document_id").bindTo { it.documentId }
    val startPosition = int("start_position").bindTo { it.startPosition }
    val endPosition = int("end_position").bindTo { it.endPosition }
}

interface MigrationEntity : Entity<MigrationEntity> {
    companion object : Entity.Factory<MigrationEntity>()
    val id: Int
    var key: String
    var value: String
    var createdAt: java.time.LocalDateTime
    var updatedAt: java.time.LocalDateTime
}

object MigrationsTable : Table<MigrationEntity>("migrations") {
    val id = int("id").primaryKey().bindTo { it.id }
    val key = varchar("key").bindTo { it.key }
    val value = varchar("value").bindTo { it.value }
    val createdAt = datetime("created_at").bindTo { it.createdAt }
    val updatedAt = datetime("updated_at").bindTo { it.updatedAt }
}
