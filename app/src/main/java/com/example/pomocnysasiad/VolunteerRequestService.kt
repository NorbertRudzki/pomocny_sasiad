package com.example.pomocnysasiad

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import com.example.pomocnysasiad.activity.VolunteerActivity
import com.example.pomocnysasiad.model.*
import kotlinx.coroutines.*

class VolunteerRequestService : LifecycleService() {
    private lateinit var search: Job

    companion object {
        var isSearching = false
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("Service", "create")
        isSearching = true
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SearchRequestService", "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        isSearching = true
        val searchChannelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("searching", "Szukanie nowego zgłoszenia")
            } else {
                // If earlier version channel ID is not used
                ""
            }
        val localRepository = LocalRepository(applicationContext)
        val firebaseRepository = FirebaseRepository()
        val preferences = MyPreference(applicationContext)
        val locationService = LocationService(applicationContext, preferences.getLocation())

        val pendingIntentSearch = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, VolunteerActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        search = CoroutineScope(Dispatchers.Main).launch {


            val searchNewRequests = firebaseRepository.noticeNewRequest(
                Filter(
                    locationService.getLatZone(preferences.getRange().toDouble()),
                    locationService.getLongZone(preferences.getRange().toDouble()),
                    locationService.getLongNearbyPoints(preferences.getRange().toDouble())
                )
            )

            searchNewRequests.observe(this@VolunteerRequestService) {
                Log.d("DODANO, można myslec o powiadomieniu", it.toString())
                val pendingIntentFound = PendingIntent.getActivity(
                    applicationContext,
                    0,
                    Intent(applicationContext, VolunteerActivity::class.java).putExtra("notified",2002),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )


                val newRequestNotification: Notification = NotificationCompat.Builder(
                    this@VolunteerRequestService,
                    searchChannelId
                )
                    .setContentTitle("Znaleziono nowe zgłoszenie w okolicy!")
                    .setContentText("${it.userInNeedName} potrzebuje Twojej pomocy")
                    .setSmallIcon(R.drawable.ic_baseline_live_help_24)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntentFound)
                    .build()
                newRequestNotification.flags =
                    newRequestNotification.flags or (Notification.FLAG_AUTO_CANCEL or Notification.DEFAULT_LIGHTS)
                NotificationManagerCompat.from(applicationContext).notify(2002, newRequestNotification)
                isSearching = false
                searchNewRequests.removeObservers(this@VolunteerRequestService)
            }

            localRepository.getAllAcceptedRequest(firebaseRepository.getUserId()).observe(this@VolunteerRequestService){ allLists ->
                firebaseRepository.getMyChatCloudUpdate(allLists.map { it.id }).observe(this@VolunteerRequestService){ list ->
                    Log.d("getVolunteerChatStatusCloudUpdate","triggered")
                    if(!list.isNullOrEmpty()){
                        localRepository.updateChats(list.map { it.chat })
                        for(i in list){
                            localRepository.insertMessages(i.messages)
                            Log.d("getVolunteerChatStatusCloudUpdate",i.toString())
                        }
                    }
                }
            }
        }
        search.start()




        val notification: Notification = NotificationCompat.Builder(this, searchChannelId)
            .setContentTitle("Sprawdzam, czy ktoś potrzebuje pomocy")
            .setContentText("zasięg poszukiwań: ${preferences.getRange()}km")
            .setSmallIcon(R.drawable.ic_baseline_search_24)
            .setContentIntent(pendingIntentSearch)
            .build()
        startForeground(2001, notification)



        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_LOW
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Service", "destroy")
        search.cancel()
        isSearching = false
    }
}
