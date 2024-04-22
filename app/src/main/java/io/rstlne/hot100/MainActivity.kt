package io.rstlne.hot100

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import io.rstlne.hot100.ui.theme.Hot100Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Hot100ViewModel(
    val dao: Hot100Dao,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY] as Application)
                val db = Room.databaseBuilder(application.applicationContext, Hot100Database::class.java, "hot100.db")
                    .createFromAsset("hot100.db")
                    .build()
                val savedStateHandle = createSavedStateHandle()
                Hot100ViewModel(
                    dao = db.hot100Dao(),
                    savedStateHandle
                )
            }
        }
    }
}

@Composable
fun SearchView(
    repository: Hot100ViewModel = viewModel(factory = Hot100ViewModel.Factory),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    var searchQuery by remember { mutableStateOf<String>("") }
    var searchResults by remember { mutableStateOf<List<Track>>(mutableListOf()) }
    Column {
        TextField(value = searchQuery, onValueChange = { query ->
            searchQuery = query
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    searchResults = repository.dao.search(query)
                }
            }
        })
        LazyColumn {
            items(searchResults) { result ->
                Text("${result.performer} - ${result.title} (${result.peakPosition})")
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
        startDestination = startDestination
    ) {
        composable("search") { entry ->
            SearchView()
        }

    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Hot100Theme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Nav()
                }
            }
        }
    }
}