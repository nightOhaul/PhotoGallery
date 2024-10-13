package com.example.photogallery

import android.content.Intent
import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.photogallery.databinding.FragmentPhotoGalleryBinding
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.photogallery.api.FlickrApi
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.create
import androidx.fragment.app.viewModels

import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// Binding the data of photo gallery fragment in this class

private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"
class PhotoGalleryFragment : Fragment() {

    private var searchView: SearchView? = null
    private var _binding: FragmentPhotoGalleryBinding? = null
    private var pollingMenuItem: MenuItem?= null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentPhotoGalleryBinding.inflate(inflater, container, false)
        binding.photoGrid.layoutManager = GridLayoutManager(context, 3)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        //creating an instance of the work Manager

//        val constrains = Constraints.Builder() //using constraints we can understand that some conditions need to be met before working.
//            .setRequiredNetworkType(NetworkType.UNMETERED) //this is telling that to use photo gallery we need to connect with the wifi(unmetered network)
//            .build()
//        val workRequest = OneTimeWorkRequest
//            .Builder(PollWorker::class.java)
//            .setConstraints(constrains)
//            .build()
//        WorkManager.getInstance(requireContext())
//            .enqueue(workRequest)
    }






    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        // Example of how to access a SearchView if you have one
        val searchItem: MenuItem? = menu.findItem(R.id.menu_item_search)
         searchView = searchItem?.actionView as? SearchView

        pollingMenuItem = menu.findItem(R.id.menu_item_toggle_polling)

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, "QueryTextSubmit: $query")
                photoGalleryViewModel.setQuery(query ?: "")
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "QueryTextChange: $newText")
                return false
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId){
                R.id.menu_item_clear -> {
                    photoGalleryViewModel.setQuery("")
                    true
                }

                R.id.menu_item_toggle_polling -> {
                    photoGalleryViewModel.toggleIsPolling()
                    true
                }


                else -> onOptionsItemSelected(item)


            }
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        pollingMenuItem = null
    }

    // Use MenuProvider instead of deprecated methods for options menu handling
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


       // Ensure the menu is tied to the fragment's lifecycle

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                photoGalleryViewModel.galleryItems.collect { items ->
//                    binding.photoGrid.adapter = PhotoListAdapter(items)
//                }
                photoGalleryViewModel.uiState.collect { state ->

//                    binding.photoGrid.adapter = PhotoListAdapter(state.images)

                    binding.photoGrid.adapter = PhotoListAdapter(
                        state.images
                    ){photoPageUri ->
                        val intent = Intent(Intent.ACTION_VIEW, photoPageUri)
                        startActivity(intent)
                    }
                    updatePollingState(state.isPolling)

                    searchView?.setQuery(state.query, false)

                }
            }
        }
    }

    private fun updatePollingState(isPolling: Boolean){
        val toggleItemTitle = if(isPolling){
            R.string.stop_polling
        }
        else{
            R.string.start_polling
        }

        pollingMenuItem?.setTitle(toggleItemTitle)


        if (isPolling) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
            val periodicRequest =
                PeriodicWorkRequestBuilder<PollWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                POLL_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        } else {
            WorkManager.getInstance(requireContext()).cancelUniqueWork(POLL_WORK)
        }

    }


}