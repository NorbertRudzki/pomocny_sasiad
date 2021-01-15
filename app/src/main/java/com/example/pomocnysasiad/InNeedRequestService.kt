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
import com.example.pomocnysasiad.activity.InNeedActivity
import com.example.pomocnysasiad.activity.VolunteerActivity
import com.example.pomocnysasiad.model.*
import kotlinx.coroutines.*

class InNeedRequestService : LifecycleService() {
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
        Log.d("InNeedRequestService", "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        isSearching = true
        val searchChannelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("inNeed", "Sprawdzanie stanu aktywnych zgłoszeń")
            } else {
                // If earlier version channel ID is not used
                ""
            }
        val localRepository = LocalRepository(applicationContext)
        val firebaseRepository = FirebaseRepository()

        val pendingIntentSearch = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, InNeedActivity::class.java).putExtra("searchClick",true),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        search = CoroutineScope(Dispatchers.Main).launch {

            localRepository.getAllMyRequest(firebaseRepository.getUserId()).observe(this@InNeedRequestService){ allLists ->
                Log.d("getAllMyRequest","trigger")
                if(allLists != null){
                    if(allLists.isEmpty()){
                        Log.d("getAllMyRequest","empty")
                        stopSelf()
                    }
                    Log.d("getAllMyRequest","notempty")
                    val notification: Notification = NotificationCompat.Builder(this@InNeedRequestService, searchChannelId)
                        .setContentTitle("Sprawdzam stan aktywnych zgłoszeń")
                        .setContentText("${allLists.size} zgłoszeń")
                        .setSmallIcon(R.drawable.ic_baseline_search_24)
                        .setContentIntent(pendingIntentSearch)
                        .build()
                    startForeground(3001, notification)

                    firebaseRepository.getMyChatCloudUpdate(allLists.map { it.id }).observe(this@InNeedRequestService){ list ->
                        Log.d("getInNeedChatStatusCloudUpdate","triggered")
                        if(!list.isNullOrEmpty()){
                            localRepository.updateChats(list.map { it.chat })
                            for(i in list){
                                localRepository.insertMessages(i.messages)
                                Log.d("getInNeedChatStatusCloudUpdate",i.toString())
                            }
                        }
                    }

                }
            }
        }
        search.start()


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
