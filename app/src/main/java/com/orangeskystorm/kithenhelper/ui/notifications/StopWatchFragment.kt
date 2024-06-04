package com.orangeskystorm.kithenhelper.ui.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.orangeskystorm.kithenhelper.R
import com.orangeskystorm.kithenhelper.SetTimeViewModel
import com.orangeskystorm.kithenhelper.TimerAlarmHandler
import com.orangeskystorm.kithenhelper.db.TimeItem
import java.util.ArrayList

class StopWatchFragment : Fragment() {

    val STOPWATCH_PREFERENCES = "STOPWATCH_PREFERENCES"

    val STOPWATCH_NUMBER = "STOPWATCH_NUMBER"


    private val setTimeViewModel: SetTimeViewModel by activityViewModels()

    val stopwatches: MutableList<TimeItem> = ArrayList()

    var isRunningArray = mutableMapOf<Int, Boolean>()
    var timeToFinish = mutableMapOf<Int, Int>()

    var stopwatchNumber = 4;
    var stopwatchNumberDefault = 4;
    val positionDelta = 100




    // inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Log.d("hjh", "TimerFragment onCreate")
        val view: View = inflater.inflate(R.layout.fragment_stop_watch, container, false)!!

        val stopwatchSettings = activity?.getSharedPreferences(STOPWATCH_PREFERENCES, Context.MODE_PRIVATE);

        val editor: SharedPreferences.Editor = stopwatchSettings?.edit()!!

        stopwatchNumber = stopwatchSettings.getInt(STOPWATCH_NUMBER, stopwatchNumberDefault)


        for (i in 1.rangeTo(stopwatchNumber)) {
            stopwatches.add(TimeItem(id=1, type="timer", timeLeft = 2000))
            timeToFinish[i - 1 + positionDelta] = 0
            isRunningArray[i - 1 + positionDelta] = false
        }

        val gridView =  view?.findViewById<View>(R.id.timers_grid) as GridView

        val addNewTimerButton =  view?.findViewById<View>(R.id.addNewTimer) as FloatingActionButton

        val deleteTimerButton =  view?.findViewById<View>(R.id.deleteStopWatch) as FloatingActionButton


        addNewTimerButton.setOnClickListener(){
            stopwatchNumber += 1
            editor.putInt(STOPWATCH_NUMBER, stopwatchNumber)
            editor.apply()


            stopwatches.add(TimeItem(id=1, type="timer", timeLeft = 2000))
            timeToFinish[stopwatches.count() - 1 + positionDelta] = 0
            isRunningArray[isRunningArray.count() - 1 + positionDelta] = false
            gridView.invalidateViews();
        }

        deleteTimerButton.setOnClickListener(){
            if (stopwatches.count() != 0) {
                stopwatchNumber -= 1
                editor.putInt(STOPWATCH_NUMBER, stopwatchNumber)
                editor.apply()

                stopwatches.removeLast()
                gridView.invalidateViews();
            }
        }

        val timerFragmentAdaptor = getActivity()?.let { StopWatchFragmentAdaptor(it,viewLifecycleOwner, stopwatches, timeToFinish, isRunningArray) }
        gridView.adapter = timerFragmentAdaptor

        gridView.invalidateViews();


        val timeFilter = IntentFilter()
        timeFilter.addAction(StopwatchService.STOPWATCH_TICK)

        val timeReceiver = object : BroadcastReceiver() {

            var t = object {

            }


            override fun onReceive(p0: Context?, p1: Intent?) {
                Log.d("ffds" , "onReceive")
                val timeElapsed = p1?.getIntExtra(StopwatchService.TOTAL_SECONDS, 0)!!

                val receivedStopWatchId = p1?.getIntExtra(StopwatchService.STOPWATCH_ID, 0)!!

                timeToFinish[receivedStopWatchId] = timeElapsed;
                Log.d("fsd", "timeElapsed = " + timeElapsed)
                gridView.invalidateViews();
            }
        }

        context?.registerReceiver(timeReceiver, timeFilter)


        setTimeViewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            if (item?.timeLeft != null) {
                setAlarm(item?.timeLeft!!)
            }
        }

        return view
    }
    override fun onResume() {
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
