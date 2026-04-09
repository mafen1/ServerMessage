package com.example.friend.repository

import com.example.friend.model.FriendRequest

interface Friend {
    fun addRequestFriend(friendRequest: FriendRequest)
    fun acceptFriend(senderUsername: String, receiverUsername: String): Boolean
    fun rejectFriend(senderUsername: String, receiverUsername: String): Boolean
    fun getFriends(username: String): List<String>
    fun getFriendRequests(username: String): List<FriendRequest>
    fun hasPendingRequest(sender: String, receiver: String): Boolean
    fun isAlreadyFriends(sender: String, receiver: String): Boolean
}