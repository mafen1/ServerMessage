package com.example.friend.repository


import com.example.data.database.table.UserTable
import com.example.friend.model.FriendRequest
import com.example.friend.table.FriendRequestTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class FriendImpl: Friend {

    override fun addRequestFriend(friendRequest: FriendRequest) {
        transaction {
            FriendRequestTable.insert {
                it[senderUserName] = friendRequest.senderUserName
                it[receiver] = friendRequest.receiverUserName
                it[status] = friendRequest.status
            }
        }
    }

    override fun acceptFriend(senderUsername: String, receiverUsername: String): Boolean {
        return transaction {
            val request = FriendRequestTable.selectAll().where {
                (FriendRequestTable.senderUserName eq senderUsername) and
                (FriendRequestTable.receiver eq receiverUsername) and
                (FriendRequestTable.status eq "pending")
            }.firstOrNull()

            if (request == null) return@transaction false

            FriendRequestTable.update({
                (FriendRequestTable.senderUserName eq senderUsername) and
                (FriendRequestTable.receiver eq receiverUsername)
            }) {
                it[status] = "accepted"
            }

            val senderFriends = UserTable.selectAll().where {
                UserTable.username eq senderUsername
            }.firstOrNull()?.get(UserTable.listUserName) ?: emptyList()

            val receiverFriends = UserTable.selectAll().where {
                UserTable.username eq receiverUsername
            }.firstOrNull()?.get(UserTable.listUserName) ?: emptyList()

            if (!senderFriends.contains(receiverUsername)) {
                UserTable.update({ UserTable.username eq senderUsername }) {
                    it[listUserName] = senderFriends + receiverUsername
                }
            }

            if (!receiverFriends.contains(senderUsername)) {
                UserTable.update({ UserTable.username eq receiverUsername }) {
                    it[listUserName] = receiverFriends + senderUsername
                }
            }

            true
        }
    }

    override fun rejectFriend(senderUsername: String, receiverUsername: String): Boolean {
        return transaction {
            val deleted = FriendRequestTable.deleteWhere {
                (FriendRequestTable.senderUserName eq senderUsername) and
                (FriendRequestTable.receiver eq receiverUsername) and
                (FriendRequestTable.status eq "pending")
            }
            deleted > 0
        }
    }

    override fun getFriends(username: String): List<String> {
        return transaction {
            val user = UserTable.selectAll().where {
                UserTable.username eq username
            }.firstOrNull()
            
            if (user == null) {
                println("getFriends: User '$username' not found in database")
                return@transaction emptyList()
            }
            
            val friends = user.get(UserTable.listUserName)
            println("getFriends for '$username': $friends")
            
            friends ?: emptyList()
        }
    }

    override fun getFriendRequests(username: String): List<FriendRequest> {
        return transaction {
            FriendRequestTable.selectAll().where {
                (FriendRequestTable.receiver eq username) and
                (FriendRequestTable.status eq "pending")
            }.map {
                FriendRequest(
                    id = it[FriendRequestTable.id],
                    senderUserName = it[FriendRequestTable.senderUserName],
                    receiverUserName = it[FriendRequestTable.receiver],
                    status = it[FriendRequestTable.status]
                )
            }
        }
    }

    override fun hasPendingRequest(sender: String, receiver: String): Boolean {
        return transaction {
            FriendRequestTable.selectAll().where {
                (FriendRequestTable.senderUserName eq sender) and
                (FriendRequestTable.receiver eq receiver) and
                (FriendRequestTable.status eq "pending")
            }.count() > 0
        }
    }

    override fun isAlreadyFriends(sender: String, receiver: String): Boolean {
        return transaction {
            val senderFriends = UserTable.selectAll().where {
                UserTable.username eq sender
            }.firstOrNull()?.get(UserTable.listUserName) ?: emptyList()
            senderFriends.contains(receiver)
        }
    }
}