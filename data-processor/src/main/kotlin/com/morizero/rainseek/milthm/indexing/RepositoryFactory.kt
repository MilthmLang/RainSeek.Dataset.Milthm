package com.morizero.rainseek.milthm.indexing

import java.util.concurrent.ConcurrentHashMap

class RepositoryFactory(val factory: (indexName: String) -> IndexRepository) {
    private val repositories = ConcurrentHashMap<String, IndexRepository>()

    operator fun invoke(indexName: String): IndexRepository {
        return repositories.getOrPut(indexName, fun(): IndexRepository {
            return factory(indexName)
        })
    }
}
