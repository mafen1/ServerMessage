package com.example.news.repository

import com.example.news.model.NewsRequest
import com.example.news.model.NewsResponse

interface News {
    fun addNews(newsRequest: NewsRequest, news: String)
    fun allNews(): List<NewsResponse>
    fun uploadNewsWithOutImage(newsWithOutImage: NewsRequest)
    fun toggleLike(newsId: Int, userName: String): NewsResponse
    fun addComment(newsId: Int, userName: String, text: String): NewsResponse
}
