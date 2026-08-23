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
import com.example.news.table.NewsTable.likedUsers
import com.example.news.table.NewsTable.nameAuthor
import com.example.news.table.NewsTable.userNameAuthor
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
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
            NewsTable.selectAll().map { it.toNewsResponse() }
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
                it[likedUsers] = emptyList()
                it[imageNews] = ""
            }
        }
    }

    override fun toggleLike(newsId: Int, userName: String): NewsResponse = transaction {
        val row = findNewsRow(newsId)
        val currentLikedUsers = row[likedUsers]
        val nextLikedUsers = if (currentLikedUsers.contains(userName)) {
            currentLikedUsers - userName
        } else {
            currentLikedUsers + userName
        }

        NewsTable.update({ NewsTable.id eq newsId }) {
            it[likedUsers] = nextLikedUsers
            it[countLike] = nextLikedUsers.size
        }

        findNewsRow(newsId).toNewsResponse()
    }

    override fun addComment(newsId: Int, userName: String, text: String): NewsResponse = transaction {
        val commentText = "$userName: ${text.trim()}"
        val row = findNewsRow(newsId)
        val nextComments = row[comments] + commentText

        NewsTable.update({ NewsTable.id eq newsId }) {
            it[comments] = nextComments
            it[countComment] = nextComments.size
        }

        findNewsRow(newsId).toNewsResponse()
    }

    private fun findNewsRow(newsId: Int): ResultRow =
        NewsTable.selectAll().where {
            NewsTable.id eq newsId
        }.firstOrNull() ?: throw IllegalArgumentException("NewsNotFound")

    private fun ResultRow.toNewsResponse() = NewsResponse(
        id = this[NewsTable.id],
        userNameAuthor = this[userNameAuthor],
        nameAuthor = this[nameAuthor],
        date = this[date],
        countLike = this[countLike],
        countComment = this[countComment],
        avatarAuthor = this[avatarString],
        description = this[description],
        comment = this[comments],
        newsImage = this[imageNews],
        likedUsers = this[likedUsers]
    )

}
