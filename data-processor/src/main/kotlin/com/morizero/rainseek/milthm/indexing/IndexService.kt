package com.morizero.rainseek.milthm.indexing

import com.morizero.rainseek.milthm.model.SearchResult
import com.morizero.rainseek.milthm.model.TokenModel
import com.morizero.rainseek.milthm.tokenizer.Tokenizer

class IndexService(
    val indexName: String,
    val tokenizers: List<Tokenizer>,
    val repository: IndexRepository,
) {

    constructor(indexName: String, tokenizer: Tokenizer, repository: IndexRepository) : this(
        indexName, listOf(tokenizer), repository
    )

    private fun tokenize(content: String): List<TokenModel> {
        val tokens = mutableListOf<TokenModel>()
        for (tokenizer in tokenizers) {
            tokens.addAll(tokenizer.tokenize(content))
        }
        return tokens
    }

    fun addDocument(documentId: String, content: List<String>) {
        val sb = StringBuilder(64)
        for (item in content) {
            sb.append(item).append("\n")
        }
        addDocument(documentId, sb.toString())
    }

    fun addDocument(documentId: String, content: String) {
        val tokens = tokenize(content)
        linkDocumentToToken(documentId, tokens)
    }

    private fun linkDocumentToToken(documentId: String, tokens: List<TokenModel>) {
        if (tokens.isEmpty()) {
            return
        }

        val tokenEntities = repository.ensureTokens(indexName, tokens.map { it.value }.toSet())
        val values = ArrayList<DocumentTokenInsert>(tokens.size)
        for (token in tokens) {
            val tokenEntity =
                tokenEntities[token.value] ?: throw IllegalStateException("failed to resolve token '${token.value}'")
            values += DocumentTokenInsert(
                tokenId = tokenEntity.id,
                documentId = documentId,
                startPosition = token.startPosition,
                endPosition = token.endPosition,
            )
        }

        repository.addDocumentTokens(indexName, values)
    }

    fun search(query: String): List<SearchResult> {
        val tokens = tokenize(query)

        val result = mutableMapOf<String, SearchResult>()

        for (token in tokens) {
            val tokenEntity = repository.findTokenByContent(indexName, token.value) ?: continue

            val documentsToken = repository.findDocumentTokenByTokenId(indexName, tokenEntity.id)

            documentsToken.forEach {

                val tokenModel = TokenModel(
                    value = tokenEntity.content, startPosition = it.startPosition, endPosition = it.endPosition
                )

                if (!result.containsKey(it.documentId)) {
                    result[it.documentId] = SearchResult(
                        documentId = it.documentId, matchedToken = mutableListOf(tokenModel)
                    )
                } else {
                    result[it.documentId]!!.matchedToken += tokenModel
                }
            }
        }

        return result.values.toList()
    }
}
