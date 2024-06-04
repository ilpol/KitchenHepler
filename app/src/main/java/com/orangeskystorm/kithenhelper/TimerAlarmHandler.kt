package com.orangeskystorm.kithenhelper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class TimerAlarmHandler: BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        Toast.makeText(context, "Alarm m", Toast.LENGTH_SHORT).show()
    }
}