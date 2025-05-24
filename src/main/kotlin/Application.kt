package com.example

//import com.example.app.configureDatabases
import com.example.app.sample.configureRouting
import com.example.app.sample.configureSerialization
import com.example.authentication.AuthenticationApp
import com.example.data.database.DatabaseFactory.initializationDatabase
import com.example.friend.FriendRouting
import com.example.friend.FriendWebSocket
import com.example.login.LoginRouting
import com.example.message.configureSockets
import com.example.news.routingNews
import com.example.user.UserRouting
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*


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
    FriendWebSocket()
    routingNews()
}
