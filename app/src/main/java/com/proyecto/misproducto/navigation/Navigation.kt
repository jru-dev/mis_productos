package com.proyecto.misproducto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.misproducto.model.Producto
import com.proyecto.misproducto.screens.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object AddProduct : Screen("add_product")
    object EditProduct : Screen("edit_product/{productId}/{codigo}/{descripcion}/{precio}/{cantidad}/{estado}/{categoria}") {
        fun createRoute(producto: Producto): String {
            return "edit_product/${producto.id}/${producto.codigo}/${producto.descripcion}/" +
                    "${producto.precio}/${producto.cantidad}/${producto.estado}/${producto.categoria}"
        }
    }
}

@Composable
fun NavigationGraph() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    val startDestination = if (auth.currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddProduct = {
                    navController.navigate(Screen.AddProduct.route)
                },
                onNavigateToEditProduct = { producto ->
                    navController.navigate(Screen.EditProduct.createRoute(producto))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AddProduct.route) {
            AddEditProductScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType },
                navArgument("codigo") { type = NavType.StringType },
                navArgument("descripcion") { type = NavType.StringType },
                navArgument("precio") { type = NavType.StringType },
                navArgument("cantidad") { type = NavType.StringType },
                navArgument("estado") { type = NavType.StringType },
                navArgument("categoria") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val producto = Producto(
                id = backStackEntry.arguments?.getString("productId") ?: "",
                codigo = backStackEntry.arguments?.getString("codigo") ?: "",
                descripcion = backStackEntry.arguments?.getString("descripcion") ?: "",
                precio = backStackEntry.arguments?.getString("precio")?.toDoubleOrNull() ?: 0.0,
                cantidad = backStackEntry.arguments?.getString("cantidad")?.toIntOrNull() ?: 0,
                estado = backStackEntry.arguments?.getString("estado") ?: "Activo",
                categoria = backStackEntry.arguments?.getString("categoria") ?: "",
                userId = auth.currentUser?.uid ?: ""
            )

            AddEditProductScreen(
                producto = producto,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}