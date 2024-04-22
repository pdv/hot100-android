package io.rstlne.hot100

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
fun TrackView(
    navController: NavController,
    performer: String,
    title: String,
    viewModel: Hot100ViewModel = viewModel(factory = Hot100ViewModel.Factory),
) {
    var entries: List<Entry> by remember { mutableStateOf(mutableListOf()) }
    LaunchedEffect(true) {
        entries = viewModel.dao.entries(performer, title)
    }
    Scaffold(
        topBar = {
            Row {
                IconButton(onClick = {
                    navController.navigateUp()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text("$performer - $title")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(entries) { entry ->
                Text("${entry.week} - ${entry.position}")
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
    }
}