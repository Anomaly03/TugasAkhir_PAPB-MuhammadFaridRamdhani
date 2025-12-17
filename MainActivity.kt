package com.example.bab9_papb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// --- 1. WEB SERVICE COMPONENTS (BAB 9) ---

// Model data dari API
data class WisataResponse(
    val id: Int,
    val nama: String,
    val deskripsi: String,
    val url_gambar: String
)

// Interface Retrofit
interface ApiService {
    @GET("wisata") // Sesuaikan dengan endpoint API kamu
    suspend fun getWisata(): List<WisataResponse>
}

// Singleton Retrofit
object RetrofitClient {
    private const val BASE_URL = "https://api.contoh.com/" // Ganti dengan URL API asli

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// --- 2. MAIN ACTIVITY ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation() // Panggil navigasi seperti Bab 7 & 8
            }
        }
    }
}

@Composable
fun WisataApiScreen() {
    // State untuk menampung data dari internet
    var listWisata by remember { mutableStateOf(emptyList<WisataResponse>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Bab 9: Fetch data menggunakan LaunchedEffect (Coroutine)
    LaunchedEffect(Unit) {
        try {
            listWisata = RetrofitClient.instance.getWisata()
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            // Handle error (misal tampilkan toast)
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        LazyColumn {
            items(listWisata) { item ->
                WisataApiItem(item)
            }
        }
    }
}

@Composable
fun WisataApiItem(wisata: WisataResponse) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Bab 9: Menampilkan gambar dari URL menggunakan Coil
            AsyncImage(
                model = wisata.url_gambar,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
            Text(text = wisata.nama, style = MaterialTheme.typography.titleLarge)
            Text(text = wisata.deskripsi)
        }
    }
}