package com.morizero.rainseek.milthm.utils

import com.morizero.rainseek.milthm.model.IdInterface
import java.util.concurrent.ConcurrentHashMap

class MapIdObject<V> : MutableMap<String, V> where V : IdInterface {
    val underlayMap: ConcurrentHashMap<String, V> = ConcurrentHashMap()
    val accessedFlag: ConcurrentHashMap<String, Boolean> = ConcurrentHashMap()

    override val entries: MutableSet<MutableMap.MutableEntry<String, V>>
        get() = underlayMap.entries
    override val keys: MutableSet<String>
        get() = underlayMap.keys
    override val size: Int
        get() = underlayMap.size
    override val values: MutableCollection<V>
        get() = underlayMap.values

    override fun clear() {
        underlayMap.clear()
        accessedFlag.clear()
    }

    fun clearAccessHistory() {
        accessedFlag.clear()
    }

    override fun isEmpty(): Boolean {
        return underlayMap.isEmpty()
    }

    override fun remove(key: String): V? {
        accessedFlag.remove(key)
        return underlayMap.remove(key)
    }

    override fun putAll(from: Map<out String, V>) {
        underlayMap.putAll(from)
        accessedFlag.putAll(from.entries.associate { it.key to false })
    }

    override fun put(key: String, value: V): V? {
        if (accessedFlag.containsKey(key)) {
            throw IllegalStateException("Duplicate key '$key'")
        }
        accessedFlag[key] = false
        underlayMap[key] = value
        return value
    }

    fun add(value: V): V? {
        return put(value.id, value)
    }

    fun addAll(values: List<V>) {
        values.forEach { put(it.id, it) }
    }

    fun remove(value: V): V? {
        return remove(value.id)
    }

    override fun get(key: String): V? {
        accessedFlag[key] = true
        return underlayMap[key]
    }

    override fun containsValue(value: V): Boolean {
        return underlayMap.containsValue(value)
    }

    override fun containsKey(key: String): Boolean {
        return underlayMap.containsKey(key)
    }

    val notAccessedKeys: Set<String>
        get() {
            val notAccessedKeys = mutableSetOf<String>()
            underlayMap.forEach { (k, v) ->
                if (accessedFlag[k] == false) {
                    notAccessedKeys.add(k)
                }
            }
            return notAccessedKeys
        }
}
