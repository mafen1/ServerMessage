package com.example.friend.repository


import com.example.friend.model.FriendRequest
import com.example.friend.table.FriendRequestTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class FriendImpl: Friend {


    override fun addRequestFriend(friendRequest: FriendRequest) {
        transaction {
            FriendRequestTable.insert {
                it[id] = friendRequest.id
                it[senderUserName] = friendRequest.senderUserName
                it[receiver] = friendRequest.receiverUserName
                it[status] = friendRequest.status
            }
        }
    }

//    override fun listFriends(userName: String): List<String> {
//        transaction {
//            UserTable.selectAll().where {
//                FriendRequestTable.senderUserName eq userName
//            }
//                .firstOrNull()
//                .toString()
//        }
//    }


}