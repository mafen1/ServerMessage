package com.example.user.repository

import com.example.data.database.table.UserTable
import com.example.login.model.LoginRequest
import com.example.user.model.User
import com.example.user.model.UserRequest
import com.example.user.model.UserResponse
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepositoryImpl() : UserRepository {


    override fun addUser(user: User) {
        transaction {
            val userId = UserTable.insert {
                it[name] = user.name
                it[username] = user.userName
                it[listUserName] = listOf()
                it[token] = user.token.toString()
                it[password] = user.password ?: ""
            } get UserTable.id
        }
    }

    override fun allUser(): List<UserResponse> = transaction {
        UserTable.selectAll().map {
            UserResponse(
                userName = it[UserTable.username],
                name = it[UserTable.name]
            )
        }
    }

    override fun addFriends(userName: String) {
        transaction {
            UserTable.insert {
                it[this.listUserName]
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

    override fun findUserByUserName(userRequest: UserRequest): UserResponse {
        return transaction {
            UserTable.selectAll().where {
                UserTable.username eq userRequest.userName
            }
                .firstOrNull()
                ?.toUserResponse()
                ?: throw IllegalArgumentException("UserNotFound")
        }
    }

    override fun findUserByStr(string: UserRequest): List<UserResponse> {
        val str = "${string.userName}%"
        return transaction {
            UserTable.selectAll().where {
                UserTable.username like str
            }
                .map { it.toUserResponse() }

        }
    }
    // todo password
    override fun findUserByUserNamePassword(loginRequest: LoginRequest): User {
        return transaction {
            UserTable.selectAll().where{
                UserTable.username eq loginRequest.userName
                UserTable.name eq loginRequest.name
                UserTable.password eq loginRequest.password
            }
                .firstOrNull()
                ?.toUser()
                ?: throw IllegalArgumentException("UserNotFound")
        }
    }
    override fun findUserUserName(userName: String): User {
        return transaction {
            UserTable.selectAll().where{
                UserTable.username eq userName
            }
                .firstOrNull()
                ?.toUser()
                ?: throw IllegalArgumentException("UserNotFound")
        }
    }

    private fun ResultRow.toUser() = User(
        id = this[UserTable.id],
        name = this[UserTable.name],
        userName = this[UserTable.username],
        listUserName = this[UserTable.listUserName],
        token = this[UserTable.token],
        password = this[UserTable.password]
    )

    private fun ResultRow.toUserResponse() = UserResponse(
        userName = this[UserTable.username],
        name = this[UserTable.name]
    )


}




