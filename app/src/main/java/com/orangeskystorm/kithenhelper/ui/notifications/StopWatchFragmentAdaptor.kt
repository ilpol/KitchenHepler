package com.orangeskystorm.kithenhelper.ui.notifications

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.graphics.Color
import android.media.MediaPlayer
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.orangeskystorm.kithenhelper.R
import com.orangeskystorm.kithenhelper.db.TimeItem
import java.util.*


public class StopWatchFragmentAdaptor(private val context: Activity, viewLifecycleOwner: LifecycleOwner, private val timers: MutableList<TimeItem>, timeToFinish: MutableMap<Int, Int>, isRunningArray: MutableMap<Int, Boolean>)
    : ArrayAdapter<String>(context, R.layout.timer_item) {
    var timersInner: MutableList<TimeItem> = mutableListOf<TimeItem>()
    var timeToFinishInner: MutableMap<Int, Int> = mutableMapOf<Int, Int>()
    var isRunningArrayInner: MutableMap<Int, Boolean> = mutableMapOf<Int, Boolean>()
    var isStopwatchRunning = false
    init {
        timersInner = timers;
        timeToFinishInner = timeToFinish
        isRunningArrayInner = isRunningArray
    }

    private fun getStopwatchStatus() {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.GET_STATUS)
        context.startService(stopwatchService)
    }

    private fun startStopwatch(stopWatchId: Int) {
        // Log.d("ffds" , "startStopwatch  stopWatchId = " + stopWatchId)
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.START)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ID,
            stopWatchId.toString()
        )
        context.startService(stopwatchService)
    }

    private fun pauseStopwatch(stopWatchId: Int) {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.PAUSE)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ID,
            stopWatchId.toString()
        )
        context.startService(stopwatchService)
    }

    private fun toggleStopwatchRunning(stopWatchId: Int) {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.TOGGLE_STOPWATCH_RUNNING)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ID,
            stopWatchId.toString()
        )
        context.startService(stopwatchService)
    }

    private fun resetStopwatch() {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.RESET)
        context.startService(stopwatchService)
    }

    private fun moveToForeground() {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ACTION,
            StopwatchService.MOVE_TO_FOREGROUND
        )
        context.startService(stopwatchService)
    }

    private fun moveToBackground() {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ACTION,
            StopwatchService.MOVE_TO_BACKGROUND
        )
        context.startService(stopwatchService)
    }

    private fun setTimeElapsed(stopWatchId: Int, timeToSet: Int) {
//        Log.d("ffds" , "setTimeElapsed stopWatchId = " + stopWatchId)
//        Log.d("ffds" , "setTimeElapsed timeToSet = " + timeToSet)
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ACTION,
            StopwatchService.SET_TIME_ELAPSED
        )

        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ID,
            stopWatchId.toString()
        )

        stopwatchService.putExtra(
            StopwatchService.SET_TIME_ELAPSED,
            timeToSet.toString()
        )
        context.startService(stopwatchService)
    }

    private fun setTotalSeconds(stopWatchId: Int, totalSecondsToSet: Int) {
        // Log.d("ffds" , "setTimeElapsed stopWatchId = " + stopWatchId)
        // Log.d("ffds" , "setTimeElapsed totalSecondsToSet = " + totalSecondsToSet)
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ACTION,
            StopwatchService.SET_TOTAL_SECONDS
        )

        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ID,
            stopWatchId.toString()
        )

        stopwatchService.putExtra(
            StopwatchService.SET_TOTAL_SECONDS,
            totalSecondsToSet.toString()
        )
        context.startService(stopwatchService)
    }

    private fun stopAlarm(stopWatchId: Int) {
        val stopwatchIntent = Intent(context, StopwatchService::class.java)
        stopwatchIntent.putExtra(
            StopwatchService.STOPWATCH_ACTION,
            StopwatchService.STOP_ALARM
        )

        stopwatchIntent.putExtra(
            StopwatchService.STOPWATCH_ID,
            stopWatchId.toString()
        )

        Log.d("ffds" , "stopAlarm stopWatchId = " + stopWatchId)

        context.startService(stopwatchIntent)
    }

    override fun getCount(): Int {
        return timersInner.count()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        Log.d("ffds" , "getView position = " + position)

        // Moving the service to background when the app is visible
        // moveToBackground()

        getStopwatchStatus()

        val timeToFinishDefault = 0
        var isRunning = false;

        val positionDelta = 100

        var timeToFinish = 0
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.stopwatch_item, null, true)



        // val titleText = rowView.findViewById(R.id.title) as TextView
        val timeLeftView = rowView.findViewById(R.id.time_left) as TextView
        val nameView = rowView.findViewById(R.id.name) as TextView
        val startStopButton = rowView.findViewById(R.id.buttonStartStop) as TextView
        val setTimeButton = rowView.findViewById(R.id.buttonSetTime) as TextView


        nameView.text = context.getResources().getString(R.string.stopwatch) + " " + (position + 1)

        fun updateView() {
            var timeToFinish = timeToFinishInner[position + positionDelta]

            Log.d("fds","position = " + position)
            Log.d("fds","timeToFinishInner[position] = " + timeToFinishInner[position + positionDelta])

            val hours = timeToFinish?.div(3600);
            val minutes = (timeToFinish?.rem(3600))?.div(60);
            val seconds = timeToFinish?.rem(60);

            if (isRunningArrayInner[position + positionDelta] == true) {
                startStopButton.text = context.getResources().getString(R.string.stop)
            } else {
                startStopButton.text = context.getResources().getString(R.string.start)
            }

            timeLeftView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        }
        updateView()

        val time = Timer()

        startStopButton.setOnClickListener(){
            toggleStopwatchRunning(position + positionDelta)
            if (isRunningArrayInner[position + positionDelta] != null) {
                isRunningArrayInner[position + positionDelta] = !isRunningArrayInner[position + positionDelta]!!
            }
            if (isRunningArrayInner[position + positionDelta] == true) {
                startStopButton.text = context.getResources().getString(R.string.stop)
            } else {
                startStopButton.text = context.getResources().getString(R.string.start)
            }

        }

        setTimeButton.setOnClickListener(){
            setTotalSeconds(position + positionDelta, 0)
            timeToFinish = 0
            timeToFinishInner[position + positionDelta] = 0
            updateView()

        }

        return rowView
    }
}