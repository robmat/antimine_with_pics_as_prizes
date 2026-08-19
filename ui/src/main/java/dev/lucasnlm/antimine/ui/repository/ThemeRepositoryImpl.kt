package dev.lucasnlm.antimine.ui.repository

import android.content.Context
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.model.AppTheme

/**
 * [context] and [preferenceRepository] are `internal` rather than `private`
 * because the theme/palette building logic was split out into extension
 * functions in ThemeRepositoryImplExtra.kt, since this class's function
 * count was over detekt's threshold.
 */
class ThemeRepositoryImpl(
    internal val context: Context,
    internal val preferenceRepository: PreferencesRepository,
) : ThemeRepository {

    override fun getCustomTheme(): AppTheme? {
        val targetThemeId = preferenceRepository.themeId()
        return getAllThemes().firstOrNull { it.id == targetThemeId }
    }

    override fun getSkin(): AppSkin {
        val targetSkinId = preferenceRepository.skinId()
        val allSkins = getAllSkins()
        return allSkins.firstOrNull { it.id == targetSkinId } ?: allSkins.first()
    }

    override fun getTheme(): AppTheme {
        return getCustomTheme() ?: getDefaultTheme()
    }

    override fun getAllThemes(): List<AppTheme> = listOf(buildSystemTheme()) + Themes.getAllCustom()

    override fun getAllSkins(): List<AppSkin> {
        return Skins.getAllSkins()
    }

    override fun setTheme(themeId: Long) {
        preferenceRepository.useTheme(themeId)
    }

    override fun setSkin(skinId: Long) {
        preferenceRepository.useSkin(skinId)
    }

    override fun reset(): AppTheme {
        val defaultTheme = getDefaultTheme()
        preferenceRepository.useTheme(defaultTheme.id)
        return defaultTheme
    }
}
