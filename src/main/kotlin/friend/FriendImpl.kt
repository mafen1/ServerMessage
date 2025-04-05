package com.example.friend


import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class FriendImpl: Friend {


    override fun addRequestFriend(friendRequest: FriendRequest) {
        transaction {
            FriendRequestTable.insert {
                it[id] = friendRequest.id
                it[FriendRequestTable.senderUserName] = friendRequest.senderUserName
                it[FriendRequestTable.receiver] = friendRequest.receiverUserName
                it[FriendRequestTable.status] = friendRequest.status
            }
        }
    }



}