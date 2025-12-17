package com.example.bab8_papb

import android.content.Context
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
import androidx.compose.material.icons.filled.*
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
import androidx.navigation.compose.*
import androidx.room.*
import com.example.bab8_papb.ui.theme.Bab8_PAPBTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// --- 1. ROOM DATABASE COMPONENTS ---

@Entity(tableName = "tempat_wisata")
data class TempatWisata(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val deskripsi: String,
    val gambarResId: Int? = null
)

@Dao
interface TempatWisataDao {
    @Query("SELECT * FROM tempat_wisata")
    fun getAll(): Flow<List<TempatWisata>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tempat: TempatWisata)

    @Delete
    suspend fun delete(tempat: TempatWisata)
}

@Database(entities = [TempatWisata::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tempatWisataDao(): TempatWisataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travelupa_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- 2. MAIN ACTIVITY ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val dao = database.tempatWisataDao()

        setContent {
            Bab8_PAPBTheme {
                val navController = rememberNavController()
                val currentUser = FirebaseAuth.getInstance().currentUser

                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        navController = navController,
                        startDestination = if (currentUser != null) "rekomendasi" else "login",
                        dao = dao
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String, dao: TempatWisataDao) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("rekomendasi") { popUpTo("login") { inclusive = true } }
            })
        }
        composable("rekomendasi") {
            RekomendasiTempatScreen(
                dao = dao,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") { popUpTo("rekomendasi") { inclusive = true } }
                }
            )
        }
    }
}

// --- 3. UI SCREENS ---

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekomendasiTempatScreen(dao: TempatWisataDao, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    val daftarWisata by dao.getAll().collectAsState(initial = emptyList())
    var showTambahDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Travelupa Bab 8") },
                actions = { IconButton(onClick = onLogout) { Icon(Icons.Default.ExitToApp, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showTambahDialog = true }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(daftarWisata) { tempat ->
                TempatItemEditable(tempat = tempat, onDelete = {
                    scope.launch { dao.delete(tempat) }
                })
            }
        }
    }

    if (showTambahDialog) {
        TambahTempatWisataDialog(
            onDismiss = { showTambahDialog = false },
            onTambah = { nama, deskripsi ->
                scope.launch {
                    // Pastikan nama drawable ini ada di folder res/drawable kamu
                    dao.insert(TempatWisata(nama = nama, deskripsi = deskripsi, gambarResId = R.drawable.gunung_bromo))
                }
                showTambahDialog = false
            }
        )
    }
}

@Composable
fun TempatItemEditable(tempat: TempatWisata, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            val imagePainter = if (tempat.gambarResId != null) painterResource(id = tempat.gambarResId)
            else painterResource(id = R.drawable.ic_launcher_background)

            Image(painter = imagePainter, contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = tempat.nama, style = MaterialTheme.typography.titleLarge)
                    Text(text = tempat.deskripsi, style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null, tint = Color.Red) }
            }
        }
    }
}

@Composable
fun TambahTempatWisataDialog(onDismiss: () -> Unit, onTambah: (String, String) -> Unit) {
    var nama by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Wisata") },
        text = {
            Column {
                TextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = deskripsi, onValueChange = { deskripsi = it }, label = { Text("Deskripsi") })
            }
        },
        confirmButton = { Button(onClick = { onTambah(nama, deskripsi) }) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}