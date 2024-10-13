package com.example.photogallery.api

import com.example.photogallery.api.FlickrResponse
import retrofit2.http.GET
import retrofit2.http.Query


private const val API_KEY = "877b91620d8c5def3b641524e27905e2"

interface FlickrApi {
//    @GET("/")
//    suspend fun fetchContents() : String
//
//    @GET(
//        "services/rest/?method=flickr.interestingness.getList" +
//                "&api_key=$API_KEY" +
//                "&format=json" +
//                "&nojsoncallback=1" +
//                "&extras=url_s"
//    )


//    suspend fun fetchPhotos(): FlickrResponse

    @GET("services/rest/?method=flickr.interestingness.getList") //if query is empty then run this
    suspend fun fetchPhotos(): FlickrResponse

    @GET("services/rest?method=flickr.photos.search") //if query is not empty then run this
    suspend fun searchPhotos(@Query("text") query: String): FlickrResponse

}


//most common HTTP request methods are: GET,POST,DELETE,PUT, UPDATE