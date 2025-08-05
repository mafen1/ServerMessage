package com.example.news.repository

import com.example.data.database.table.UserTable
import com.example.news.model.NewsRequest
import com.example.news.table.NewsTable
import com.example.user.model.UserResponse
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class NewsImpl: News {

    override fun addNews(newsRequest: NewsRequest) {
        transaction {
            NewsTable.insert {
                it[id] =  newsRequest.id
                it[name] = newsRequest.userName
                it[text] = newsRequest.text
                it[data] = newsRequest.image
            }
        }
    }

    override fun allNews(): List<NewsRequest> {
        return transaction {
            NewsTable.selectAll().map {
                NewsRequest(
                    id = it[NewsTable.id],
                    userName = it[NewsTable.name],
                    image = it[NewsTable.data],
                    text = it[NewsTable.text]
                )
            }
        }
    }

    private fun ResultRow.toNews() = NewsRequest(
        id = this[NewsTable.id],
        userName = this[NewsTable.name],
        image = this[NewsTable.data],
        text = this[NewsTable.text]
    )

}