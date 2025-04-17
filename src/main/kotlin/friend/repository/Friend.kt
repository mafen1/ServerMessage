package com.example.friend.repository

import com.example.friend.model.FriendRequest

interface Friend {
    fun addRequestFriend(friendRequest: FriendRequest)
//    fun listFriends(userName: String): List<String>

}