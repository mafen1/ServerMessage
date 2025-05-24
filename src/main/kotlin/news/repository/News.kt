package com.example.news.repository

import com.example.news.model.NewsRequest

interface News{
    fun addNews(newsRequest: NewsRequest)
}
