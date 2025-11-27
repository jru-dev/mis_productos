package com.proyecto.misproducto.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.misproducto.model.Producto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    producto: Producto? = null,
    onNavigateBack: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var codigo by remember { mutableStateOf(producto?.codigo ?: "") }
    var descripcion by remember { mutableStateOf(producto?.descripcion ?: "") }
    var precio by remember { mutableStateOf(producto?.precio?.toString() ?: "") }
    var cantidad by remember { mutableStateOf(producto?.cantidad?.toString() ?: "") }
    var estado by remember { mutableStateOf(producto?.estado ?: "Activo") }
    var categoria by remember { mutableStateOf(producto?.categoria ?: "") }
    var expandedEstado by remember { mutableStateOf(false) }
    var expandedCategoria by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val estadosDisponibles = listOf("Activo", "Inactivo")
    val categoriasDisponibles = listOf(
        "Electrónica",
        "Ropa",
        "Alimentos",
        "Hogar",
        "Deportes",
        "Libros",
        "Juguetes",
        "Otros"
    )

    val isEditing = producto != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Producto" else "Agregar Producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text("Código del producto *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = precio,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        precio = it
                    }
                },
                label = { Text("Precio (S/) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("S/ ") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = cantidad,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                        cantidad = it
                    }
                },
                label = { Text("Cantidad *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown Estado
            ExposedDropdownMenuBox(
                expanded = expandedEstado,
                onExpandedChange = { expandedEstado = it }
            ) {
                OutlinedTextField(
                    value = estado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstado) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedEstado,
                    onDismissRequest = { expandedEstado = false }
                ) {
                    estadosDisponibles.forEach { estadoOption ->
                        DropdownMenuItem(
                            text = { Text(estadoOption) },
                            onClick = {
                                estado = estadoOption
                                expandedEstado = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown Categoría
            ExposedDropdownMenuBox(
                expanded = expandedCategoria,
                onExpandedChange = { expandedCategoria = it }
            ) {
                OutlinedTextField(
                    value = categoria,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedCategoria,
                    onDismissRequest = { expandedCategoria = false }
                ) {
                    categoriasDisponibles.forEach { categoriaOption ->
                        DropdownMenuItem(
                            text = { Text(categoriaOption) },
                            onClick = {
                                categoria = categoriaOption
                                expandedCategoria = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    when {
                        codigo.isBlank() || descripcion.isBlank() || precio.isBlank() ||
                                cantidad.isBlank() || categoria.isBlank() -> {
                            errorMessage = "Por favor completa todos los campos"
                        }
                        precio.toDoubleOrNull() == null || precio.toDouble() <= 0 -> {
                            errorMessage = "El precio debe ser mayor a 0"
                        }
                        cantidad.toIntOrNull() == null || cantidad.toInt() < 0 -> {
                            errorMessage = "La cantidad debe ser mayor o igual a 0"
                        }
                        else -> {
                            isLoading = true
                            errorMessage = null

                            val productoData = Producto(
                                id = producto?.id ?: "",
                                codigo = codigo,
                                descripcion = descripcion,
                                precio = precio.toDouble(),
                                cantidad = cantidad.toInt(),
                                estado = estado,
                                categoria = categoria,
                                userId = userId
                            )

                            if (isEditing) {
                                // Actualizar producto existente
                                db.collection("productos")
                                    .document(producto!!.id)
                                    .set(productoData.toMap())
                                    .addOnSuccessListener {
                                        isLoading = false
                                        onNavigateBack()
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "Error al actualizar: ${e.message}"
                                    }
                            } else {
                                // Crear nuevo producto
                                db.collection("productos")
                                    .add(productoData.toMap())
                                    .addOnSuccessListener {
                                        isLoading = false
                                        onNavigateBack()
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "Error al guardar: ${e.message}"
                                    }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditing) "Actualizar Producto" else "Guardar Producto")
                }
            }
        }
    }
}