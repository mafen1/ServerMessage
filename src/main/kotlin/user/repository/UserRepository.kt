package com.example.user.repository

import com.example.user.model.User
import com.example.user.model.UserRequest
import com.example.user.model.UserResponse

interface UserRepository {

    fun addUser(user: User)
     fun allUser(): List<UserResponse>
    fun addFriends(userName: String)
    fun findUser(user: User): User
    fun findUserToken(token: String): User
    fun findUserByName(userName: UserRequest): UserResponse
}