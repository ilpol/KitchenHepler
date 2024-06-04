package com.orangeskystorm.kithenhelper.ui.noteItemInfoScreen

import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import com.orangeskystorm.kithenhelper.ui.addNewItem.AddNewItemViewModel


// import android.R
//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.orangeskystorm.kithenhelper.*
import com.orangeskystorm.kithenhelper.db.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class NoteItemInfoScreen : Fragment() {
private val notesItemInfoViewModel: NotesInfoViewModel by activityViewModels()

    private val notesForFridgeViewModel: NotesForFridgeViewModel by activityViewModels()

    private val modifiedNotesViewModel: ModifiedNotesViewModel by activityViewModels()

    private val modifiedFridgeItemViewModel: ModifiedFridgeItemViewModel by activityViewModels()

    lateinit var storedNotesItemDao: StoredNotesItemDAO

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var selectedNotesInfo: StoredNotesItem? = null
        val compositeDisposable = CompositeDisposable()
        val db: StoredNotesItemsDataBase? = (activity as NotesItemDbInstance).getNotesDatabase()
        storedNotesItemDao = db?.storedNotesItemDao()!!
        val notificationsViewModel =
            ViewModelProvider(this).get(AddNewItemViewModel::class.java)

        // _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        //val root: View = binding.root

        val root: View = inflater.inflate(com.orangeskystorm.kithenhelper.R.layout.fragment_note_item_info, container, false)

        val name = root.findViewById(R.id.title) as TextView
        val description = root.findViewById(R.id.description) as TextView
        val image  = root.findViewById(R.id.icon) as ImageView
        val addToFridgeFromNotes = root.findViewById(R.id.buttonToFridge) as TextView

        val deleteButton: FloatingActionButton = root.findViewById(R.id.deleteNotesItem)


        notesItemInfoViewModel.selectedItem.observe(viewLifecycleOwner) { selectedNotesItem ->
            name.text = selectedNotesItem.name
            description.text = selectedNotesItem.description
            if (selectedNotesItem?.itemUri != null) {
                try {
                    //val f = storedFridgeItem?.itemUri
                    val cw = ContextWrapper(activity)
                    val directory: File = cw.getDir("imageDir", Context.MODE_PRIVATE)
                    // Create imageDir
                    val mypath = File(directory, selectedNotesItem?.itemUri)
                    val b = BitmapFactory.decodeStream(FileInputStream(mypath))

                    // Log.d("dfdsf", "loadImageFromStorage b = " + b)
                    //val img: ImageView = findViewById(R.id.imgPicker) as ImageView
                    //img.setImageBitmap(b)
                    image?.setImageBitmap(b)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            }
            selectedNotesInfo = selectedNotesItem
        }

        deleteButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                storedNotesItemDao?.delete(selectedNotesInfo)
                findNavController().navigate(R.id.action_navigation_notes_info_to_navigation_notes)
            }
        }

        addToFridgeFromNotes.setOnClickListener {
            val db: StoredFridgeItemsDataBase? = (activity as FridgeItemDbInstance).getDatabase()

            val storedFridgeItemDao: StoredFridgeItemDAO? = db?.storedFridgeItemDao()

            val fridgeItem = StoredFridgeItem()


            notesItemInfoViewModel.selectedItem.observe(viewLifecycleOwner) { selectedNotesItem ->
                fridgeItem.name = selectedNotesItem.name
                fridgeItem.description = selectedNotesItem.description
                fridgeItem.imgUrl = "fsdfsdf"
                fridgeItem.itemUri = selectedNotesItem?.itemUri

                viewLifecycleOwner.lifecycleScope.launch {
                    selectedNotesInfo?.let { it1 -> notesForFridgeViewModel.selectItem(it1) }
                    // storedFridgeItemDao?.insert(fridgeItem)
                    // modifiedFridgeItemViewModel.selectItem(fridgeItem)
                    findNavController().navigate(R.id.action_navigation_notes_info_to_add_new_item)
                }
            }
        }

        val title = root.findViewById(R.id.title) as TextView
        title.text = "Тестовый текст"




        val openAddFridgeItemScreenButton: FloatingActionButton = root.findViewById(R.id.openAddNotesItemScreen)

        openAddFridgeItemScreenButton.setOnClickListener {
            modifiedNotesViewModel.selectItem(selectedNotesInfo)
            findNavController().navigate(R.id.action_navigation_notes_info_to_add_notes_item)
//          val transaction = activity?.supportFragmentManager?.beginTransaction()
//          transaction?.replace(R.id.recepiesFragment, AddNewItemFragment())
//          transaction?.disallowAddToBackStack()
//          //transaction?.addToBackStack("fragB");
//          transaction?.commit()

            // Do some work here
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
}
