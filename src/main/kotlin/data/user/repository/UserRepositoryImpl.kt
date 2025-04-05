package com.example.data.user.repository

import com.example.data.user.model.User
import com.example.data.database.table.UserTable
import com.example.data.user.model.UserRequest
import com.example.data.user.model.UserResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepositoryImpl() : UserRepository {


    override fun addUser(user: User) {
        transaction {
            UserTable.insert {
                it[UserTable.id] = user.id
                it[UserTable.name] = user.name
                it[UserTable.username] = user.userName
                it[UserTable.friend] = listOf()
                it[UserTable.token] = user.token.toString()
            }
        }
    }

    override fun allUser(): List<User> = transaction {
        UserTable.selectAll().map {
            User(
                it[UserTable.id],
                it[UserTable.name],
                it[UserTable.username],
                it[UserTable.friend],
                it[UserTable.token]
            )
        }
    }

    override fun addFriends(userName: String) {
        val listUserName = mutableListOf<String>().add(userName)

        transaction {
            UserTable.insert {
                it[UserTable.friend]
            }
        }
    }

    override fun findUser(user: User): User {
       return transaction {
           UserTable.selectAll().where {
               UserTable.username eq user.userName
           }
               .firstOrNull()
               ?.toUser()
               ?: throw IllegalArgumentException("UserNotFound")
        }
    }

    override fun findUserToken(token: String): User {
        return transaction {
            UserTable.selectAll().where {
                UserTable.token eq token
            }
                .firstOrNull()
                ?.toUser()
                ?: throw IllegalArgumentException("UserNotFound")
        }
    }

    override fun findUserByName(userRequest: UserRequest): UserResponse {
        return transaction {
            UserTable.selectAll().where {
                UserTable.username eq userRequest.username
            }
                .firstOrNull()
                ?.toUserResponse()
                ?: throw  IllegalArgumentException("UserNotFound")
        }
    }

    private fun ResultRow.toUser() = User(
        id = this[UserTable.id],
        name = this[UserTable.name],
        userName = this[UserTable.username],
        friend = this[UserTable.friend],
        token = this[UserTable.token]
    )

    private fun ResultRow.toUserResponse() = UserResponse(
        userName = this[UserTable.username],
        name = this[UserTable.name]
    )
}




