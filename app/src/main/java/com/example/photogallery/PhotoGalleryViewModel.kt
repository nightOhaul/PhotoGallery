package com.example.photogallery

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photogallery.api.GalleryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update

private const val TAG = "PhotoGalleryViewModel"

@SuppressLint("SuspiciousIndentation")


class PhotoGalleryViewModel: ViewModel() {

    private val photoRepository = PhotoRepository()
    private val preferencesRepository = PreferencesRepository.get()


//    private val _galleryItems: MutableStateFlow<List<GalleryItem>> =
//        MutableStateFlow(emptyList())
//
//    val galleryItems: StateFlow<List<GalleryItem>>
//        get() = _galleryItems.asStateFlow()

    private val _uiState: MutableStateFlow<photoGalleryUiState> = MutableStateFlow(
        photoGalleryUiState()
    )

    val uiState: StateFlow<photoGalleryUiState>
        get() = _uiState.asStateFlow()

    init{
        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true)
            preferencesRepository.storedQuery.collectLatest { storedQuery ->
                try {
//                val items = photoRepository.fetchPhotos()
//                val items = photoRepository.searchPhotos("Formula 1")
                  val items = fetchGalleryItems(storedQuery)
//                Log.d(TAG, "Items Received: $items")


//                    _uiState.value = items
//                    _uiState.update { oldState ->
//                        oldState.copy(
//                            images = items,
//                            query = storedQuery,
//
//                            )
//                        }

                    _uiState.value = photoGalleryUiState(
                        images = items,
                        query = storedQuery
                    )

                    Log.d(TAG, "images updated: $items")
                    Log.d(TAG, "query updated: $storedQuery")
                }


                 catch (ex: Exception) {
                    Log.e(TAG, "Failed to fetch images", ex)
                }
            }
        }

        viewModelScope.launch{
            preferencesRepository.isPolling.collect{isPolling ->
                _uiState.update{it.copy(isPolling = isPolling)}
            }
        }
    }

    fun toggleIsPolling(){
        viewModelScope.launch{
            preferencesRepository.setPolling(!uiState.value.isPolling)
        }
    }

    fun setQuery(query: String){
//        viewModelScope.launch { _galleryItems.value = fetchGalleryItems(query) }
        viewModelScope.launch { preferencesRepository.setStoredQuery(query) }
    }

    private suspend fun fetchGalleryItems(query: String): List<GalleryItem>{
        return if(query.isEmpty()){
            photoRepository.fetchPhotos()
        }
        else{
            photoRepository.searchPhotos(query)
        }
    }
}


data class photoGalleryUiState(
    val images: List<GalleryItem> = listOf(),
    val query: String = "",
    val isPolling: Boolean = false,
    )

