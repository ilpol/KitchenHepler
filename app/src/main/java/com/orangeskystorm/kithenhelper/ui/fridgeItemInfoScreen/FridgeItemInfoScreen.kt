package com.orangeskystorm.kithenhelper.ui.fridgeItemInfoScreen



import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.orangeskystorm.kithenhelper.*
import com.orangeskystorm.kithenhelper.db.*
import com.orangeskystorm.kithenhelper.ui.addNewItem.AddNewItemViewModel
import com.orangeskystorm.kithenhelper.ui.home.MyAlarm
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

class FridgeItemInfoScreen : Fragment() {

    private val fridgeForNotesViewModel: FridgeForNotesViewModel by activityViewModels()

    private val fridgeItemInfoViewModel: FridgeInfoViewModel by activityViewModels()

    private val modifiedFridgeItemViewModel: ModifiedFridgeItemViewModel by activityViewModels()

    private val modifiedNotesViewModel: ModifiedNotesViewModel by activityViewModels()

    lateinit var storedFridgeItemDao: StoredFridgeItemDAO

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val db: StoredFridgeItemsDataBase? = (activity as FridgeItemDbInstance).getDatabase()
        storedFridgeItemDao = db?.storedFridgeItemDao()!!
        val compositeDisposable = CompositeDisposable()
        val notificationsViewModel =
            ViewModelProvider(this).get(AddNewItemViewModel::class.java)

        var selectedFridgeItemInfo: StoredFridgeItem? = null

        val root: View = inflater.inflate(com.orangeskystorm.kithenhelper.R.layout.fragment_fridge_item_info, container, false)

        val name = root.findViewById(R.id.title) as TextView
        val description = root.findViewById(R.id.description) as TextView
        val date = root.findViewById(R.id.date) as TextView

        val image  = root.findViewById(R.id.icon) as ImageView

        val addToNoteFromFridge = root.findViewById(R.id.buttonToNote) as Button

        addToNoteFromFridge.setOnClickListener {
            val db: StoredNotesItemsDataBase? = (activity as NotesItemDbInstance).getNotesDatabase()

            val storedNotesItemDao: StoredNotesItemDAO? = db?.storedNotesItemDao()

            val notesItem = StoredNotesItem()


            fridgeItemInfoViewModel.selectedItem.observe(viewLifecycleOwner) { selectedFridgeItem ->
                notesItem.name = selectedFridgeItem.name
                notesItem.description = selectedFridgeItem.description
                notesItem.imgUrl = "fsdfsdf"
                notesItem.itemUri = selectedFridgeItem?.itemUri

                viewLifecycleOwner.lifecycleScope.launch {
                    fridgeForNotesViewModel.selectItem(selectedFridgeItemInfo)
                    findNavController().navigate(R.id.action_fragment_fridge_item_info_to_add_notes_item)
                }
            }
        }


        fridgeItemInfoViewModel.selectedItem.observe(viewLifecycleOwner) { selectedFridgeItem ->
            name.text = selectedFridgeItem.name
            description.text = selectedFridgeItem.description
            var formatter  = SimpleDateFormat("dd/MM/yyyy");
            var dateString = ""
            if (selectedFridgeItem?.alarmTime != null) {
                dateString = formatter.format(selectedFridgeItem?.alarmTime?.let { Date(it) })
            }
            date.text = dateString
            selectedFridgeItemInfo = selectedFridgeItem

            if (selectedFridgeItem?.itemUri != null) {
                try {
                    //val f = storedFridgeItem?.itemUri
                    val cw = ContextWrapper(activity)
                    val directory: File = cw.getDir("imageDir", Context.MODE_PRIVATE)
                    // Create imageDir
                    val mypath = File(directory, selectedFridgeItem?.itemUri)
                    val b = BitmapFactory.decodeStream(FileInputStream(mypath))

                    // Log.d("dfdsf", "loadImageFromStorage b = " + b)
                    //val img: ImageView = findViewById(R.id.imgPicker) as ImageView
                    //img.setImageBitmap(b)
                    image?.setImageBitmap(b)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            }
        }

        val openAddFridgeItemScreenButton: FloatingActionButton = root.findViewById(R.id.openAddFridgeItemScreen)

        val deleteButton: FloatingActionButton = root.findViewById(R.id.deleteFridgeItem)

        openAddFridgeItemScreenButton.setOnClickListener {
            Log.d("fsf", "selectedFridgeItemInfo = " + selectedFridgeItemInfo)
            modifiedFridgeItemViewModel.selectItem(selectedFridgeItemInfo)
            findNavController().navigate(R.id.action_fragment_fridge_item_info_to_add_new_item)
//          val transaction = activity?.supportFragmentManager?.beginTransaction()
//          transaction?.replace(R.id.recepiesFragment, AddNewItemFragment())
//          transaction?.disallowAddToBackStack()
//          //transaction?.addToBackStack("fragB");
//          transaction?.commit()

            // Do some work here
        }

        deleteButton.setOnClickListener {
            val intent = Intent(context, MyAlarm::class.java)
            val productText: String = getString(R.string.product)
            val expiredText: String = getString(R.string.expired)
            val checkTheFridgeText = getString(R.string.checkTheFridge)
            intent.putExtra("title",  productText + " " + selectedFridgeItemInfo?.name + " " + expiredText)
            intent.putExtra("description", checkTheFridgeText)
            var pendingIntent: PendingIntent? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getBroadcast(getActivity(), 42, intent, PendingIntent.FLAG_MUTABLE)
            } else {
                pendingIntent = PendingIntent.getBroadcast(getActivity(), 42, intent, PendingIntent.FLAG_ONE_SHOT)
            }
            val manager: AlarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            manager.cancel(pendingIntent)

            viewLifecycleOwner.lifecycleScope.launch {
                selectedFridgeItemInfo?.alarmTime?.let { it1 -> cancelAlarm(it1) }
                storedFridgeItemDao?.delete(selectedFridgeItemInfo)
                findNavController().navigate(R.id.action_fragment_fridge_item_info_to_dashboard)
            }
        }

        return root
    }


    private fun handleResponse() {
//    val intent = Intent(this@AddActivity, MainActivity::class.java)
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//    startActivity(intent)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
    }

    private fun cancelAlarm(timeInMillis: Long) {
        val alarmManager = getActivity()?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(activity, TimerAlarmHandler::class.java)
        //val intent = getActivity()?.Intent(this, TimerAlarmHandler::class.java)
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
        alarmManager.cancel(pendingIntent)
        Toast.makeText(getActivity(), "Alarm is canceled", Toast.LENGTH_SHORT).show()
    }
}
