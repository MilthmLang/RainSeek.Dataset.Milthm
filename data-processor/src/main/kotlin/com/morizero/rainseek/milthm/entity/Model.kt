package com.morizero.rainseek.milthm.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar

interface TokenEntity : Entity<TokenEntity> {
    companion object : Entity.Factory<TokenEntity>() {
        fun generateTable(tableName: String): TokenEntityTable {
            return TokenEntityTable(tableName)
        }
    }

    var id: Long
    var content: String
}

class TokenEntityTable(tableName: String) : Table<TokenEntity>(tableName) {
    val id = long("id").primaryKey().bindTo { it.id }
    val content = varchar("name").bindTo { it.content }
}

interface TokensDocumentsEntity : Entity<TokensDocumentsEntity> {
    companion object : Entity.Factory<TokensDocumentsEntity>() {
        fun generateTable(tableName: String): TokensDocumentsEntityTable {
            return TokensDocumentsEntityTable(tableName)
        }
    }

    var id: Long
    var tokenId: Long
    var documentId: String
    var startPosition: Int
    var endPosition: Int
}

class TokensDocumentsEntityTable(tableName: String) : Table<TokensDocumentsEntity>(tableName) {
    val id = long("id").primaryKey().bindTo { it.id }
    val tokenId = long("token_id").bindTo { it.tokenId }
    val documentId = varchar("document_id").bindTo { it.documentId }
    val startPosition = int("start_position").bindTo { it.startPosition }
    val endPosition = int("end_position").bindTo { it.endPosition }
}
