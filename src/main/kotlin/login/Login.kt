package com.example.login

import com.example.login.model.LoginResponse
import com.example.data.user.model.User

interface Login {
    fun createJWT(user: User): LoginResponse
    fun validateToken(token: String): Boolean
    fun validateUser(user: User): Boolean
}