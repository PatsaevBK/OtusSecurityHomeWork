package com.example.otussecurityhomework

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.otussecurityhomework.data.RepositoryImpl
import com.example.otussecurityhomework.data.biometric.BiometricHelper
import com.example.otussecurityhomework.domain.BiometricEnabledUseCase
import com.example.otussecurityhomework.domain.BiometricRegisterUseCase
import com.example.otussecurityhomework.domain.LoginBeforeUseCase
import com.example.otussecurityhomework.domain.LoginUseCase
import com.example.otussecurityhomework.presentation.LoginScreenViewModel
import com.example.otussecurityhomework.presentation.SignInScreen
import com.example.otussecurityhomework.ui.theme.OtusSecurityHomeWorkTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = applicationContext as App
        val repository = RepositoryImpl()
        val loginUseCase = LoginUseCase(repository, app.storage)
        val loginBeforeUseCase = LoginBeforeUseCase(app.storage)
        val biometricHelper = BiometricHelper(app.applicationContext)
        val biometricRegisterUseCase = BiometricRegisterUseCase(biometricHelper)
        val biometricEnabledUseCase = BiometricEnabledUseCase(app.storage)

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
                                val loginViewModel = LoginScreenViewModel(
                                    loginUseCase = loginUseCase,
                                    biometricRegisterUseCase = biometricRegisterUseCase,
                                    biometricEnabledUseCase = biometricEnabledUseCase,
                                    biometricHelper = biometricHelper,
                                    loginBeforeUseCase = loginBeforeUseCase,
                                    onSuccess = {
                                        screens.value = Screens.ENTER
                                    },
                                    onError = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message = it)
                                        }
                                    }
                                )
                                SignInScreen(
                                    viewModel = loginViewModel
                                )
                            }

                            Screens.ENTER -> {
                                var checked by remember { mutableStateOf(biometricEnabledUseCase.isBiometricEnabled()) }

                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        Text(text = "Enable biometric: ")
                                        Switch(
                                            checked = checked,
                                            onCheckedChange = {
                                                checked = it
                                                biometricEnabledUseCase.changeBiometricEnabled(it)
                                            }
                                        )
                                    }
                                }
                            }
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