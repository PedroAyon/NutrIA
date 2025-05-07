package dev.pedroayon.nutria.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.pedroayon.nutria.R

@Composable
fun ProfileFormScreen(onComplete: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var diseases by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.additional_info_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = stringResource(R.string.additional_info_message),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text(stringResource(R.string.height_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text(stringResource(R.string.weight_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = dob,
            onValueChange = { dob = it },
            label = { Text(stringResource(R.string.dob_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text(stringResource(R.string.gender_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = diseases,
            onValueChange = { diseases = it },
            label = { Text(stringResource(R.string.diseases_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = allergies,
            onValueChange = { allergies = it },
            label = { Text(stringResource(R.string.allergies_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onComplete() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.finish_button))
        }
    }
}
