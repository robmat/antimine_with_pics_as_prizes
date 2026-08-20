package dev.lucasnlm.antimine.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class TestPreferenceManager : PreferencesManager {
    val values = mutableMapOf<String, Any>()

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean = values.getOrDefault(key, defaultValue) as Boolean

    override fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        values[key] = value
    }

    override fun getInt(
        key: String,
        defaultValue: Int,
    ): Int = values.getOrDefault(key, defaultValue) as Int

    override fun getIntOrNull(key: String): Int? = values[key] as? Int

    override fun putInt(
        key: String,
        value: Int,
    ) {
        values[key] = value
    }

    override fun getString(key: String): String? = values[key] as? String

    override fun putString(
        key: String,
        value: String,
    ) {
        values[key] = value
    }

    override fun removeKey(key: String) {
        values.remove(key)
    }

    override fun clear() {
        values.clear()
    }

    override fun contains(key: String): Boolean = values.containsKey(key)

    override fun toMap(): Map<String, Any?> = values.toMap()

    override fun getLong(
        key: String,
        defaultValue: Long,
    ): Long = values.getOrDefault(key, defaultValue) as Long

    override fun putLong(
        key: String,
        value: Long,
    ) {
        values[key] = value
    }

    override fun getLongOrNull(key: String): Long? = values[key] as? Long
}

class PreferencesRepositoryTest {
    @Test
    fun testProgressValue() {
        val preferenceManager = TestPreferenceManager()
        val preferencesRepository = PreferencesRepositoryImpl(preferenceManager, 400)

        assertEquals(0, preferencesRepository.getProgressiveValue())

        preferencesRepository.incrementProgressiveValue()
        assertEquals(1, preferencesRepository.getProgressiveValue())

        preferencesRepository.incrementProgressiveValue()
        assertEquals(2, preferencesRepository.getProgressiveValue())

        preferencesRepository.decrementProgressiveValue()
        assertEquals(1, preferencesRepository.getProgressiveValue())

        preferencesRepository.decrementProgressiveValue()
        assertEquals(0, preferencesRepository.getProgressiveValue())

        preferencesRepository.decrementProgressiveValue()
        assertEquals(0, preferencesRepository.getProgressiveValue())
    }

    @Test
    fun testMigrationWhenUsingDoubleClick() {
        val preferenceManager = TestPreferenceManager()
        preferenceManager.putBoolean("preference_double_click_open", true)
        assertTrue(preferenceManager.values["preference_double_click_open"] as Boolean)

        PreferencesRepositoryImpl(preferenceManager, 400)

        assertTrue(preferenceManager.values["preference_double_click_open"] == null)
        assertEquals(1, preferenceManager.values["preference_control_style"])
        assertFalse(preferenceManager.getBoolean("preference_double_click_open", false))
    }

    @Test
    fun testMigrationWhenNotUsingDoubleClick() {
        val preferenceManager = TestPreferenceManager()
        preferenceManager.putBoolean("preference_double_click_open", false)
        assertFalse(preferenceManager.values["preference_double_click_open"] as Boolean)

        PreferencesRepositoryImpl(preferenceManager, 400)

        assertTrue(preferenceManager.values["preference_double_click_open"] == null)
        assertFalse(preferenceManager.getBoolean("preference_double_click_open", false))
    }

    @Test
    fun testPremiumFlag() {
        val preferenceManager = TestPreferenceManager()
        val preferencesRepository = PreferencesRepositoryImpl(preferenceManager, 500)
        assertFalse(preferencesRepository.isPremiumEnabled())

        preferencesRepository.setPremiumFeatures(true)
        assertTrue(preferencesRepository.isPremiumEnabled())

        // Premium can't be disabled after enabled.
        preferencesRepository.setPremiumFeatures(false)
        assertTrue(preferencesRepository.isPremiumEnabled())
    }
}
