package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.translateTargetLanguage
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put
import java.util.Locale

val LocalTranslateTargetLanguage = compositionLocalOf<TranslateTargetLanguagePreference> { TranslateTargetLanguagePreference.default }

sealed class TranslateTargetLanguagePreference(val value: Int) : Preference() {
    data object UseDeviceLanguages : TranslateTargetLanguagePreference(0)
    data object English : TranslateTargetLanguagePreference(1)
    data object ChineseSimplified : TranslateTargetLanguagePreference(2)
    data object German : TranslateTargetLanguagePreference(3)
    data object French : TranslateTargetLanguagePreference(4)
    data object Czech : TranslateTargetLanguagePreference(5)
    data object Italian : TranslateTargetLanguagePreference(6)
    data object Hindi : TranslateTargetLanguagePreference(7)
    data object Spanish : TranslateTargetLanguagePreference(8)
    data object Polish : TranslateTargetLanguagePreference(9)
    data object Russian : TranslateTargetLanguagePreference(10)
    data object Basque : TranslateTargetLanguagePreference(11)
    data object Indonesian : TranslateTargetLanguagePreference(12)
    data object ChineseTraditional : TranslateTargetLanguagePreference(13)
    data object Arabic : TranslateTargetLanguagePreference(14)
    data object Bulgarian : TranslateTargetLanguagePreference(15)
    data object Catalan : TranslateTargetLanguagePreference(16)
    data object Danish : TranslateTargetLanguagePreference(17)
    data object Dutch : TranslateTargetLanguagePreference(18)
    data object Esperanto : TranslateTargetLanguagePreference(19)
    data object Filipino : TranslateTargetLanguagePreference(20)
    data object Hebrew : TranslateTargetLanguagePreference(21)
    data object Hungarian : TranslateTargetLanguagePreference(22)
    data object Japanese : TranslateTargetLanguagePreference(23)
    data object Kannada : TranslateTargetLanguagePreference(24)
    data object NorwegianBokmal : TranslateTargetLanguagePreference(25)
    data object Persian : TranslateTargetLanguagePreference(26)
    data object Portuguese : TranslateTargetLanguagePreference(27)
    data object PortugueseBrazil : TranslateTargetLanguagePreference(28)
    data object Romanian : TranslateTargetLanguagePreference(29)
    data object Serbian : TranslateTargetLanguagePreference(30)
    data object Slovenian : TranslateTargetLanguagePreference(31)
    data object Swedish : TranslateTargetLanguagePreference(32)
    data object Turkish : TranslateTargetLanguagePreference(33)
    data object Ukrainian : TranslateTargetLanguagePreference(34)
    data object Vietnamese : TranslateTargetLanguagePreference(35)
    data object ArabicNorthLevantine : TranslateTargetLanguagePreference(36)
    data object Estonian : TranslateTargetLanguagePreference(37)
    data object Galician : TranslateTargetLanguagePreference(38)
    data object Slovak : TranslateTargetLanguagePreference(39)
    data object Tamil : TranslateTargetLanguagePreference(40)


    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.translateTargetLanguage, value
            )
        }
    }

    @Composable
    fun toDesc(): String {
        return when (this) {
            ChineseTraditional -> stringResource(id = R.string.chinese_traditional)
            ChineseSimplified -> stringResource(id = R.string.chinese_simplified)
            else -> {
                this.toLocale().toDisplayName()
            }
        }
    }


    fun toLocale(): Locale? = when (this) {
        UseDeviceLanguages -> null
        English -> Locale("en")
        ChineseSimplified -> Locale.forLanguageTag("zh-Hans")
        German -> Locale("de")
        French -> Locale("fr")
        Czech -> Locale("cs")
        Italian -> Locale("it")
        Hindi -> Locale("hi")
        Spanish -> Locale("es")
        Polish -> Locale("pl")
        Russian -> Locale("ru")
        Basque -> Locale("eu")
        Indonesian -> Locale("in")
        ChineseTraditional -> Locale.forLanguageTag("zh-Hant")
        Arabic -> Locale("ar")
        Bulgarian -> Locale("bg")
        Catalan -> Locale("ca")
        Danish -> Locale("da")
        Dutch -> Locale("nl")
        Esperanto -> Locale("eo")
        Filipino -> Locale("fil")
        Hebrew -> Locale("he")
        Hungarian -> Locale("hu")
        Japanese -> Locale("ja")
        Kannada -> Locale("kn")
        NorwegianBokmal -> Locale("nb")
        Persian -> Locale("fa")
        Portuguese -> Locale("pt")
        PortugueseBrazil -> Locale("pt", "BR")
        Romanian -> Locale("ro")
        Serbian -> Locale("sr")
        Slovenian -> Locale("sl")
        Swedish -> Locale("sv")
        Turkish -> Locale("tr")
        Ukrainian -> Locale("uk")
        Vietnamese -> Locale("vi")
        ArabicNorthLevantine -> Locale.forLanguageTag("apc")
        Estonian -> Locale("et")
        Galician -> Locale("gl")
        Slovak -> Locale("sk")
        Tamil -> Locale("ta")
    }

    companion object {

        val default = UseDeviceLanguages

        val values = listOf(
            UseDeviceLanguages,
            Arabic,
            ArabicNorthLevantine,
            Basque,
            Bulgarian,
            Catalan,
            ChineseSimplified,
            ChineseTraditional,
            Czech,
            Danish,
            Dutch,
            English,
            Esperanto,
            Estonian,
            Filipino,
            French,
            Galician,
            German,
            Hebrew,
            Hindi,
            Hungarian,
            Indonesian,
            Italian,
            Japanese,
            Kannada,
            NorwegianBokmal,
            Persian,
            Polish,
            Portuguese,
            PortugueseBrazil,
            Romanian,
            Russian,
            Serbian,
            Slovak,
            Slovenian,
            Spanish,
            Swedish,
            Tamil,
            Turkish,
            Ukrainian,
            Vietnamese
        )

        fun fromPreferences(preferences: Preferences): TranslateTargetLanguagePreference =
            fromValue(preferences[DataStoreKey.keys[translateTargetLanguage]?.key as Preferences.Key<Int>] ?: 0)


        fun fromValue(value: Int): TranslateTargetLanguagePreference = when (value) {
            0 -> UseDeviceLanguages
            1 -> English
            2 -> ChineseSimplified
            3 -> German
            4 -> French
            5 -> Czech
            6 -> Italian
            7 -> Hindi
            8 -> Spanish
            9 -> Polish
            10 -> Russian
            11 -> Basque
            12 -> Indonesian
            13 -> ChineseTraditional
            14 -> Arabic
            15 -> Bulgarian
            16 -> Catalan
            17 -> Danish
            18 -> Dutch
            19 -> Esperanto
            20 -> Filipino
            21 -> Hebrew
            22 -> Hungarian
            23 -> Japanese
            24 -> Kannada
            25 -> NorwegianBokmal
            26 -> Persian
            27 -> Portuguese
            28 -> PortugueseBrazil
            29 -> Romanian
            30 -> Serbian
            31 -> Slovenian
            32 -> Swedish
            33 -> Turkish
            34 -> Ukrainian
            35 -> Vietnamese
            36 -> ArabicNorthLevantine
            37 -> Estonian
            38 -> Galician
            39 -> Slovak
            40 -> Tamil
            else -> default
        }

    }
}
