package com.example.bab4_papb

import android.os.Bundle
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bab4_papb.ui.theme.Bab4_PAPBTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Bab4_PAPBTheme {
                RekomendasiTempatScreen()
            }
        }
    }
}

@Composable
fun RekomendasiTempatScreen() {
    // State list untuk menampung data wisata
    var daftarWisata by remember {
        mutableStateOf(listOf(
            TempatWisata("Tumpak Sewu", "Air terjun tercantik di Jawa Timur.", gambarResId = R.drawable.tumpak_sewu),
            TempatWisata("Gunung Bromo", "Matahari terbitnya bagus banget.", gambarResId = R.drawable.gunung_bromo)
        ))
    }

    // State untuk memunculkan dialog
    var showTambahDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTambahDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            LazyColumn {
                items(daftarWisata) { tempat ->
                    TempatItemEditable(
                        tempat = tempat,
                        onDelete = {
                            // Proses hapus data
                            daftarWisata = daftarWisata.filter { it != tempat }
                        }
                    )
                }
            }
        }
    }

    if (showTambahDialog) {
        TambahTempatWisataDialog(
            onDismiss = { showTambahDialog = false },
            onTambah = { nama, deskripsi, uri ->
                // Agar ada gambarnya, kita pasang gambar Bromo secara default sementara
                val baru = TempatWisata(
                    nama = nama,
                    deskripsi = deskripsi,
                    gambarResId = R.drawable.everest // Gunakan gambar yang sudah ada di drawable
                )
                daftarWisata = daftarWisata + baru
                showTambahDialog = false
            }
        )
    }
}

@Composable
fun TambahTempatWisataDialog(
    onDismiss: () -> Unit,
    onTambah: (String, String, String?) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Tempat Wisata Baru") },
        text = {
            Column {
                TextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Tempat") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isNotBlank() && deskripsi.isNotBlank()) {
                        onTambah(nama, deskripsi, null)
                    }
                }
            ) {
                Text("Tambah")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun TempatItemEditable(tempat: TempatWisata, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

data class TempatWisata(
    val nama: String = "",
    val deskripsi: String = "",
    val gambarUriString: String? = null,
    val gambarResId: Int? = null
)