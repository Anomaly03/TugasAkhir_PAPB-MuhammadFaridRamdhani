package com.example.bab3_papb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bab3_papb.ui.theme.Bab3_PAPBTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Bab3_PAPBTheme {
                RekomendasiTempatScreen()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun RekomendasiTempatScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Fungsi items() akan mengulang tampilan sebanyak data di list
        items(daftarTempatWisata) { tempat ->
            TempatItem(tempat)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Bab3_PAPBTheme {
        Greeting("Android")
    }
}
@Composable
fun TempatItem(tempat: TempatWisata) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Versi Material 3
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Menampilkan Gambar
            Image(
                painter = painterResource(id = tempat.gambar),
                contentDescription = tempat.nama,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            // Menampilkan Nama Tempat
            Text(
                text = tempat.nama,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )
            // Menampilkan Deskripsi
            Text(text = tempat.deskripsi, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// Data class untuk menampung informasi tempat wisata
data class TempatWisata(
    val nama: String,
    val deskripsi: String,
    val gambar: Int // Ini untuk ID gambar dari folder res/drawable
)

// List data sederhana untuk ditampilkan
val daftarTempatWisata = listOf(
    TempatWisata("Tumpak Sewu", "Air terjun tercantik di Jawa Timur.", R.drawable.tumpak_sewu),
    TempatWisata("Gunung Bromo", "Matahari terbitnya bagus banget.", R.drawable.gunung_bromo)
)