package com.orangeskystorm.kithenhelper.ui.notifications

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.*
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.GridView
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.orangeskystorm.kithenhelper.R
import com.orangeskystorm.kithenhelper.SetTimeViewModel
import com.orangeskystorm.kithenhelper.TimerAlarmHandler
import com.orangeskystorm.kithenhelper.db.TimeItem
import java.util.*


class TimePickerFragment : DialogFragment(), OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c[Calendar.HOUR_OF_DAY]
        val minute = c[Calendar.MINUTE]

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(
            getActivity(), this, hour, minute,
            DateFormat.is24HourFormat(getActivity())
        )
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user
    }
}


class TimerFragment : Fragment() {

    private fun getStopwatchStatus() {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.GET_STATUS)
        context?.startService(stopwatchService)
    }

    private fun startStopwatch(stopWatchId: Int) {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.START)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ID,
            stopWatchId.toString()
        )
        context?.startService(stopwatchService)
    }

    private fun pauseStopwatch(stopWatchId: Int) {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.PAUSE)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ID,
            stopWatchId.toString()
        )
        context?.startService(stopwatchService)
    }

    private fun toggleStopwatchRunning(stopWatchId: Int) {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.TOGGLE_STOPWATCH_RUNNING)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ID,
            stopWatchId.toString()
        )
        context?.startService(stopwatchService)
    }

    private fun resetStopwatch() {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.RESET)
        context?.startService(stopwatchService)
    }

    private fun moveToForeground() {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ACTION,
            StopwatchService.MOVE_TO_FOREGROUND
        )
        context?.startService(stopwatchService)
    }

    private fun moveToBackground() {
        val stopwatchService = Intent(context, StopwatchService::class.java)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ACTION,
            StopwatchService.MOVE_TO_BACKGROUND
        )
        context?.startService(stopwatchService)
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
        context?.startService(stopwatchService)
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

        context?.startService(stopwatchIntent)
    }

    private fun onSetTime(stopWatchId: Int, time: Int) {
        timeToFinish[stopWatchId] = time
    }

    val TIMERS_PREFERENCES = "TIMERS_PREFERENCES"

    val TIMERS_NUMBER = "TIMERS_NUMBER"

//    private var _binding: TimerFragmentBinding? = null
//
//    // This property is only valid between onCreateView and
//    // onDestroyView.
//    private val binding get() = _binding!!

    private val setTimeViewModel: SetTimeViewModel by activityViewModels()

    val timers: MutableList<TimeItem> = ArrayList()

    var timersNumber = 4;
    var timersNumberDefault = 4;

    var timeToFinish = mutableMapOf<Int, Int>()
    var isRunningArray = mutableMapOf<Int, Boolean>()
    var isTimeFinishedArray = mutableMapOf<Int, Boolean>()




    // inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val timersSettings = activity?.getSharedPreferences(TIMERS_PREFERENCES, Context.MODE_PRIVATE);

        val editor: SharedPreferences.Editor = timersSettings?.edit()!!

        timersNumber = timersSettings.getInt(TIMERS_NUMBER, timersNumberDefault)




        // Log.d("hjh", "TimerFragment onCreate")
        val view: View = inflater.inflate(R.layout.fragmet_timer, container, false)!!

        val gridView =  view?.findViewById<View>(R.id.timers_grid) as GridView

        val addNewTimerButton =  view?.findViewById<View>(R.id.addNewTimer) as FloatingActionButton

        val deleteTimerButton =  view?.findViewById<View>(R.id.deleteTimer) as FloatingActionButton


        addNewTimerButton.setOnClickListener(){
            timersNumber += 1
            editor.putInt(TIMERS_NUMBER, timersNumber)
            editor.apply()

            timers.add(TimeItem(id=1, type="timer", timeLeft = 2000))
            gridView.invalidateViews();
        }

        deleteTimerButton.setOnClickListener(){
            if (timers.count() != 0) {
                timersNumber -= 1
                editor.putInt(TIMERS_NUMBER, timersNumber)
                editor.apply()

                timers.removeLast()
                gridView.invalidateViews();
            }
        }

        val timerFragmentAdaptor = getActivity()?.let { TimerFragmentAdaptor(it,viewLifecycleOwner, timers, timeToFinish, isRunningArray, isTimeFinishedArray/*, onSetTime*/) }
        gridView.adapter = timerFragmentAdaptor

        gridView.invalidateViews();

        setTimeViewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            if (item?.timeLeft != null) {
                setAlarm(item?.timeLeft!!)
            }
        }

        for (i in 1.rangeTo(timersNumber)) {
            timers.add(TimeItem(id=1, type="timer", timeLeft = 2000))

            isTimeFinishedArray[i] = false
            // Receiving time values from service
        }


        val timeFilter = IntentFilter()
        timeFilter.addAction(StopwatchService.STOPWATCH_TICK)

        val timeReceiver = object : BroadcastReceiver() {
            var t = object {

            }


            override fun onReceive(p0: Context?, p1: Intent?) {
                val timeElapsed = p1?.getIntExtra(StopwatchService.TIME_ELAPSED, 0)!!

                val receivedStopWatchId = p1?.getIntExtra(StopwatchService.STOPWATCH_ID, 0)!!
                val action = p1?.getAction();
                timeToFinish[receivedStopWatchId] = timeElapsed;
                isRunningArray[receivedStopWatchId] = true
                if (timeElapsed == 0 && receivedStopWatchId < 100) {
                    pauseStopwatch(receivedStopWatchId)

                    isTimeFinishedArray[receivedStopWatchId] = true

                } else {
                    isTimeFinishedArray[receivedStopWatchId] = false
                }
                gridView.invalidateViews();
            }
        }

        context?.registerReceiver(timeReceiver, timeFilter)

        return view
    }
    override fun onResume() {
        // Log.d("hjh", "TimerFragment onResume")
        super.onResume()
    }
    fun setAlarm(timeInMillis: Long) {
        val alarmManager =
            getActivity()?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(activity, TimerAlarmHandler::class.java)
        //val intent = getActivity()?.Intent(this, TimerAlarmHandler::class.java)
        var pendingIntent: PendingIntent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent =
                PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            pendingIntent = PendingIntent.getBroadcast(
                getActivity(),
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )
        }
        /*alarmManager.setRepeating(
        AlarmManager.RTC,
        timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )*/
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        Toast.makeText(getActivity(), "Alarm is set", Toast.LENGTH_SHORT).show()
    }
}


