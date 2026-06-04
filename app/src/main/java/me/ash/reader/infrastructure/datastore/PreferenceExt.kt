package me.ash.reader.infrastructure.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey

@Suppress(names = ["UNCHECKED_CAST"])
inline fun <reified T> Preferences.get(key: String): T? {
    val preferenceKey = when (T::class) {
        Int::class -> intPreferencesKey(key)
        String::class -> stringPreferencesKey(key)
        Boolean::class -> booleanPreferencesKey(key)
        Float::class -> floatPreferencesKey(key)
        Long::class -> longPreferencesKey(key)
        Double::class -> doublePreferencesKey(key)
        else -> throw IllegalArgumentException("Unsupported type: ${T::class.java.name}")
    }
    return this[preferenceKey as Preferences.Key<T>]
}

inline fun <reified T> Preferences.getOrDefault(key: String, default: T) = get(key) ?: default