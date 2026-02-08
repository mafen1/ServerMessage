package com.example.news.repository

import com.example.news.model.NewsRequest
import com.example.news.model.NewsResponse
import com.example.news.table.NewsTable
import com.example.news.table.NewsTable.avatarString
import com.example.news.table.NewsTable.comments
import com.example.news.table.NewsTable.countComment
import com.example.news.table.NewsTable.countLike
import com.example.news.table.NewsTable.date
import com.example.news.table.NewsTable.description
import com.example.news.table.NewsTable.imageNews
import com.example.news.table.NewsTable.nameAuthor
import com.example.news.table.NewsTable.userNameAuthor
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class NewsImpl : News {

    override fun addNews(newsRequest: NewsRequest, newsImage: String) {
        transaction {
            NewsTable.insert {
                it[userNameAuthor] = newsRequest.userNameAuthor
                it[nameAuthor] = newsRequest.nameAuthor
                it[date] = newsRequest.date
                it[countLike] = newsRequest.countLike
                it[countComment] = newsRequest.countComment
                it[avatarString] = newsRequest.avatarAuthor
                it[description] = newsRequest.description
                it[comments] = newsRequest.comment
                it[imageNews] = newsImage

            }
        }
    }

    override fun allNews(): List<NewsResponse> {
        return transaction {
            NewsTable.selectAll().map {
                NewsResponse(
                    userNameAuthor = it[userNameAuthor],
                    nameAuthor = it[nameAuthor],
                    date = it[date],
                    countLike = it[countLike],
                    countComment = it[countComment],
                    avatarAuthor = it[avatarString],
                    description = it[description],
                    comment = it[comments],
                    newsImage = it[imageNews]
                )
            }
        }
    }

    override fun uploadNewsWithOutImage(newsWithOutImage: NewsRequest) {
        transaction {
            NewsTable.insert {
                it[userNameAuthor] = newsWithOutImage.userNameAuthor
                it[nameAuthor] = newsWithOutImage.nameAuthor
                it[date] = newsWithOutImage.date
                it[countLike] = newsWithOutImage.countLike
                it[countComment] = newsWithOutImage.countComment
                it[avatarString] = newsWithOutImage.avatarAuthor
                it[description] = newsWithOutImage.description
                it[comments] = newsWithOutImage.comment
            }
        }
    }

//    private fun ResultRow.toNews() = NewsRequest(
//        id = this[NewsTable.id],
//        userName = this[NewsTable.name],
//        image = this[NewsTable.data],
//        text = this[NewsTable.text]
//    )

}