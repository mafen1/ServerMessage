package com.example.news.repository

import com.example.news.model.NewsRequest
import com.example.news.model.NewsWithOutImage

interface News {
    fun addNews(newsRequest: NewsRequest)
    fun allNews(): List<NewsRequest>
    fun uploadNewsWithOutImage(newsWithOutImage: NewsWithOutImage)
}
