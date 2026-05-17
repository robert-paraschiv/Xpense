package com.rokudo.xpense.utils

object MapUtil {
    fun <K, V : Comparable<V>> sortByValue(map: Map<K, V>): Map<K, V> {
        return map.entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }
    }
}

