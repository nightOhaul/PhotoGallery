package com.example.photogallery

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract.Contacts.Photo
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first


private const val TAG = "PollWorker"


class PollWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters){
    override suspend fun doWork(): Result { //doWork is called from bg thread and in that we can do all our work.
//        Log.d(TAG, "work request triggers")

        val preferenceRepository = PreferencesRepository.get()
        val photoRepository = PhotoRepository()

        val query = preferenceRepository.storedQuery.first()
        val lastResultId = preferenceRepository.lastResultId.first()

        if(query.isEmpty()){
            Log.i(TAG, "No saved query")
            return Result.success()
        }




//        return Result.success()
        return try{
            val items = photoRepository.searchPhotos(query)
            if(items.isNotEmpty()){
                val newResultId = items.first().id
                if(newResultId == lastResultId){
                    Log.d(TAG, "same photo found: $newResultId") //similarly doing here
                }
                else{
                    Log.d(TAG, "new photo found: $newResultId") //getting the String Flow in a int format in logcat
                    preferenceRepository.setLastResultId(newResultId)
                        notifyUser()
                }
            }

            Result.success()
        } catch(ex: Exception){
            Log.e(TAG, "background updated failed", ex)
            Result.failure()
        }

    }

    private fun notifyUser() {
        val intent = MainActivity.newIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE //to check what happens when user clicks on notification
        )
        val resources = context.resources
        val notification = NotificationCompat
            .Builder(context, NOTIFICATION_CHANNEL_ID)
            .setTicker(resources.getString(R.string.new_pictures_title))
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setContentTitle(resources.getString(R.string.new_pictures_title))
            .setContentText(resources.getString(R.string.new_pictures_text))
            .setContentIntent(pendingIntent) //to check things done after user clicks on notification
            .setAutoCancel(true)
            .build()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            return
        }
        NotificationManagerCompat.from(context).notify(0, notification)
    }
}