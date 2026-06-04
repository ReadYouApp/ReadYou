package me.ash.reader.ui.page.home.reading.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.BottomDrawerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.component.base.BottomDrawer
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.page.home.feeds.drawer.feed.FeedOptionViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StyleOptionDrawer(
    drawerState: ModalBottomSheetState,
    feedOptionViewModel: FeedOptionViewModel = hiltViewModel(),
    content: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val feedOptionUiState = feedOptionViewModel.feedOptionUiState.collectAsStateValue()
    val feed = feedOptionUiState.feed
    val toastString = stringResource(R.string.rename_toast, feedOptionUiState.newName)

    BackHandler(drawerState.isVisible) {
        scope.launch {
            drawerState.hide()
        }
    }

    BottomDrawer(
        drawerState = drawerState,
        sheetContent = {
            Info()
        }
    ) {
        content()
    }
}

@Composable
fun Info() {
    Column(modifier = Modifier.navigationBarsPadding()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Tab(selected = true, onClick = { /*TODO*/ })
        }
    }
}

@Preview
@Composable
fun Prev() {
    Tab(selected = true, onClick = { /*TODO*/ })
}
