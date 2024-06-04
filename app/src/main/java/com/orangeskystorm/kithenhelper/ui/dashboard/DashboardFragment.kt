package com.orangeskystorm.kithenhelper.ui.dashboard

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.orangeskystorm.kithenhelper.FridgeInfoViewModel
import com.orangeskystorm.kithenhelper.FridgeItemDbInstance
import com.orangeskystorm.kithenhelper.R
import com.orangeskystorm.kithenhelper.databinding.FragmentDashboardBinding
import com.orangeskystorm.kithenhelper.db.FridgeItem
import com.orangeskystorm.kithenhelper.db.StoredFridgeItem
import com.orangeskystorm.kithenhelper.db.StoredFridgeItemDAO
import com.orangeskystorm.kithenhelper.db.StoredFridgeItemsDataBase
import com.orangeskystorm.kithenhelper.ui.addNewItem.AddNewItemFragment
import com.orangeskystorm.kithenhelper.ui.home.MyAlarm
import kotlinx.coroutines.*

class DashboardFragment : Fragment() {

private var _binding: FragmentDashboardBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

    var allItems: MutableList<StoredFridgeItem?>? = null
    lateinit var recyclerView: RecyclerView
    lateinit var storedFridgeItemDao: StoredFridgeItemDAO

    private val fridgeInfoViewModel: FridgeInfoViewModel by activityViewModels()

    fun onItemClick(gifItem: StoredFridgeItem) {
        fridgeInfoViewModel.selectItem(gifItem)
        findNavController().navigate(R.id.action_navigation_dashboard_to_fridge_item_info)
    }

    fun onDeleteElem(fridgeItem: StoredFridgeItem?) {
        val intent = Intent(context, MyAlarm::class.java)
        val productText: String = getString(R.string.product)
        val expiredText: String = getString(R.string.expired)
        val checkTheFridgeText = getString(R.string.checkTheFridge)
        intent.putExtra("title",  productText + " " + fridgeItem?.name + " " + expiredText)
        intent.putExtra("description", checkTheFridgeText)
        var pendingIntent: PendingIntent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(getActivity(), 42, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            pendingIntent = PendingIntent.getBroadcast(getActivity(), 42, intent, PendingIntent.FLAG_ONE_SHOT)
        }
        // val pending = PendingIntent.getBroadcast(context, 42, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Schdedule notification
        // Schdedule notification
        val manager: AlarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(pendingIntent)
        viewLifecycleOwner.lifecycleScope.launch {
            storedFridgeItemDao?.delete(fridgeItem)
        }
    }

    override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

     var i:StoredFridgeItem = StoredFridgeItem()
    val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

    _binding = FragmentDashboardBinding.inflate(inflater, container, false)
    val root: View = binding.root

      recyclerView = root.findViewById(R.id.recyclerView)
      recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        recyclerView.adapter =
            activity?.let { CustomRecyclerAdapterFridgeItems(it, allItems, this::onItemClick, this::onDeleteElem) }

        val openAddFridgeItemScreenButton: FloatingActionButton = root.findViewById(R.id.openAddFridgeItemScreen)

      openAddFridgeItemScreenButton.setOnClickListener {
          findNavController().navigate(R.id.action_navigation_dashboard_to_add_new_item)
//          val transaction = activity?.supportFragmentManager?.beginTransaction()
//          transaction?.replace(R.id.recepiesFragment, AddNewItemFragment())
//          transaction?.disallowAddToBackStack()
//          //transaction?.addToBackStack("fragB");
//          transaction?.commit()

          // Do some work here
      }

      val db: StoredFridgeItemsDataBase? = (activity as FridgeItemDbInstance).getDatabase()
        // Log.d("dsfdsfds", "Dashboard db = " + db)
        storedFridgeItemDao = db?.storedFridgeItemDao()!!
    return root
  }

    private fun fillList(): List<String> {
        val data = mutableListOf<String>()
        (0..30).forEach { i -> data.add("$i element") }
        return data
    }

    override fun onResume() {
        super.onResume()

        GlobalScope.launch(Dispatchers.IO) {
            allItems?.clear()
            allItems = storedFridgeItemDao?.getAll() as MutableList<StoredFridgeItem?>?
            withContext(Dispatchers.Main) {
                (recyclerView.adapter as CustomRecyclerAdapterFridgeItems).setData(allItems)
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }

    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}