package com.example

//import com.example.app.configureDatabases
import com.example.app.DatabaseFactory.initializationDatabase
import com.example.app.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {


    embeddedServer(CIO, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)


}



fun Application.module() {
//    configureSockets()
//    configureFrameworks()

//    configureDatabases()
//    configureMonitoring()
//    configureHTTP()
//    configureSecurity()
    initializationDatabase()
    configureSerialization()
    configureRouting()
}
