package com.example.otussecurityhomework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.otussecurityhomework.data.RepositoryImpl
import com.example.otussecurityhomework.domain.LoginUseCase
import com.example.otussecurityhomework.ui.theme.OtusSecurityHomeWorkTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = applicationContext as App
        val repository = RepositoryImpl()
        val loginUseCase = LoginUseCase(repository, app.storage)


        enableEdgeToEdge()
        setContent {
            OtusSecurityHomeWorkTheme {
                val screens = remember { mutableStateOf(Screens.LOGIN) }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (screens.value) {
                            Screens.LOGIN -> {
                                val loginViewModel = remember {
                                    LoginScreenViewModel(
                                        loginUseCase = loginUseCase,
                                        onSuccess = { screens.value = Screens.ENTER },
                                        onError = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(message = it)
                                            }
                                        }
                                    )
                                }
                                LoginScreen(
                                    viewModel = loginViewModel
                                )
                            }

                            Screens.ENTER -> Text(text = app.storage.getTokenWithHash() ?: "No token saved")
                        }
                    }

                }
            }
        }
    }
}

private enum class Screens {
    LOGIN, ENTER
}