package com.orangeskystorm.kithenhelper.ui.notifications

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


public class TimerFragmentAdaptor(private val context: Activity, viewLifecycleOwner: LifecycleOwner, private val timers: MutableList<TimeItem>, timeToFinish: MutableMap<Int, Int>, isRunningArray: MutableMap<Int, Boolean>, isTimeFinishedArray: MutableMap<Int, Boolean>/*, onSetTime: (stopWatchId: Int, time: Int)-> Unit*/)
    : ArrayAdapter<String>(context, R.layout.timer_item) {
    var timersInner: MutableList<TimeItem> = mutableListOf<TimeItem>()
    var isStopwatchRunning = false
    var isRunning = false;
    val timeToFinishDefault = 10

    var timeToFinishInner = mutableMapOf<Int, Int>()
    var isRunningArrayInner = mutableMapOf<Int, Boolean>()
    var isTimeFinishedArrayInner = mutableMapOf<Int, Boolean>()

    init {

        timers.forEachIndexed { index, element ->
            timeToFinish[index] = timeToFinishDefault
            isRunningArray[index] = false
        }
        timersInner = timers;
        timeToFinishInner = timeToFinish
        isRunningArrayInner = isRunningArray
        isTimeFinishedArrayInner = isTimeFinishedArray
    }

    private fun getStopwatchStatus() {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.GET_STATUS)
        context.startService(stopwatchService)
    }

    private fun startStopwatch(stopWatchId: Int) {
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
    Log.d("ffds" , "setTimeElapsed timeToSet = " + timeToSet)
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

        // Log.d("ffds" , "stopAlarm stopWatchId = " + stopWatchId)

        context.startService(stopwatchIntent)
    }

    override fun getCount(): Int {
        return timersInner.count()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        getStopwatchStatus()

        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.timer_item, null, true)

        val timeLeftView = rowView.findViewById(R.id.time_left) as TextView
        val nameView = rowView.findViewById(R.id.name) as TextView
        val startStopButton = rowView.findViewById(R.id.buttonStartStop) as TextView
        val setTimeButton = rowView.findViewById(R.id.buttonSetTime) as TextView

        if (isTimeFinishedArrayInner[position] == true) {
            val anim: Animation = AlphaAnimation(0.0f, 1.0f)
                    anim.duration = 500 //You can manage the blinking time with this parameter

                    anim.startOffset = 20
                    anim.repeatMode = Animation.REVERSE
                    anim.repeatCount = Animation.INFINITE
                        startStopButton.startAnimation(anim)
                        startStopButton.setBackgroundColor(Color.RED)
        }


        nameView.text = context.getResources().getString(R.string.timer) + " " + (position + 1)

        fun updateView() {

            var hours = 0
            var minutes = 0
            var seconds = timeToFinishDefault
            if (timeToFinishInner[position] != null) {
                hours = timeToFinishInner[position]?.div(3600)!!;
                minutes = (timeToFinishInner[position]?.rem(3600))?.div(60)!!;
                seconds = timeToFinishInner[position]?.rem(60)!!;
            }

            if (isRunningArrayInner[position] == true) {
                startStopButton.text = context.getResources().getString(R.string.stop)
            } else {
                startStopButton.text = context.getResources().getString(R.string.start)
            }

            timeLeftView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        }
        updateView()
        // Receiving time values from service
        val timeFilter = IntentFilter()
        timeFilter.addAction(StopwatchService.STOPWATCH_TICK)

        startStopButton.setOnClickListener(){
                stopAlarm(position);
            isTimeFinishedArrayInner[position] = false

            startStopButton.setBackgroundColor(context.getResources().getColor(R.color.purple_500))
            startStopButton.clearAnimation()
            if (timeToFinishInner[position] != 0) {
                toggleStopwatchRunning(position)
            }
            if (timeToFinishInner[position] == 0) {
                setTimeElapsed(position, timeToFinishDefault)
                timeToFinishInner[position] = timeToFinishDefault
            }
            if (isRunningArrayInner[position] != null) {
                isRunningArrayInner[position] = !isRunningArrayInner[position]!!
            }
            updateView()

        }

        setTimeButton.setOnClickListener(){
            stopAlarm(position);
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setTitle("Title")

            val hours = NumberPicker(context)
            hours.minValue = 0
            hours.maxValue = 24

            val minutes = NumberPicker(context)
            minutes.minValue = 0
            minutes.maxValue = 60

            val seconds = NumberPicker(context)
            seconds.minValue = 0
            seconds.maxValue = 60

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(60, 0, 0, 0)
            minutes.setLayoutParams(lp)
            seconds.setLayoutParams(lp)

            val ll = LinearLayout(context)
            ll.orientation = LinearLayout.HORIZONTAL
            ll.gravity = Gravity.CENTER_HORIZONTAL
            ll.addView(hours)
            ll.addView(minutes)
            ll.addView(seconds)
            builder.setView(ll)

            builder.setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, which ->

                    // onSetTime(hours.value * 60 * 60 + minutes.value * 60 + seconds.value)
                    timeToFinishInner[position] = hours.value * 60 * 60 + minutes.value * 60 + seconds.value
                    timeToFinishInner[position]?.let { it1 -> setTimeElapsed(position, it1) }
                    updateView()
                })
            builder.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

            builder.show()

        }

        return rowView
    }
}