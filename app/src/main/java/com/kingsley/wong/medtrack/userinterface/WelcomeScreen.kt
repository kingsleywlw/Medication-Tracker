package com.kingsley.wong.medtrack.userinterface

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.kingsley.wong.medtrack.R
import com.kingsley.wong.medtrack.navigation.Screen

@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Image(
            painter = painterResource(id = R.drawable.medtrack_logo__1_),
            contentDescription = "MedTrack Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .offset(y = 70.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "MedTrack",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.offset(y = (-10).dp)
        )

        Text(
            text = "This app is for tracking purposes only and does not replace professional medical advice",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, "https://www.monashhealth.org/".toUri())
                context.startActivity(intent)
            }
        ) {
            Text("Open Monash Health")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate(Screen.Login.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.navigate(Screen.ClaimAccount.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Claim Account")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.navigate(Screen.SignUp.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up (New User)")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Medication Tracker",
            style = MaterialTheme.typography.bodySmall
        )
    }
}