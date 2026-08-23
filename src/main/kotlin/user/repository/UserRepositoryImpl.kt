package com.example.user.repository

import com.example.data.database.table.UserTable
import com.example.login.model.LoginRequest
import com.example.security.PasswordHasher
import com.example.user.model.User
import com.example.user.model.UserRequest
import com.example.user.model.UserResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepositoryImpl : UserRepository {

    override fun addUser(
        name: String,
        userName: String,
        password: String,
        listUserName: List<String>
    ) {
        transaction {
            val alreadyExists = UserTable.selectAll().where {
                UserTable.username eq userName
            }.count() > 0

            if (alreadyExists) {
                throw IllegalArgumentException("UserAlreadyExists")
            }

            UserTable.insert {
                it[UserTable.name] = name
                it[UserTable.username] = userName
                it[UserTable.listUserName] = listUserName
                it[UserTable.password] = PasswordHasher.hash(password)
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

    override fun findUser(userName: String): User {
        return transaction {
            UserTable.selectAll().where {
                UserTable.username eq userName
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
        val raw = string.userName.trim()
        val normalized = raw.removePrefix("@").trim()
        if (normalized.isBlank()) return emptyList()
        // кейс-инсенситив поиск подстроки, работает как с "@test" так и "test"
        val pattern = "%$normalized%"
        return transaction {
            UserTable.selectAll().where {
                UserTable.username.lowerCase() like pattern.lowercase()
            }
                .map { it.toUserResponse() }

        }
    }

    override fun findUserByUserNamePassword(loginRequest: LoginRequest): User {
        return transaction {
            val row = UserTable.selectAll().where {
                UserTable.username eq loginRequest.userName
            }
                .firstOrNull()
                ?: throw IllegalArgumentException("UserNotFound")

            val passwordHash = row[UserTable.password]
            if (!PasswordHasher.verify(loginRequest.password, passwordHash)) {
                throw IllegalArgumentException("InvalidPassword")
            }

            row.toUser()
        }
    }

    override fun findUserUserName(userName: String): User {
        return transaction {
            UserTable.selectAll().where {
                UserTable.username eq userName
            }
                .firstOrNull()
                ?.toUser()
                ?: throw IllegalArgumentException("UserNotFound")
        }
    }

    override fun existsByUserName(userName: String): Boolean = transaction {
        UserTable.selectAll().where {
            UserTable.username eq userName
        }.count() > 0
    }

    override fun updateProfile(userName: String, name: String, password: String?): User = transaction {
        val updated = UserTable.update({ UserTable.username eq userName }) {
            it[UserTable.name] = name
            if (!password.isNullOrBlank()) {
                it[UserTable.password] = PasswordHasher.hash(password)
            }
        }

        if (updated == 0) {
            throw IllegalArgumentException("UserNotFound")
        }

        UserTable.selectAll().where {
            UserTable.username eq userName
        }
            .firstOrNull()
            ?.toUser()
            ?: throw IllegalArgumentException("UserNotFound")
    }

    override fun updatePublicKey(userName: String, publicKey: String) {
        transaction {
            val updated = UserTable.update({ UserTable.username eq userName }) {
                it[UserTable.publicKey] = publicKey
            }
            if (updated == 0) {
                throw IllegalArgumentException("UserNotFound")
            }
        }
    }

    override fun getPublicKey(userName: String): String? = transaction {
        UserTable.selectAll()
            .where { UserTable.username eq userName }
            .firstOrNull()
            ?.let { it[UserTable.publicKey] }
    }

    private fun ResultRow.toUser() = User(
        id = this[UserTable.id],
        name = this[UserTable.name],
        userName = this[UserTable.username],
        listUserName = this[UserTable.listUserName]
    )

    private fun ResultRow.toUserResponse() = UserResponse(
        userName = this[UserTable.username],
        name = this[UserTable.name]
    )

}
