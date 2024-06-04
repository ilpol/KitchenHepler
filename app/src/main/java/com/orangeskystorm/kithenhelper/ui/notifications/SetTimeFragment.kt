package com.orangeskystorm.kithenhelper.ui.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
import com.orangeskystorm.kithenhelper.R
import com.orangeskystorm.kithenhelper.TimerAlarmHandler

import androidx.fragment.app.activityViewModels
import com.orangeskystorm.kithenhelper.SetTimeViewModel
import com.orangeskystorm.kithenhelper.db.TimeItem
import java.util.*

class SetTimeFragment : Fragment() {

    private val setTimeViewModel: SetTimeViewModel by activityViewModels()
    var timeoutItem: TimeItem? = null

    // inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_set_time, container, false)!!



    val timePicker: TimePicker = view?.findViewById<View>(R.id.timePicker) as TimePicker
    val btnSetAlarm: Button = view?.findViewById<View>(R.id.buttonStartStop) as Button

        setTimeViewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            timeoutItem = item
        }


    btnSetAlarm.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()

            if (Build.VERSION.SDK_INT >= 23) {
                calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    timePicker.hour,
                    timePicker.minute,
                    0
                )
            } else {
                calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    timePicker.currentHour,
                    timePicker.currentMinute, 0
                )
            }
            setAlarm(calendar.timeInMillis)

            val timeItem = TimeItem(id=timeoutItem!!.id, type="timer", timeLeft=calendar.timeInMillis)

            setTimeViewModel.setTimeItem(timeItem)
    }
        return view
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
