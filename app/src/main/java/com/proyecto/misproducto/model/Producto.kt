package com.proyecto.misproducto.model

data class Producto(
    val id: String = "",
    val codigo: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 0,
    val estado: String = "Activo",
    val categoria: String = "",
    val userId: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "codigo" to codigo,
            "descripcion" to descripcion,
            "precio" to precio,
            "cantidad" to cantidad,
            "estado" to estado,
            "categoria" to categoria,
            "userId" to userId
        )
    }
}