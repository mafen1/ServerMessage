package com.example.user.repository

import com.example.login.model.LoginRequest
import com.example.user.model.User
import com.example.user.model.UserRequest
import com.example.user.model.UserResponse

interface UserRepository {

    fun addUser(name: String, userName: String, password: String, listUserName: List<String> = emptyList())
    fun allUser(): List<UserResponse>
    fun findUser(userName: String): User
    fun findUserByUserName(userRequest: UserRequest): UserResponse
    fun findUserByStr(string: UserRequest): List<UserResponse>

    fun findUserByUserNamePassword(loginRequest: LoginRequest): User
    fun findUserUserName(userName: String): User
    fun existsByUserName(userName: String): Boolean
    fun updateProfile(userName: String, name: String, password: String?): User
    fun updatePublicKey(userName: String, publicKey: String)
    fun getPublicKey(userName: String): String?

}
