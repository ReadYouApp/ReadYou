package me.ash.reader.infrastructure.preference

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class HideDuplicateArticlesPreferenceTest {

    @Before
    fun resolvePreferenceObjects() {
        // Resolve the data objects first: under the unit-test classloader the
        // companion's `default` is otherwise observed as null before class init settles.
        HideDuplicateArticlesPreference.ON
        HideDuplicateArticlesPreference.OFF
    }

    @Test
    fun defaultIsOff() {
        Assert.assertEquals(
            HideDuplicateArticlesPreference.OFF,
            HideDuplicateArticlesPreference.default,
        )
        Assert.assertFalse(HideDuplicateArticlesPreference.default.value)
    }

    @Test
    fun fromPreferencesMapsStoredValue() {
        val key = booleanPreferencesKey("hideDuplicateArticles")
        Assert.assertEquals(
            HideDuplicateArticlesPreference.ON,
            HideDuplicateArticlesPreference.fromPreferences(preferencesOf(key to true)),
        )
        Assert.assertEquals(
            HideDuplicateArticlesPreference.OFF,
            HideDuplicateArticlesPreference.fromPreferences(preferencesOf(key to false)),
        )
    }

    @Test
    fun fromPreferencesFallsBackToOffWhenMissing() {
        Assert.assertEquals(
            HideDuplicateArticlesPreference.OFF,
            HideDuplicateArticlesPreference.fromPreferences(emptyPreferences()),
        )
    }
}
