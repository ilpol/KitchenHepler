package com.orangeskystorm.kithenhelper.ui.home

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.beust.klaxon.Klaxon
import com.orangeskystorm.kithenhelper.R
import com.orangeskystorm.kithenhelper.databinding.FragmentHomeBinding
import com.orangeskystorm.kithenhelper.ui.dashboard.DashboardFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

class HomeFragment : Fragment() {

    var client: OkHttpClient = OkHttpClient();
    private fun getRequest(sUrl: String): String? {
        var result: String? = null
        try {
            // Create URL
            val url = URL(sUrl)
            // Build request
            val request = Request.Builder().url(url).build()
            // Execute request
            val response = client.newCall(request).execute()
            result = response.body?.string()
            // Log.d("dfdsf", "result = " + result)
        }
        catch(err:Error) {
            print("Error when executing get request: "+err.localizedMessage)
        }
        return result
    }

    private fun fetch(sUrl: String): RecepiesCategoriesResponse? {
        var recepiesCategories: RecepiesCategoriesResponse? = null
        lifecycleScope.launch(Dispatchers.IO) {
            val result = getRequest(sUrl)
            if (result != null) {
                try {
                    // Parse result string JSON to data class
                    recepiesCategories = Klaxon().parse<RecepiesCategoriesResponse>(result)
                    withContext(Dispatchers.Main) {
                    }
                }
                catch(err:Error) {
                    print("Error when parsing JSON: "+err.localizedMessage)
                }
            }
            else {
                print("Error: Get request returned no response")
            }
        }
        return recepiesCategories
    }

    // val language = arrayOf<String>("C","C++","Java",".Net","Kotlin","Ruby","Rails","Python","Java Script","Php","Ajax","Perl","Hadoop")
    val language: MutableLiveData<List<String>> = MutableLiveData()

    val images: MutableLiveData<List<String>> = MutableLiveData()


    val description = arrayOf<String>(
        "C programming is considered as the base for other programming languages",
        "C++ is an object-oriented programming language.",
        "Java is a programming language and a platform.",
        ".NET is a framework which is used to develop software applications.",
        "Kotlin is a open-source programming language, used to develop Android apps and much more.",
        "Ruby is an open-source and fully object-oriented programming language.",
        "Ruby on Rails is a server-side web application development framework written in Ruby language.",
        "Python is interpreted scripting  and object-oriented programming language.",
        "JavaScript is an object-based scripting language.",
        "PHP is an interpreted language, i.e., there is no need for compilation.",
        "AJAX allows you to send and receive data asynchronously without reloading the web page.",
        "Perl is a cross-platform environment used to create network and server-side applications.",
        "Hadoop is an open source framework from Apache written in Java."
    )

    val imageId = arrayOf<Int>(
        R.drawable.ic_baseline_search_24,R.drawable.ic_baseline_search_24,R.drawable.ic_baseline_search_24,
        R.drawable.ic_baseline_search_24,R.drawable.ic_baseline_search_24,R.drawable.ic_baseline_search_24,
        R.drawable.ic_baseline_search_24,R.drawable.ic_baseline_search_24,R.drawable.ic_baseline_search_24,
        R.drawable.ic_baseline_search_24,R.drawable.ic_baseline_search_24,R.drawable.ic_baseline_search_24,
        R.drawable.ic_baseline_search_24
    )

    private var _binding: FragmentHomeBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

      // fetch("https://www.themealdb.com/api/json/v1/1/categories.php")
    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    val root: View = binding.root

      val listView = binding.recepiesCategoriesGrid
      val recepiesCategoriesGridAdapter = getActivity()?.let { CustomGridRecepiesCategoriesAdaptor(it,language, viewLifecycleOwner, images, description,imageId) }
      listView.adapter = recepiesCategoriesGridAdapter



      language.observe(viewLifecycleOwner, Observer {
          recepiesCategoriesGridAdapter?.notifyDataSetChanged()

      })

      listView.setOnItemClickListener(){adapterView, view, position, id ->
          val itemAtPos = adapterView.getItemAtPosition(position)
          val itemIdAtPos = adapterView.getItemIdAtPosition(position)
      }

      return root
  }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


class MyAlarm : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        lateinit var notificationChannel: NotificationChannel

        val channelId = "i.apps.notifications"

         val description = "Test notification"

        var notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val intentNotification = Intent(context, DashboardFragment::class.java)

    var pendingIntent: PendingIntent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          pendingIntent = PendingIntent.getActivity(
        context, 0, intentNotification,
        PendingIntent.FLAG_IMMUTABLE
      )
    } else {
          pendingIntent = PendingIntent.getActivity(
        context, 0, intentNotification,
        PendingIntent.FLAG_UPDATE_CURRENT
      )
    }

    // RemoteViews are used to use the content of
    // some different layout apart from the current activity layout
    // val contentView = RemoteViews("com.orangeskystorm.kithenhelper", R.layout.recepies_categories_list)
        lateinit var builder: Notification.Builder
    // checking if android version is greater than oreo(API 26) or not
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
      notificationChannel.enableLights(true)
      notificationChannel.lightColor = Color.GREEN
      notificationChannel.enableVibration(false)
      notificationManager.createNotificationChannel(notificationChannel)

      builder = Notification.Builder(context, channelId)
        .setContentTitle(intent.getStringExtra("title"))
        .setContentText(intent.getStringExtra("description"))
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_background))
        .setContentIntent(pendingIntent)
    } else {

      builder = Notification.Builder(context)
        .setContentTitle(intentNotification.getStringExtra("title"))
        .setContentText(intentNotification.getStringExtra("description"))
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_background))
        .setContentIntent(pendingIntent)
    }
    notificationManager.notify(1234, builder.build())

    }
}