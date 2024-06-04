package com.orangeskystorm.kithenhelper.ui.notes


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
import com.orangeskystorm.kithenhelper.*
import com.orangeskystorm.kithenhelper.databinding.FragmentDashboardBinding
import com.orangeskystorm.kithenhelper.databinding.FragmentNotesBinding
import com.orangeskystorm.kithenhelper.db.*
import com.orangeskystorm.kithenhelper.ui.dashboard.CustomRecyclerAdapterFridgeItems
import com.orangeskystorm.kithenhelper.ui.notes.CustomRecyclerAdapterNotesItems
import kotlinx.coroutines.*

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var allItems: MutableList<StoredNotesItem?>? = null
    lateinit var recyclerView: RecyclerView
    var storedNotesItemDao: StoredNotesItemDAO? = null

    private val notesInfoViewModel: NotesInfoViewModel by activityViewModels()

    fun onDeleteElem(notesItem: StoredNotesItem?) {
        viewLifecycleOwner.lifecycleScope.launch {
            storedNotesItemDao?.delete(notesItem)
        }
    }

    fun onItemClick(notesItem: StoredNotesItem) {
        notesInfoViewModel.selectItem(notesItem)
        findNavController().navigate(R.id.action_navigation_notes_to_notes_info)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        var i:StoredFridgeItem = StoredFridgeItem()

        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        val root: View = binding.root


        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)



        recyclerView.adapter =
            activity?.let { CustomRecyclerAdapterNotesItems(it, allItems, this::onItemClick, this::onDeleteElem) }

        val openAddFridgeItemScreenButton: FloatingActionButton = root.findViewById(R.id.openAddNotesItemScreen)

        openAddFridgeItemScreenButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_notes_to_add_new_note_item)
//          val transaction = activity?.supportFragmentManager?.beginTransaction()
//          transaction?.replace(R.id.recepiesFragment, AddNewItemFragment())
//          transaction?.disallowAddToBackStack()
//          //transaction?.addToBackStack("fragB");
//          transaction?.commit()

            // Do some work here
        }

        val db: StoredNotesItemsDataBase? = (activity as NotesItemDbInstance).getNotesDatabase()
        storedNotesItemDao = db?.storedNotesItemDao()

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
            allItems = storedNotesItemDao?.getAll() as MutableList<StoredNotesItem?>?
            withContext(Dispatchers.Main) {
                (recyclerView.adapter as CustomRecyclerAdapterNotesItems).setData(allItems)
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}