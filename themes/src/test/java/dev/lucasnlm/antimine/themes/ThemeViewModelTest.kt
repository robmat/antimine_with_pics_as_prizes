package dev.lucasnlm.antimine.themes

import dev.lucasnlm.antimine.themes.viewmodel.ThemeEvent
import dev.lucasnlm.antimine.themes.viewmodel.ThemeState
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.AreaPalette
import dev.lucasnlm.antimine.ui.repository.ThemeRepository
import dev.lucasnlm.external.AnalyticsManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ThemeViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val lightTheme =
        AppTheme(
            id = 1L,
            theme = R.style.CustomLightTheme,
            isDarkTheme = false,
            palette =
                AreaPalette(
                    accent = 0xD32F2F,
                    background = 0xFFFFFF,
                    covered = 0x424242,
                    coveredOdd = 0x424242,
                    uncovered = 0xd5d2cc,
                    uncoveredOdd = 0xd5d2cc,
                    minesAround1 = 0x527F8D,
                    minesAround2 = 0x2B8D43,
                    minesAround3 = 0xE65100,
                    minesAround4 = 0x20A5f7,
                    minesAround5 = 0xED1C24,
                    minesAround6 = 0xFFC107,
                    minesAround7 = 0x66126B,
                    minesAround8 = 0x000000,
                    highlight = 0x212121,
                    focus = 0xD32F2F,
                ),
        )

    private val darkTheme =
        AppTheme(
            id = 2L,
            theme = R.style.CustomDarkTheme,
            isDarkTheme = true,
            palette =
                AreaPalette(
                    accent = 0xFFFFFF,
                    background = 0x212121,
                    covered = 0x171717,
                    coveredOdd = 0x171717,
                    uncovered = 0x424242,
                    uncoveredOdd = 0x424242,
                    minesAround1 = 0xd5d2cc,
                    minesAround2 = 0xd5d2cc,
                    minesAround3 = 0xd5d2cc,
                    minesAround4 = 0xd5d2cc,
                    minesAround5 = 0xd5d2cc,
                    minesAround6 = 0xd5d2cc,
                    minesAround7 = 0xd5d2cc,
                    minesAround8 = 0xd5d2cc,
                    highlight = 0xFFFFFF,
                    focus = 0xFFFFFF,
                ),
        )

    private val gardenTheme =
        AppTheme(
            id = 3L,
            theme = R.style.CustomGardenTheme,
            isDarkTheme = false,
            palette =
                AreaPalette(
                    accent = 0x689f38,
                    background = 0xefebe9,
                    covered = 0x689f38,
                    coveredOdd = 0x558b2f,
                    uncovered = 0xefebe9,
                    uncoveredOdd = 0xd7ccc8,
                    minesAround1 = 0x527F8D,
                    minesAround2 = 0x2B8D43,
                    minesAround3 = 0xE65100,
                    minesAround4 = 0x20A5f7,
                    minesAround5 = 0xED1C24,
                    minesAround6 = 0xFFC107,
                    minesAround7 = 0x66126B,
                    minesAround8 = 0x000000,
                    highlight = 0xFFFFFF,
                    focus = 0xFFFFFF,
                ),
        )

    private val allThemes =
        listOf(
            lightTheme,
            darkTheme,
            gardenTheme,
        )

    private val skin =
        AppSkin(
            id = 1L,
            file = "default",
            thumbnailImageRes = 0,
            canTint = true,
            isPremium = false,
            hasPadding = false,
        )

    private val allSkins = listOf(skin)

    @Test
    fun testInitialValue() {
        val themeRepository =
            mockk<ThemeRepository> {
                every { getAllThemes() } returns allThemes
                every { getTheme() } returns gardenTheme
                every { getSkin() } returns skin
                every { getAllSkins() } returns allSkins
            }

        val analyticsManager = mockk<AnalyticsManager> { }

        val viewModel = ThemeViewModel(themeRepository, analyticsManager)
        assertEquals(ThemeState(gardenTheme, skin, allThemes, allSkins), viewModel.singleState())
    }

    @Test
    fun testChangeValue() {
        val themeRepository =
            mockk<ThemeRepository> {
                every { getAllThemes() } returns allThemes
                every { getTheme() } returns gardenTheme
                every { getSkin() } returns skin
                every { getAllSkins() } returns allSkins
                every { setTheme(any()) } returns Unit
            }

        val analyticsManager =
            mockk<AnalyticsManager> {
                every { sentEvent(any()) } returns Unit
            }

        val state =
            ThemeViewModel(themeRepository, analyticsManager).run {
                sendEvent(ThemeEvent.ChangeTheme(darkTheme))
                singleState()
            }
        assertEquals(ThemeState(darkTheme, skin, allThemes, allSkins), state)

        verify { themeRepository.setTheme(darkTheme.id) }
    }

    @Test
    fun testChangeValueWithoutExtras() {
        val themeRepository =
            mockk<ThemeRepository> {
                every { getAllThemes() } returns allThemes
                every { getTheme() } returns gardenTheme
                every { getSkin() } returns skin
                every { getAllSkins() } returns allSkins
                every { setTheme(any()) } returns Unit
            }

        val analyticsManager =
            mockk<AnalyticsManager> {
                every { sentEvent(any()) } returns Unit
            }

        val state =
            ThemeViewModel(themeRepository, analyticsManager).run {
                sendEvent(ThemeEvent.ChangeTheme(gardenTheme))
                singleState()
            }
        assertEquals(ThemeState(gardenTheme, skin, allThemes, allSkins), state)
    }
}
