package com.example.photogallery

import com.example.photogallery.api.FlickrApi
import com.example.photogallery.api.GalleryItem
import com.example.photogallery.api.PhotoInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

import retrofit2.create


//this class will contain most of networking code
class PhotoRepository {
    private val flickrApi: FlickrApi

    init{

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()

        flickrApi = retrofit.create()
    }


//    suspend fun fetchContents() = flickrApi.fetchContents()

//    suspend fun fetchPhotos() = flickrApi.fetchPhotos()

    suspend fun fetchPhotos(): List<GalleryItem> = flickrApi.fetchPhotos().photos.galleryItem

    suspend fun searchPhotos(query: String): List<GalleryItem> = flickrApi.searchPhotos(query).photos.galleryItem
}