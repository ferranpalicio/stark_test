package com.pal.starktest.domain.model

data class User(
    val email: String,
    val name: String,
    val phone: String? = null,
    val country: String? = null,
)
