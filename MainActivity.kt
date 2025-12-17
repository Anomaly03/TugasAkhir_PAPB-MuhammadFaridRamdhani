package com.example.bab7_papb

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bab7_papb.ui.theme.Bab7_PAPBTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

// --- DATA MODEL ---
data class TempatWisata(
    val nama: String = "",
    val deskripsi: String = "",
    val gambarUriString: String? = null,
    val gambarResId: Int? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        val currentUser = FirebaseAuth.getInstance().currentUser

        setContent {
            Bab7_PAPBTheme {
                val navController = rememberNavController()
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        navController = navController,
                        startDestination = if (currentUser != null) "rekomendasi" else "login"
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
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

// --- SCREEN LOGIN (BAB 5/6) ---
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Travelupa Login", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) onLoginSuccess()
                    else Toast.makeText(context, "Login Gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Masuk") }
    }
}

// --- SCREEN REKOMENDASI (BAB 4 DENGAN NAVIGASI) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekomendasiTempatScreen(onLogout: () -> Unit) {
    // CEK TYPO DI SINI: tumpak_sweu -> tumpak_sewu (sesuaikan dengan file di folder res/drawable)
    var daftarWisata by remember {
        mutableStateOf(listOf(
            TempatWisata("Tumpak Sewu", "Air terjun tercantik.", gambarResId = R.drawable.tumpak_sewu),
            TempatWisata("Gunung Bromo", "Matahari terbit bagus.", gambarResId = R.drawable.gunung_bromo)
        ))
    }

    var showTambahDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Travelupa") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showTambahDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            items(daftarWisata) { tempat ->
                TempatItemEditable(tempat = tempat, onDelete = {
                    daftarWisata = daftarWisata.filter { it != tempat }
                })
            }
        }
    }

    if (showTambahDialog) {
        TambahTempatWisataDialog(
            onDismiss = { showTambahDialog = false },
            onTambah = { nama, deskripsi, _ ->
                // Gunakan gambar yang PASTI ADA di folder drawable agar tidak error
                daftarWisata = daftarWisata + TempatWisata(nama, deskripsi, gambarResId = R.drawable.gunung_bromo)
                showTambahDialog = false
            }
        )
    }
}

@Composable
fun TempatItemEditable(tempat: TempatWisata, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Gunakan ic_launcher_background sebagai cadangan jika file gambar tidak ditemukan
            val imagePainter = if (tempat.gambarResId != null) {
                painterResource(id = tempat.gambarResId)
            } else {
                painterResource(id = R.drawable.ic_launcher_background)
            }

            Image(
                painter = imagePainter,
                contentDescription = tempat.nama,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = tempat.nama, style = MaterialTheme.typography.titleLarge)
                    Text(text = tempat.deskripsi, style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun TambahTempatWisataDialog(onDismiss: () -> Unit, onTambah: (String, String, String?) -> Unit) {
    var nama by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Tempat") },
        text = {
            Column {
                TextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = deskripsi, onValueChange = { deskripsi = it }, label = { Text("Deskripsi") })
            }
        },
        confirmButton = { Button(onClick = { onTambah(nama, deskripsi, null) }) { Text("Tambah") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}