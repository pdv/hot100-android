package io.rstlne.hot100

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TopBar(
    navController: NavController,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.DarkGray)
    ) {
        IconButton(onClick = { navController.navigateUp() }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
        }
        Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxHeight()) {
            Text(title)
        }
    }
}

@Composable
fun ChartView(
    navController: NavController,
    week: String,
    viewModel: Hot100ViewModel = viewModel(factory = Hot100ViewModel.Factory)
) {
    var tracks: List<Track> by remember { mutableStateOf(mutableListOf()) }
    LaunchedEffect(true) {
        tracks = viewModel.dao.chart(week)
    }
    Scaffold(
        topBar = { TopBar(navController, title = week) },
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(tracks) { track ->
                Surface(onClick = {
                    navController.navigate("tracks/${track.performer}/${track.title}")
                }) {
                    ListItem(
                        leadingContent = {
                            Text("${track.peak}")
                        },
                        headlineContent = {
                            Text("${track.performer} - ${track.title}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TrackView(
    navController: NavController,
    performer: String,
    title: String,
    viewModel: Hot100ViewModel = viewModel(factory = Hot100ViewModel.Factory),
) {
    var entries: List<Entry> by remember { mutableStateOf(mutableListOf()) }
    val peak = entries.minOfOrNull { it.position }
    LaunchedEffect(true) {
        entries = viewModel.dao.entries(performer, title)
    }
    Scaffold(
        topBar = { TopBar(navController, title = "$performer - $title") }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(entries) { entry ->
                Surface(onClick = {
                    navController.navigate("charts/${entry.week}")
                }) {
                    ListItem(headlineContent = {
                        val isPeak = if (entry.position == peak) "*" else ""
                        Text("${entry.week} (${entry.position}$isPeak)")
                    })
                }
            }
        }
    }
}

@Composable
fun SearchView(
    navController: NavController,
    repository: Hot100ViewModel = viewModel(factory = Hot100ViewModel.Factory),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
) {
    var searchQuery by rememberSaveable { mutableStateOf<String>("") }
    var searchResults by rememberSaveable { mutableStateOf<List<Track>>(mutableListOf()) }
    var focusRequester = remember { FocusRequester() }
    LaunchedEffect(true) {
        focusRequester.requestFocus()
    }
    Column {
        TextField(value = searchQuery, onValueChange = { query ->
            searchQuery = query
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    searchResults = repository.dao.search(query)
                }
            }
        }, modifier = Modifier
            .fillMaxWidth()
            .focusable()
            .focusRequester(focusRequester))
        LazyColumn {
            items(searchResults) { result ->
                Surface(onClick = {
                    navController.navigate("tracks/${result.performer}/${result.title}")
                }) {
                    ListItem(headlineContent = {
                        Text("${result.performer} - ${result.title} (${result.peak})")
                    })
                }
            }
        }
    }
}

@Composable
fun Nav(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "search"
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable("search") { entry ->
            SearchView(navController)
        }
        composable("tracks/{performer}/{title}",
            arguments = listOf(
                navArgument("performer") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { entry ->
            val performer = entry.arguments?.getString("performer") ?: "Rick Astley"
            val title = entry.arguments?.getString("title") ?: "Never Gonna Give You Up"
            TrackView(navController, performer, title)
        }
        composable("charts/{week}",
            arguments = listOf(navArgument("week") { type = NavType.StringType })
        ) { entry ->
            val week = entry.arguments?.getString("week") ?: "1958-08-04"
            ChartView(navController, week)
        }
    }
}