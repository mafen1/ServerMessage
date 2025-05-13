package com.example.login

import com.example.login.model.LoginRequest
import com.example.login.model.LoginResponse
import com.example.user.model.User

interface Login {
    fun createJWT(user: User): LoginResponse
    fun validateToken(token: String): Boolean
    fun validateUser(user: User): Boolean
    fun loginAccount(loginRequest: LoginRequest): User
    fun validateUserByUserName(userName: String): Boolean
}