package com.example.di

import com.example.friend.FriendWebSocketManager
import com.example.friend.FriendWebSocketManagerImpl
import com.example.friend.repository.Friend
import com.example.friend.repository.FriendImpl
import com.example.login.JwtSettings
import com.example.login.Login
import com.example.login.LoginImpl
import com.example.message.WebSocketManager
import com.example.message.WebSocketManagerImpl
import com.example.message.repository.MessageRepoImpl
import com.example.message.repository.MessageRepository
import com.example.news.repository.News
import com.example.news.repository.NewsImpl
import com.example.user.repository.UserRepository
import com.example.user.repository.UserRepositoryImpl
import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module

fun appModule(config: ApplicationConfig) = module {
    single { JwtSettings.from(config) }

    single<UserRepository> { UserRepositoryImpl() }
    single<MessageRepository> { MessageRepoImpl() }
    single<Friend> { FriendImpl() }
    single<News> { NewsImpl() }

    single<WebSocketManager> { WebSocketManagerImpl() }
    single<FriendWebSocketManager> { FriendWebSocketManagerImpl() }

    single<Login> { LoginImpl(get(), get()) }
}
