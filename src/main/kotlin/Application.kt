package com.example

//import com.example.app.configureDatabases
import com.example.login.LoginRouting
import com.example.app.sample.configureRouting
import com.example.data.database.DatabaseFactory.initializationDatabase
import com.example.app.sample.configureSerialization
import com.example.authentication.AuthenticationApp
import com.example.data.message.configureSockets
import com.example.data.user.UserRouting
import com.example.friend.FriendRequest
import com.example.friend.FriendRouting
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.channels.Channel


val chanel = Channel<FriendRequest>(capacity = Channel.UNLIMITED)

fun main() {


    embeddedServer(CIO, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)


}



fun Application.module() {
    configureSockets()
//    configureFrameworks()

//    configureDatabases()
//    configureMonitoring()
//    configureHTTP()
//    configureSecurity()
    initializationDatabase()
    configureSerialization()
    AuthenticationApp()
    configureRouting()
    LoginRouting()
    FriendRouting()
    UserRouting()
}
