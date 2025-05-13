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
            UserTable.insert {
                it[id] = user.id
                it[name] = user.name
                it[username] = user.userName
                it[friend] = listOf()
                it[token] = user.token.toString()
            }
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

    // todo доделать
    override fun addFriends(userName: String) {
        val listUserName = mutableListOf<String>().add(userName)

        transaction {
            UserTable.insert {
                it[friend]
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
                UserTable.username eq userRequest.username
            }
                .firstOrNull()
                ?.toUserResponse()
                ?: throw IllegalArgumentException("UserNotFound")
        }
    }

    override fun findUserByStr(string: UserRequest): List<UserResponse> {
        val str = "${string.username}%"
        return transaction {
            UserTable.selectAll().where {
                UserTable.username like str
            }
                .map { it.toUserResponse() }

        }
    }

    override fun findUserByUserNamePassword(loginRequest: LoginRequest): User {
        return transaction {
            UserTable.selectAll().where{
                UserTable.username eq loginRequest.userName
                UserTable.name eq loginRequest.password
            }
                .firstOrNull()
                ?.toUser()
                ?: throw IllegalArgumentException("UserNotFound")
        }
    }
    // todo переделать данную функцию, так как есть такая же функция
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
        friend = this[UserTable.friend],
        token = this[UserTable.token]
    )

    private fun ResultRow.toUserResponse() = UserResponse(
        userName = this[UserTable.username],
        name = this[UserTable.name]
    )


}




