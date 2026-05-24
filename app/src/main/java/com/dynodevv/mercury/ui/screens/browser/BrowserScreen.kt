package com.dynodevv.mercury.ui.screens.browser

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.dynodevv.mercury.R
import com.dynodevv.mercury.domain.model.TabInfo
import com.dynodevv.mercury.ui.components.AddressBar
import com.dynodevv.mercury.ui.components.MercuryWebViewClient
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSearch: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUrl by viewModel.currentUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pageTitle by viewModel.pageTitle.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()
    val canGoForward by viewModel.canGoForward.collectAsState()
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    val showAiChat by viewModel.showAiChat.collectAsState()
    val homepageUrl by viewModel.homepageUrl.collectAsState()

    var showTabSwitcher by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val adBlockService = viewModel.adBlockService

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            webViewClient = MercuryWebViewClient(
                adBlockService = adBlockService,
                onPageStarted = { viewModel.onPageStarted(it) },
                onPageFinished = { url, title -> viewModel.onPageFinished(url, title) },
                onShouldOverrideUrlLoading = { url ->
                    viewModel.loadUrl(url)
                    false
                }
            )
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    viewModel.onProgressChanged(newProgress)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        adBlockService.initialize()
    }

    DisposableEffect(webView) {
        viewModel.setWebView(webView)
        onDispose {
            viewModel.setWebView(null)
        }
    }

    // Load URL when it changes
    LaunchedEffect(currentUrl) {
        if (currentUrl != webView.url) {
            if (currentUrl == "mercury://homepage") {
                // Don't load anything in WebView for homepage
            } else {
                webView.loadUrl(currentUrl)
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (currentUrl == "mercury://homepage") stringResource(R.string.app_name) else pageTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.toggleBookmark() }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = stringResource(R.string.add_bookmark),
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.settings)
                            )
                        }
                    }
                )
                AddressBar(
                    url = currentUrl,
                    isLoading = isLoading,
                    onUrlSubmit = { viewModel.loadUrl(it) },
                    onAiSearch = { onNavigateToSearch(it) }
                )
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.goBack() },
                        enabled = canGoBack
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                    IconButton(
                        onClick = { viewModel.goForward() },
                        enabled = canGoForward
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.forward))
                    }
                    IconButton(onClick = {
                        if (isLoading) viewModel.stopLoading() else viewModel.reload()
                    }) {
                        Icon(
                            imageVector = if (isLoading) Icons.Filled.Stop else Icons.Filled.Refresh,
                            contentDescription = if (isLoading) stringResource(R.string.stop) else stringResource(R.string.reload)
                        )
                    }
                    IconButton(onClick = { viewModel.loadUrl(homepageUrl) }) {
                        Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.home))
                    }
                    BadgedBox(
                        badge = {
                            if (viewModel.tabs.size > 1) {
                                Badge { Text(viewModel.tabs.size.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = { showTabSwitcher = true }) {
                            Icon(Icons.Filled.Tab, contentDescription = stringResource(R.string.tabs))
                        }
                    }
                    IconButton(onClick = { viewModel.toggleAiChat() }) {
                        Icon(Icons.Filled.ChatBubble, contentDescription = stringResource(R.string.talk_to_website))
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (currentUrl == "mercury://homepage") {
                HomePageContent(
                    onSearch = { onNavigateToSearch(it) },
                    onNavigateToUrl = { viewModel.loadUrl(it) }
                )
            } else {
                AndroidView(
                    factory = { webView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // AI Chat Bottom Sheet
            if (showAiChat) {
                AiChatPanel(
                    pageTitle = pageTitle,
                    pageUrl = currentUrl,
                    pageContent = viewModel.pageContent.collectAsState().value,
                    onDismiss = { viewModel.hideAiChat() }
                )
            }
        }
    }

    // Tab Switcher Bottom Sheet
    if (showTabSwitcher) {
        ModalBottomSheet(
            onDismissRequest = { showTabSwitcher = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.tabs),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Button(onClick = {
                        viewModel.addNewTab()
                        showTabSwitcher = false
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.new_tab))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.tabs) { tab ->
                        TabItem(
                            tab = tab,
                            isActive = tab.id == viewModel.tabs.getOrNull(viewModel.currentTabIndex)?.id,
                            onClick = {
                                val index = viewModel.tabs.indexOfFirst { it.id == tab.id }
                                viewModel.switchToTab(index)
                                showTabSwitcher = false
                            },
                            onClose = {
                                val index = viewModel.tabs.indexOfFirst { it.id == tab.id }
                                viewModel.closeTab(index)
                                if (viewModel.tabs.isEmpty()) showTabSwitcher = false
                            }
                        )
                    }
                }
            }
        }
    }

    // Menu Bottom Sheet
    if (showMenu) {
        ModalBottomSheet(
            onDismissRequest = { showMenu = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                MenuItem(
                    icon = Icons.Filled.Settings,
                    label = stringResource(R.string.settings),
                    onClick = { showMenu = false; onNavigateToSettings() }
                )
                MenuItem(
                    icon = Icons.Filled.List,
                    label = stringResource(R.string.history),
                    onClick = { showMenu = false; onNavigateToHistory() }
                )
                MenuItem(
                    icon = Icons.Filled.Bookmark,
                    label = stringResource(R.string.bookmarks),
                    onClick = { showMenu = false }
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: TabInfo,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tab.title.ifBlank { stringResource(R.string.new_tab) },
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tab.url,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close_tab))
            }
        }
    }
}

@Composable
private fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun HomePageContent(
    onSearch: (String) -> Unit,
    onNavigateToUrl: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

        var searchText by remember { mutableStateOf("") }
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.extraLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (searchText.isNotBlank()) {
                    onSearch(searchText)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.ai_search))
        }
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Quick Links",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val quickLinks = listOf(
                "Google" to "https://google.com",
                "GitHub" to "https://github.com",
                "YouTube" to "https://youtube.com",
                "Reddit" to "https://reddit.com",
                "Hacker News" to "https://news.ycombinator.com"
            )
            items(quickLinks) { (name, url) ->
                Card(
                    onClick = { onNavigateToUrl(url) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Text(
                        text = name,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
