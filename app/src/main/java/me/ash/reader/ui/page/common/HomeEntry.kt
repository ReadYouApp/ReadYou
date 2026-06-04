package me.ash.reader.ui.page.common

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.infrastructure.preference.LocalDarkTheme
import me.ash.reader.infrastructure.preference.LocalKeywordFilters
import me.ash.reader.infrastructure.preference.LocalReadingDarkTheme
import me.ash.reader.ui.ext.animatedComposable
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.findActivity
import me.ash.reader.ui.ext.initialFilter
import me.ash.reader.ui.ext.initialPage
import me.ash.reader.ui.ext.isFirstLaunch
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feeds.FeedsPage
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel
import me.ash.reader.ui.page.home.flow.FlowPage
import me.ash.reader.ui.page.home.reading.ReadingPage
import me.ash.reader.ui.page.settings.SettingsPage
import me.ash.reader.ui.page.settings.accounts.AccountDetailsPage
import me.ash.reader.ui.page.settings.accounts.AccountsPage
import me.ash.reader.ui.page.settings.accounts.AddAccountsPage
import me.ash.reader.ui.page.settings.color.ColorAndStylePage
import me.ash.reader.ui.page.settings.color.DarkThemePage
import me.ash.reader.ui.page.settings.color.feeds.FeedsPageStylePage
import me.ash.reader.ui.page.settings.color.flow.FlowPageStylePage
import me.ash.reader.ui.page.settings.color.reading.BionicReadingPage
import me.ash.reader.ui.page.settings.color.reading.ReadingDarkThemePage
import me.ash.reader.ui.page.settings.color.reading.ReadingImagePage
import me.ash.reader.ui.page.settings.color.reading.ReadingStylePage
import me.ash.reader.ui.page.settings.color.reading.ReadingTextPage
import me.ash.reader.ui.page.settings.color.reading.ReadingTitlePage
import me.ash.reader.ui.page.settings.color.reading.ReadingVideoPage
import me.ash.reader.ui.page.settings.interaction.InteractionPage
import me.ash.reader.ui.page.settings.languages.LanguagesPage
import me.ash.reader.ui.page.settings.tips.LicenseListPage
import me.ash.reader.ui.page.settings.tips.TipsAndSupportPage
import me.ash.reader.ui.page.settings.filter.FiltersPage
import me.ash.reader.ui.page.settings.troubleshooting.TroubleshootingPage
import me.ash.reader.ui.page.startup.StartupPage
import me.ash.reader.ui.theme.AppTheme

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun HomeEntry(
    homeViewModel: HomeViewModel = hiltViewModel(),
    subscribeViewModel: SubscribeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var isReadingPage by rememberSaveable { mutableStateOf(false) }
    val filterUiState = homeViewModel.filterUiState.collectAsStateValue()
    val subscribeUiState = subscribeViewModel.subscribeUiState.collectAsStateValue()
    val navController = rememberNavController()
    val keywordFilters = LocalKeywordFilters.current

    val intent by rememberSaveable { mutableStateOf(context.findActivity()?.intent) }
    var openArticleId by rememberSaveable {
        mutableStateOf(intent?.extras?.getString(ExtraName.ARTICLE_ID) ?: "")
    }.also {
        intent?.replaceExtras(null)
    }

    LaunchedEffect(keywordFilters) {
        homeViewModel.updateKeywordFilters(keywordFilters)
    }

    LaunchedEffect(Unit) {
        when (context.initialPage) {
            1 -> {
                navController.navigate(RouteName.FLOW) {
                    launchSingleTop = true
                }
            }
            // Other initial pages
        }

        homeViewModel.changeFilter(
            filterUiState.copy(
                filter = when (context.initialFilter) {
                    0 -> Filter.Starred
                    1 -> Filter.Unread
                    2 -> Filter.All
                    else -> Filter.All
                }
            )
        )

        // This is finally
        navController.currentBackStackEntryFlow.collectLatest {
            Log.i("RLog", "currentBackStackEntry: ${navController.currentDestination?.route}")
            // Animation duration takes 310 ms
            delay(310L)
            isReadingPage =
                navController.currentDestination?.route == "${RouteName.READING}/{articleId}"
        }
    }

    DisposableEffect(subscribeUiState.shouldNavigateToFeedPage) {
        if (subscribeUiState.shouldNavigateToFeedPage) {
            if (navController.currentDestination?.route != RouteName.FEEDS) {
                navController.popBackStack(
                    route = RouteName.FEEDS,
                    inclusive = false,
                    saveState = true
                )
            }
        }
        onDispose {
            subscribeViewModel.onIntentConsumed()
        }
    }

    LaunchedEffect(openArticleId) {
        if (openArticleId.isNotEmpty()) {
            navController.navigate(RouteName.FLOW) {
                launchSingleTop = true
            }
            navController.navigate("${RouteName.READING}/${openArticleId}") {
                launchSingleTop = true
            }
            openArticleId = ""
        }
    }

    val useDarkTheme = if (isReadingPage) {
        LocalReadingDarkTheme.current.isDarkTheme()
    } else {
        LocalDarkTheme.current.isDarkTheme()
    }

    AppTheme(
        useDarkTheme = if (isReadingPage) LocalReadingDarkTheme.current.isDarkTheme()
        else LocalDarkTheme.current.isDarkTheme()
    ) {

        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            navController = navController,
            startDestination = if (context.isFirstLaunch) RouteName.STARTUP else RouteName.FEEDS,
        ) {
            // Startup
            animatedComposable(route = RouteName.STARTUP) {
                StartupPage(navController)
            }

            // Home
            animatedComposable(route = RouteName.FEEDS) {
                FeedsPage(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    subscribeViewModel = subscribeViewModel
                )
            }
            animatedComposable(route = RouteName.FLOW) {
                FlowPage(
                    navController = navController,
                    homeViewModel = homeViewModel,
                )
            }
            animatedComposable(route = "${RouteName.READING}/{articleId}") {
                ReadingPage(navController = navController, homeViewModel = homeViewModel)
            }

            // Settings
            animatedComposable(route = RouteName.SETTINGS) {
                SettingsPage(navController)
            }

            // Accounts
            animatedComposable(route = RouteName.ACCOUNTS) {
                AccountsPage(navController)
            }

            animatedComposable(route = "${RouteName.ACCOUNT_DETAILS}/{accountId}") {
                AccountDetailsPage(navController)
            }

            animatedComposable(route = RouteName.ADD_ACCOUNTS) {
                AddAccountsPage(navController)
            }

            // Color & Style
            animatedComposable(route = RouteName.COLOR_AND_STYLE) {
                ColorAndStylePage(navController)
            }
            animatedComposable(route = RouteName.DARK_THEME) {
                DarkThemePage(navController)
            }
            animatedComposable(route = RouteName.FEEDS_PAGE_STYLE) {
                FeedsPageStylePage(navController)
            }
            animatedComposable(route = RouteName.FLOW_PAGE_STYLE) {
                FlowPageStylePage(navController)
            }
            animatedComposable(route = RouteName.READING_PAGE_STYLE) {
                ReadingStylePage(navController)
            }
            animatedComposable(route = RouteName.READING_BIONIC_READING) {
                BionicReadingPage(navController)
            }
            animatedComposable(route = RouteName.READING_DARK_THEME) {
                ReadingDarkThemePage(navController)
            }
            animatedComposable(route = RouteName.READING_PAGE_TITLE) {
                ReadingTitlePage(navController)
            }
            animatedComposable(route = RouteName.READING_PAGE_TEXT) {
                ReadingTextPage(navController)
            }
            animatedComposable(route = RouteName.READING_PAGE_IMAGE) {
                ReadingImagePage(navController)
            }
            animatedComposable(route = RouteName.READING_PAGE_VIDEO) {
                ReadingVideoPage(navController)
            }

            // Interaction
            animatedComposable(route = RouteName.INTERACTION) {
                InteractionPage(navController)
            }

            // Languages
            animatedComposable(route = RouteName.LANGUAGES) {
                LanguagesPage(navController = navController)
            }

            // Troubleshooting
            animatedComposable(route = RouteName.TROUBLESHOOTING) {
                TroubleshootingPage(navController = navController)
            }

            // Tips & Support
            animatedComposable(route = RouteName.TIPS_AND_SUPPORT) {
                TipsAndSupportPage(navController)
            }
            animatedComposable(route = RouteName.LICENSE_LIST) {
                LicenseListPage(navController)
            }

            // Filters
            animatedComposable(route = RouteName.FILTERS) {
                FiltersPage(navController)
            }
        }
    }
}
