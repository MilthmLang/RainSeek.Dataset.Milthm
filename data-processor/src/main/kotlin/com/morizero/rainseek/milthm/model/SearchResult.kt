package com.morizero.rainseek.milthm.model

data class SearchResult(
    var documentId: String,
    var matchedToken: List<TokenModel>
)