package com.proyecto.misproducto.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.misproducto.model.Producto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Producto) -> Unit,
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productoToDelete by remember { mutableStateOf<Producto?>(null) }

    // Cargar productos del usuario actual
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("productos")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    isLoading = false
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        productos = snapshot.documents.mapNotNull { doc ->
                            Producto(
                                id = doc.id,
                                codigo = doc.getString("codigo") ?: "",
                                descripcion = doc.getString("descripcion") ?: "",
                                precio = doc.getDouble("precio") ?: 0.0,
                                cantidad = doc.getLong("cantidad")?.toInt() ?: 0,
                                estado = doc.getString("estado") ?: "Activo",
                                categoria = doc.getString("categoria") ?: "",
                                userId = doc.getString("userId") ?: ""
                            )
                        }
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis Productos")
                        Text(
                            text = auth.currentUser?.displayName ?: auth.currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                showMenu = false
                                auth.signOut()
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ExitToApp, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddProduct) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                productos.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay productos registrados",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Presiona el botón + para agregar uno",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(productos) { producto ->
                            ProductoCard(
                                producto = producto,
                                onEdit = { onNavigateToEditProduct(producto) },
                                onDelete = {
                                    productoToDelete = producto
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && productoToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar producto") },
            text = { Text("¿Estás seguro de eliminar '${productoToDelete!!.descripcion}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        db.collection("productos")
                            .document(productoToDelete!!.id)
                            .delete()
                        showDeleteDialog = false
                        productoToDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    productoToDelete = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.descripcion,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Código: ${producto.codigo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Precio: S/ ${String.format("%.2f", producto.precio)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Cantidad: ${producto.cantidad}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    AssistChip(
                        onClick = { },
                        label = { Text(producto.estado) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (producto.estado == "Activo")
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = producto.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}