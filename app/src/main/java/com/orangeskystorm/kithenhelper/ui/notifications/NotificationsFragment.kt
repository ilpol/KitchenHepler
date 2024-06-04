package com.orangeskystorm.kithenhelper.ui.notifications

// import android.R

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.orangeskystorm.kithenhelper.R
import com.orangeskystorm.kithenhelper.TimerAlarmHandler
import com.orangeskystorm.kithenhelper.databinding.FragmentNotificationsBinding


class NotificationsFragment : Fragment() {

private var _binding: FragmentNotificationsBinding? = null
  private val binding get() = _binding!!

    val timerFragment = TimerFragment()
    val stopWatchFragment = StopWatchFragment()
    lateinit var adapter: StoveViewPagerAdapter

    lateinit var pager: ViewPager2 // creating object of ViewPager
    lateinit var tab: TabLayout  // creating object of TabLayout
    lateinit var bar: Toolbar    // creating object of ToolBar

    lateinit var root: View


    override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

    _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
    root = binding.root


      // set the references of the declared objects above
      pager = root.findViewById(R.id.viewPager)
      tab = root.findViewById(R.id.tabs)


      adapter = StoveViewPagerAdapter(activity)


      // add fragment to the list
      adapter.addFragment(timerFragment, "Timer")
      adapter.addFragment(stopWatchFragment, "Stopwatch")



      // Adding the Adapter to the ViewPager
      pager.adapter = adapter

        TabLayoutMediator(tab, pager) { tab, position ->
            if (position == 0) {
                val timerTabTitle: String = getString(R.string.timerTabTitle)
                tab.text = timerTabTitle
            } else {
                val stopWatchTitle: String = getString(R.string.stopWatchTitle)
                tab.text = stopWatchTitle
            }
        }.attach()

      return root
  }

    override fun onResume() {
        super.onResume()

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setAlarm(timeInMillis: Long) {
        val alarmManager = getActivity()?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(activity, TimerAlarmHandler::class.java)
        var pendingIntent: PendingIntent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_ONE_SHOT)
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
