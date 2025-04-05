package com.example.data.user.repository

import com.example.data.user.model.User
import com.example.data.user.model.UserRequest
import com.example.data.user.model.UserResponse

interface UserRepository {

    fun addUser(user: User)
     fun allUser(): List<User>
    fun addFriends(userName: String)
    fun findUser(user: User): User
    fun findUserToken(token: String): User
    fun findUserByName(userName: UserRequest): UserResponse
}