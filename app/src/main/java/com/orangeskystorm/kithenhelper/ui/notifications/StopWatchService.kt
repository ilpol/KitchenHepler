package com.orangeskystorm.kithenhelper.ui.notifications


import android.app.*
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.orangeskystorm.kithenhelper.MainActivity
import com.orangeskystorm.kithenhelper.R
import java.util.*

class StopwatchService : Service() {
    companion object {

        const val defaultTimeElapsed = 10
        // Channel ID for notifications
        const val CHANNEL_ID = "Stopwatch_Notifications"

        // Service Actions
        const val START = "START"
        const val PAUSE = "PAUSE"
        const val RESET = "RESET"
        const val SET_TIME_ELAPSED = "SET_TIME_ELAPSED"
        const val SET_TOTAL_SECONDS = "SET_TOTAL_SECONDS"
        const val GET_STATUS = "GET_STATUS"
        const val MOVE_TO_FOREGROUND = "MOVE_TO_FOREGROUND"
        const val MOVE_TO_BACKGROUND = "MOVE_TO_BACKGROUND"
        const val TOGGLE_STOPWATCH_RUNNING = "TOGGLE_STOPWATCH_RUNNING"

        const val STOP_ALARM = "STOP_ALARM"

        // Intent Extras
        const val STOPWATCH_ACTION = "STOPWATCH_ACTION"
        const val TIME_ELAPSED = "TIME_ELAPSED"
        const val TOTAL_SECONDS = "TOTAL_SECONDS"
        const val IS_STOPWATCH_RUNNING = "IS_STOPWATCH_RUNNING"
        const val STOPWATCH_ID = "STOPWATCH_ID"


        // Intent Actions
        const val STOPWATCH_TICK = "STOPWATCH_TICK"
        const val STOPWATCH_STATUS = "STOPWATCH_STATUS"
    }

    private var timeElapsed = mutableMapOf<Int, Int>()
    private var totalSeconds = mutableMapOf<Int, Int>()
    private var isStopWatchRunning = mutableMapOf<Int, Boolean>()

    private var updateTimer = mutableMapOf<Int, Timer>()
    private var stopwatchTimer = mutableMapOf<Int, Timer>()

    private var players = mutableMapOf<Int, MediaPlayer?>()

    // Getting access to the NotificationManager
    private lateinit var notificationManager: NotificationManager

    /*
    * The system calls onBind() method to retrieve the IBinder only when the first client binds.
    * The system then delivers the same IBinder to any additional clients that bind,
    * without calling onBind() again.
    * */
    override fun onBind(p0: Intent?): IBinder? {
        // Log.d("Stopwatch", "Stopwatch onBind")
        // startStopwatch()
        return null
    }


    /*
    * onStartCommand() is called every time a client starts the service
    * using startService(Intent intent)
    * We will check for what action has this service been called for and then perform the
    * action accordingly. The action is extracted from the intent that is used to start
    * this service.
    * */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        getNotificationManager()

        val action = intent?.getStringExtra(STOPWATCH_ACTION)!!
        // Log.d("Stopwatch", "onStartCommand action: $action")
        var stopWatchId = intent?.getStringExtra(STOPWATCH_ID)?.toInt()
        // Log.d("Stopwatch", "onStartCommand stopWatchId: $stopWatchId")
        if (stopWatchId == null) {
            stopWatchId = 0
        }

        var timeToSetElsapsedString = intent?.getStringExtra(SET_TIME_ELAPSED)

        // Log.d("Stopwatch", "onStartCommand timeToSetElsapsedString: $timeToSetElsapsedString")

        var timeToSetElsapsed = intent?.getStringExtra(SET_TIME_ELAPSED)?.toInt()
        // Log.d("Stopwatch", "onStartCommand timeToSetElsapsed: $timeToSetElsapsed")
//        if (timeToSetElsapsed == null) {
//            timeToSetElsapsed = 0
//        }

        var totalSecondsToSet = intent?.getStringExtra(SET_TOTAL_SECONDS)?.toInt()
        // Log.d("Stopwatch", "onStartCommand totalSecondsToSet: $totalSecondsToSet")
//        if (totalSecondsToSet == null) {
//            totalSecondsToSet = 0
//        }



        when (action) {
            START -> startStopwatch(stopWatchId)
            PAUSE -> pauseStopwatch(stopWatchId)
            RESET -> resetStopwatch(stopWatchId)
            SET_TIME_ELAPSED -> setTimeElapsed(stopWatchId, timeToSetElsapsed)
            SET_TOTAL_SECONDS -> setTotalSeconds(stopWatchId, totalSecondsToSet)
            GET_STATUS -> sendStatus(stopWatchId)
            MOVE_TO_FOREGROUND -> moveToForeground(stopWatchId)
            MOVE_TO_BACKGROUND -> moveToBackground(stopWatchId)
            STOP_ALARM -> stopAlarm(stopWatchId)
            TOGGLE_STOPWATCH_RUNNING -> toggleStopwatchRunning(stopWatchId)
        }

        return START_STICKY
    }

    /*
    * This function is triggered when the app is not visible to the user anymore
    * It check if the stopwatch is running, if it is then it starts a foreground service
    * with the notification.
    * We run another timer to update the notification every second.
    * */
    private fun moveToForeground(stopWatchId: Int) {

        isStopWatchRunning.forEach { (stopWatchId, isRunning) ->
            if (isRunning) {
                startForeground(1, buildNotification(stopWatchId))

                updateTimer[stopWatchId] = Timer()

                updateTimer[stopWatchId]?.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        updateNotification(stopWatchId)

                    }
                }, 0, 1000)
            }

        }
    }

    /*
    * This function is triggered when the app is visible again to the user
    * It cancels the timer which was updating the notification every second
    * It also stops the foreground service and removes the notification
    * */
    private fun moveToBackground(stopWatchId: Int) {
        updateTimer[stopWatchId]?.cancel()
        stopForeground(true)
    }

    /*
    * This function starts the stopwatch
    * Sets the status of stopwatch running to true
    * We start a Timer and increase the timeElapsed by 1 every second and broadcast the value
    * with the action of STOPWATCH_TICK.
    * We will receive this broadcast in the MainActivity to get access to the time elapsed.
    * */
    private fun startStopwatch(stopWatchId: Int) {
        isStopWatchRunning[stopWatchId] = true

        sendStatus(stopWatchId)

        stopwatchTimer[stopWatchId] = Timer()
        val context = this
        stopwatchTimer[stopWatchId]?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val stopwatchIntent = Intent()
                stopwatchIntent.action = STOPWATCH_TICK

                var isStopWatch = false
                if (stopWatchId >= 100) {
                    isStopWatch = true
                }


                val timeElapsedVal = timeElapsed[stopWatchId]
                if (timeElapsedVal != null) {
                    if ( timeElapsed[stopWatchId] != null) {
                        timeElapsed[stopWatchId] = timeElapsed[stopWatchId]!! - 1
                    }
                } else {
                    timeElapsed[stopWatchId] = defaultTimeElapsed
                }

                var totalSecondsVal = totalSeconds[stopWatchId]
                if (totalSecondsVal != null) {
                    if (totalSeconds[stopWatchId] != null) {
                        totalSeconds[stopWatchId] = totalSeconds[stopWatchId]!! + 1
                    }
                } else {
                    totalSeconds[stopWatchId] = 0
                    totalSecondsVal = totalSeconds[stopWatchId]
                }

//                Log.d("ffds" , "service totalSecondsVal = " + totalSecondsVal)
//                Log.d("ffds" , "service timeElapsedVal = " + timeElapsedVal)
//                Log.d("ffds" , "service totalSecondsVal = " + totalSecondsVal)
//





                if (timeElapsed[stopWatchId] == 0 && !isStopWatch) {
                    var player = players[stopWatchId]
                    if (player == null) {
                        player = MediaPlayer.create(context, R.raw.bell_sound)
                    }
                    players[stopWatchId] = player
                    player?.start()
                    player?.isLooping = true

                    Log.d("ffds" , "service plaaaaaaayyyyy = ")

                }

                // Log.d("ffds" , "service timeElapsed = " + timeElapsed)

                stopwatchIntent.putExtra(TIME_ELAPSED, timeElapsed[stopWatchId])
                stopwatchIntent.putExtra(TOTAL_SECONDS, totalSeconds[stopWatchId])
                stopwatchIntent.putExtra(STOPWATCH_ID, stopWatchId)
                sendBroadcast(stopwatchIntent)
            }
        }, 0, 1000)
    }

    /*
    * This function pauses the stopwatch
    * Sends an update of the current state of the stopwatch
    * */
    private fun pauseStopwatch(stopWatchId: Int) {
        stopwatchTimer[stopWatchId]?.cancel()
        isStopWatchRunning[stopWatchId] = false
        Log.d("ffds" , "service   pauseStopwatch  stopwatchTimer[stopWatchId] = " +  stopwatchTimer[stopWatchId])
        //sendStatus(stopWatchId)
    }

    private fun toggleStopwatchRunning(stopWatchId: Int) {
        if (isStopWatchRunning[stopWatchId] == true) {
            isStopWatchRunning[stopWatchId] = false
            stopwatchTimer[stopWatchId]?.cancel()
        } else {
            startStopwatch(stopWatchId)
        }

        Log.d("ffds" , "service   toggleStopwatchRunning  isStopWatchRunning[stopWatchId] = " +  isStopWatchRunning[stopWatchId])

        //sendStatus(stopWatchId)
    }

    private fun stopAlarm(stopWatchId: Int) {

        val player = players[stopWatchId]
        // Log.d("ffds" , "service   stopAlarm player = " + player)
        if (player != null) {
            players[stopWatchId]?.stop();
            players[stopWatchId]?.release();
            players[stopWatchId]= null;
        }
    }

    private fun setTimeElapsed(stopWatchId: Int, timeToSetElapsed: Int?) {
        if (timeToSetElapsed !== null) {
            timeElapsed[stopWatchId] = timeToSetElapsed
        }
    }

    private fun setTotalSeconds(stopWatchId: Int, totalSecondsToSet: Int?) {
        if (totalSecondsToSet != null) {
            totalSeconds[stopWatchId] = totalSecondsToSet
        }
    }

    /*
    * This function resets the stopwatch
    * Sends an update of the current state of the stopwatch
    * */
    private fun resetStopwatch(stopWatchId: Int) {
        pauseStopwatch(stopWatchId)
        timeElapsed[stopWatchId] = 0
        sendStatus(stopWatchId)
    }

    /*
    * This function is responsible for broadcasting the status of the stopwatch
    * Broadcasts if the stopwatch is running and also the time elapsed
    * */
    private fun sendStatus(stopWatchId: Int) {
        val statusIntent = Intent()
        statusIntent.action = STOPWATCH_STATUS
        statusIntent.putExtra(IS_STOPWATCH_RUNNING, isStopWatchRunning[stopWatchId])
        statusIntent.putExtra(TIME_ELAPSED, timeElapsed[stopWatchId])
        statusIntent.putExtra(TOTAL_SECONDS, totalSeconds[stopWatchId])
        sendBroadcast(statusIntent)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Stopwatch",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setSound(null, null)
            notificationChannel.setShowBadge(true)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun getNotificationManager() {
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
    }

    /*
    * This function is responsible for building and returning a Notification with the current
    * state of the stopwatch along with the timeElapsed
    * */
    private fun buildNotification(stopWatchId: Int): Notification {
        val title = if (isStopWatchRunning[stopWatchId] == true) {
            "Stopwatch is running!"
        } else {
            "Stopwatch is paused!"
        }

        val hours: Int? = timeElapsed[stopWatchId]?.div(60)?.div(60)
        val minutes: Int? = timeElapsed[stopWatchId]?.div(60)
        val seconds: Int? = timeElapsed[stopWatchId]?.rem(60)

        val intent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setOngoing(true)
            .setContentText(
                "${"%02d".format(hours)}:${"%02d".format(minutes)}:${
                    "%02d".format(
                        seconds
                    )
                }"
            )
            .setColorized(true)
            .setColor(Color.parseColor("#BEAEE2"))
            //.setSmallIcon(R.drawable.ic_clock)
            .setOnlyAlertOnce(true)
            .setContentIntent(pIntent)
            .setAutoCancel(true)
            .build()
    }


    /*
    * This function uses the notificationManager to update the existing notification with the new notification
    * */
    private fun updateNotification(stopWatchId: Int) {
        notificationManager.notify(
            1,
            buildNotification(stopWatchId)
        )
    }
}
