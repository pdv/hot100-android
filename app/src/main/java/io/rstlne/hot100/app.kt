package io.rstlne.hot100

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room

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

class Hot100Activity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color.Gray,
                    secondary = Color.DarkGray,
                    tertiary = Color.LightGray
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Nav()
                }
            }
        }
    }
}