package dev.pedroayon.nutria.auth.ui

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.pedroayon.nutria.R
import dev.pedroayon.nutria.auth.domain.model.AuthState
import dev.pedroayon.nutria.auth.presentation.AuthViewModel
import dev.pedroayon.nutria.auth.ui.components.GoogleAuthButton

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    Scaffold(modifier = Modifier.padding(32.dp)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground), // TODO: Replace with your actual logo resource ID
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(250.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.app_description),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)

                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                GoogleAuthButton(
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = uiState is AuthState.LoadingGoogle || uiState is AuthState.LoadingFirebase,
                    onClicked = {
                        if (activity != null) {
                            viewModel.triggerGoogleSignIn(activity)
                        } else {
                            Log.e(TAG, "Activity context not available to trigger Google Sign In")
                            // Optionally, set an error state in the ViewModel
                            // viewModel.handleCredentialError(IllegalStateException("LoginScreen requires an Activity context..."))
                        }
                    }
                )

                if (uiState is AuthState.Error) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Error: ${(uiState as AuthState.Error).exception?.message ?: "Unknown error"}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
