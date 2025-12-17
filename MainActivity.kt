package com.example.bab6_papb

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.bab6_papb.ui.theme.Bab6_PAPBTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class TempatWisata(
    val nama: String = "",
    val deskripsi: String = "",
    val gambarResId: Int? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek user saat ini (Bab 6) [cite: 1377, 2110]
        val currentUser = FirebaseAuth.getInstance().currentUser

        setContent {
            Bab6_PAPBTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    AppNavigation(currentUser)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(currentUser: com.google.firebase.auth.FirebaseUser?) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        // Alur Bab 6: Start destination berdasarkan login state [cite: 1397, 2136]
        startDestination = if (currentUser != null) "rekomendasi" else "login"
    ) {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("rekomendasi") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("rekomendasi") {
            RekomendasiTempatScreen(onLogout = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo("rekomendasi") { inclusive = true }
                }
            })
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // CoroutineScope (Bab 6) [cite: 879, 2197]
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Travelupa", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                isLoading = true
                scope.launch { // Menjalankan Coroutine [cite: 920, 2248]
                    try {
                        withContext(Dispatchers.IO) { // Operasi asinkron [cite: 923, 2251]
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
                        }
                        onLoginSuccess()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            else Text("Masuk")
        }
    }
}

@Composable
fun RekomendasiTempatScreen(onLogout: () -> Unit) {
    val daftar = listOf(
        TempatWisata("Tumpak Sewu", "Air terjun tercantik di Jawa Timur.", R.drawable.tumpak_sewu),
        TempatWisata("Gunung Bromo", "Matahari terbitnya bagus banget.", R.drawable.gunung_bromo)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rekomendasi") },
                actions = {
                    IconButton(onClick = onLogout) { Icon(Icons.Default.ExitToApp, contentDescription = "Logout") }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(daftar) { tempat ->
                Card(elevation = 4.dp, modifier = Modifier.padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Image(painter = painterResource(id = tempat.gambarResId!!), contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop)
                        Text(text = tempat.nama, style = MaterialTheme.typography.h6, modifier = Modifier.padding(top = 8.dp))
                        Text(text = tempat.deskripsi, style = MaterialTheme.typography.body2)
                    }
                }
            }
        }
    }
}