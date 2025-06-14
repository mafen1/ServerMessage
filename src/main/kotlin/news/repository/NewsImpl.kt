package com.example.news.repository

import com.example.news.model.NewsRequest
import com.example.news.table.NewsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class NewsImpl: News {

    override fun addNews(newsRequest: NewsRequest) {
        transaction {
            NewsTable.insert {
                it[id] =  newsRequest.id
                it[name] = newsRequest.userName
                it[text] = newsRequest.text
            }
        }
    }

}