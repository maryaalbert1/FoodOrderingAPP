package com.example.myapplication

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val role: String = ""   // "Buyer" or "Seller"
)