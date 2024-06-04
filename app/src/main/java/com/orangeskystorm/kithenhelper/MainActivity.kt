package com.orangeskystorm.kithenhelper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.orangeskystorm.kithenhelper.databinding.ActivityMainBinding
import com.orangeskystorm.kithenhelper.db.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

interface FridgeItemDbInstance {
    fun getDatabase(): StoredFridgeItemsDataBase?
}

interface NotesItemDbInstance {
    fun getNotesDatabase(): StoredNotesItemsDataBase?
}


class FridgeInfoViewModel : ViewModel() {
    private val mutableSelectedItem = MutableLiveData<StoredFridgeItem>()
    val selectedItem: LiveData<StoredFridgeItem> get() = mutableSelectedItem

    fun selectItem(item: StoredFridgeItem) {
        mutableSelectedItem.value = item
    }
}

class ModifiedFridgeItemViewModel : ViewModel() {
    private val mutableSelectedItem = MutableLiveData<StoredFridgeItem?>()
    val selectedItem: LiveData<StoredFridgeItem?> get() = mutableSelectedItem

    fun selectItem(item: StoredFridgeItem?) {
        mutableSelectedItem.value = item
    }
}

class NotesInfoViewModel : ViewModel() {
    private val mutableSelectedItem = MutableLiveData<StoredNotesItem>()
    val selectedItem: LiveData<StoredNotesItem> get() = mutableSelectedItem

    fun selectItem(item: StoredNotesItem) {
        mutableSelectedItem.value = item
    }
}

class NotesForFridgeViewModel : ViewModel() {
    private val mutableSelectedItem = MutableLiveData<StoredNotesItem>()
    val selectedItem: LiveData<StoredNotesItem> get() = mutableSelectedItem

    fun selectItem(item: StoredNotesItem?) {
        mutableSelectedItem.value = item
    }
}

class FridgeForNotesViewModel : ViewModel() {
    private val mutableSelectedItem = MutableLiveData<StoredFridgeItem?>()
    val selectedItem: LiveData<StoredFridgeItem?> get() = mutableSelectedItem

    fun selectItem(item: StoredFridgeItem?) {
        mutableSelectedItem.value = item
    }
}

class ModifiedNotesViewModel : ViewModel() {
    private val mutableSelectedItem = MutableLiveData<StoredNotesItem?>()
    val selectedItem: LiveData<StoredNotesItem?> get() = mutableSelectedItem

    fun selectItem(item: StoredNotesItem?) {
        mutableSelectedItem.value = item
    }
}

class SetTimeViewModel : ViewModel() {
    private val mutableSetTimeItem = MutableLiveData<TimeItem?>()
    val selectedItem: MutableLiveData<TimeItem?> get() = mutableSetTimeItem

    fun setTimeItem(item: TimeItem) {
        mutableSetTimeItem.value = item
    }
}

class MainActivity : AppCompatActivity(), FridgeItemDbInstance, NotesItemDbInstance {

    private var database: StoredFridgeItemsDataBase? = null
    private var notesDatabase: StoredNotesItemsDataBase? = null

private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Room.databaseBuilder(this, StoredFridgeItemsDataBase::class.java, "storedFridgeItemsDataBase")
            .fallbackToDestructiveMigration().build()

        notesDatabase = Room.databaseBuilder(this, StoredNotesItemsDataBase::class.java, "storedNotesItemsDataBase")
            .fallbackToDestructiveMigration().build()

        val hour = 14
        val minute = 49
        val myTime = "$hour:$minute"
        var date: Date? = null

        // today at your defined time Calendar
        val customCalendar: Calendar = GregorianCalendar()
        // set hours and minutes
        customCalendar.set(Calendar.HOUR_OF_DAY, hour)
        customCalendar.set(Calendar.MINUTE, minute)
        customCalendar.set(Calendar.SECOND, 0)
        customCalendar.set(Calendar.MILLISECOND, 0)
        val customDate: Date = customCalendar.getTime()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        try {
            date = sdf.parse(myTime)
        } catch (e: ParseException) {
            e.printStackTrace()

        } catch (e: ParseException) {
            e.printStackTrace()
        }
        var timeInMs = 0L
        if (date != null) {
            timeInMs = customDate.getTime()
        }


        setAlarm(timeInMs)

     binding = ActivityMainBinding.inflate(layoutInflater)
     setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_notes, R.id.navigation_dashboard, R.id.navigation_notifications))

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // always show selected Bottom Navigation item as selected (return true)
        navView.setOnItemSelectedListener { item ->
            // In order to get the expected behavior, you have to call default Navigation method manually
            NavigationUI.onNavDestinationSelected(item, navController)

            return@setOnItemSelectedListener true
        }
    }

    private fun setAlarm(timeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MyAlarm::class.java)
        var pendingIntent: PendingIntent? = null
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE)

        } else {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC,
            timeInMillis,
            AlarmManager.INTERVAL_FIFTEEN_MINUTES,
            pendingIntent
        )
    }
    private class MyAlarm : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        //return  super.onSupportNavigateUp()
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun getDatabase(): StoredFridgeItemsDataBase? {
        return database
    }


    override fun getNotesDatabase(): StoredNotesItemsDataBase? {
        return notesDatabase
    }
}