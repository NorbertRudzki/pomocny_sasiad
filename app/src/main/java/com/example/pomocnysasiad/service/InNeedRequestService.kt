package com.example.pomocnysasiad.service

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
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.activity.InNeedActivity
import com.example.pomocnysasiad.activity.VolunteerActivity
import com.example.pomocnysasiad.model.*
import kotlinx.coroutines.*

class InNeedRequestService : LifecycleService() {
    private lateinit var search: Job
    private lateinit var channel: String
    private lateinit var previousState: List<ChatWithMessages>

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
        channel =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("inNeed", "Sprawdzanie stanu aktywnych zgłoszeń")
            } else {
                // If earlier version channel ID is not used
                ""
            }
        val localRepository = LocalRepository(applicationContext)
        val firebaseRepository = FirebaseRepository()
        val preferences = MyPreference(applicationContext)

        val pendingIntentSearch = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, InNeedActivity::class.java).putExtra("searchClick", true),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        search = CoroutineScope(Dispatchers.Main).launch {

            previousState = emptyList()
            localRepository.getAllMyRequest(firebaseRepository.getUserId())
                .observe(this@InNeedRequestService) { allLists ->
                    var startNotify = false
                    Log.d("getAllMyRequest", "trigger")
                    if (allLists != null) {
                        if (allLists.isEmpty()) {
                            Log.d("getAllMyRequest", "empty")
                            stopSelf()
                        }
                        Log.d("getAllMyRequest", "notempty")
                        val notification: Notification =
                            NotificationCompat.Builder(this@InNeedRequestService, channel)
                                .setContentTitle("Sprawdzam stan aktywnych zgłoszeń")
                                .setContentText("${allLists.size} zgłoszeń")
                                .setSmallIcon(R.drawable.ic_baseline_search_24)
                                .setContentIntent(pendingIntentSearch)
                                .build()
                        startForeground(3001, notification)

                        firebaseRepository.getMyChatCloudUpdate(allLists.map { it.id })
                            .observe(this@InNeedRequestService) { list ->
                                Log.d("getInNeedChatStatusCloudUpdate", "triggered")
                                if (!list.isNullOrEmpty()) {

                                    localRepository.updateChats(list.map { it.chat })

                                    for ((index, value) in list.withIndex()) {
                                        if (startNotify && previousState.isNotEmpty()) {
                                            var currentStatus = value.chat.status
                                            var previousStatus = previousState[index].chat.status
                                            var currentMessages = value.messages.size
                                            var previousMessages = previousState[index].messages.size
                                            if (currentStatus != previousStatus
                                                && !(currentStatus == 4 && previousStatus == 2)
                                                && currentStatus != 3
                                                && currentStatus != 7
                                            ) {
                                                if(!(currentStatus == 9 && previousStatus == 6)){
                                                    createNotificationStatus(value.chat)
                                                }
                                                if(currentStatus == 9){
                                                    localRepository.deleteRequestById(value.chat.id)
                                                    localRepository.deleteProductsByListId(value.chat.id)
                                                    localRepository.deleteChat(value.chat)
                                                    localRepository.deleteMessagesByChatId(value.chat.id)
                                                }
                                            }
                                            if (currentMessages != previousMessages && value.messages.last().userId != firebaseRepository.getUserId() && preferences.getOpenChat() != value.chat.id) {
                                                createNotificationNewMessage(value)
                                            }
                                        }
                                        localRepository.insertMessages(value.messages)
                                        Log.d("getInNeedChatStatusCloudUpdate", value.toString())
                                    }
                                    previousState = list
                                    if (!startNotify) {
                                        startNotify = true
                                    }
                                }
                            }
                    }
                }
        }
        search.start()


        return START_STICKY
    }

    private fun createNotificationStatus(chat: Chat) {
        val pendingIntentFound = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, InNeedActivity::class.java).putExtra(
                "statusChanged",
                chat.id
            ),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val contentText = when (chat.status) {
            1 -> "${chat.volunteerName} zaproponował pomoc!"
            2, 4 -> "${chat.volunteerName} potwierdził, że chce pomóc!"
            5 -> "${chat.volunteerName} zrezygnował z pomocy"
            6, 9 -> "${chat.volunteerName} potwierdził zakończenie zgłoszenia"
            else -> ""
        }
        val chatStatusChanged: Notification = NotificationCompat.Builder(
            this@InNeedRequestService,
            channel
        )
            .setContentTitle("Zmiana stanu zgłoszenia!")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_baseline_live_help_24)
            .setAutoCancel(true)
            .setContentIntent(pendingIntentFound)
            .build()
        chatStatusChanged.flags =
            chatStatusChanged.flags or (Notification.FLAG_AUTO_CANCEL or Notification.DEFAULT_LIGHTS)
        NotificationManagerCompat.from(applicationContext).notify(3002, chatStatusChanged)
    }

    private fun createNotificationNewMessage(chatWithMessages: ChatWithMessages) {
        val pendingIntentFound = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, InNeedActivity::class.java).putExtra(
                "statusChanged",
                chatWithMessages.chat.id
            ),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val chatStatusChanged: Notification = NotificationCompat.Builder(
            this@InNeedRequestService,
            channel
        )
            .setContentTitle("Nowa wiadomość od ${chatWithMessages.chat.volunteerName}")
            .setContentText(chatWithMessages.messages.last().content)
            .setSmallIcon(R.drawable.ic_baseline_live_help_24)
            .setAutoCancel(true)
            .setContentIntent(pendingIntentFound)
            .build()
        chatStatusChanged.flags =
            chatStatusChanged.flags or (Notification.FLAG_AUTO_CANCEL or Notification.DEFAULT_LIGHTS)
        NotificationManagerCompat.from(applicationContext).notify(3003, chatStatusChanged)
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
