package com.example.user.repository

import com.example.login.model.LoginRequest
import com.example.user.model.User
import com.example.user.model.UserRequest
import com.example.user.model.UserResponse

interface UserRepository {

    fun addUser(user: User)
     fun allUser(): List<UserResponse>
    fun addFriends(userName: String)
    fun findUser(user: User): User
    fun findUserToken(token: String): User
    fun findUserByUserName(userName: UserRequest): UserResponse
    fun findUserByStr(string: UserRequest): List<UserResponse>

    fun findUserByUserNamePassword(loginRequest: LoginRequest): User
    fun findUserUserName(userName: String): User

}