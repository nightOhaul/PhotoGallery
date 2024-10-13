package com.example.photogallery.api

import com.example.photogallery.PhotoResponse
import com.squareup.moshi.JsonClass

//this is used to deserialize the json response data
@JsonClass(generateAdapter = true)
data class FlickrResponse(
    val photos: PhotoResponse
)
