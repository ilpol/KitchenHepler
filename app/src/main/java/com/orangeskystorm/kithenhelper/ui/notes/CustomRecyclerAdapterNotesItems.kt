package com.orangeskystorm.kithenhelper.ui.notes

import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.orangeskystorm.kithenhelper.R
import com.orangeskystorm.kithenhelper.db.StoredFridgeItem
import com.orangeskystorm.kithenhelper.db.StoredNotesItem
import com.orangeskystorm.kithenhelper.ui.dashboard.ConfirmDeleteFragment
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class CustomRecyclerAdapterNotesItems(private var context: Context, private val items: MutableList<StoredNotesItem?>?, private val listener: (StoredNotesItem) -> Unit, private val onDeleteElem: (StoredNotesItem?) -> Unit) :
    RecyclerView.Adapter<CustomRecyclerAdapterNotesItems.MyViewHolder>()  {
    var itemsInner = items
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val largeTextView: TextView = itemView.findViewById(R.id.titleFridge)
        val decriptionTextView: TextView = itemView.findViewById(R.id.description)
        val image: ImageView = itemView.findViewById(R.id.pic)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.notes_item, parent, false)
        return MyViewHolder(itemView)
    }

    fun setData(items: MutableList<StoredNotesItem?>?) {
        itemsInner = items
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val storedNotesItem: StoredNotesItem = itemsInner?.get(position) ?: StoredNotesItem()
        // Log.d("dsfdsfds", "storedFridgeItem?.name here = " + storedNotesItem?.name)
        holder.largeTextView.text = storedNotesItem?.name
        holder.decriptionTextView.text = storedNotesItem?.description

        //Log.d("dsfdsfds", "storedFridgeItem?.itemUri = " + storedNotesItem?.itemUri?.name)

        if (storedNotesItem?.itemUri != null) {
            try {
                //val f = storedFridgeItem?.itemUri
                val cw = ContextWrapper(holder.itemView.context)
                val directory: File = cw.getDir("imageDir", Context.MODE_PRIVATE)
                // Create imageDir
                val mypath = File(directory, storedNotesItem?.itemUri)
                val b = BitmapFactory.decodeStream(FileInputStream(mypath))

                // Log.d("dfdsf", "loadImageFromStorage b = " + b)
                //val img: ImageView = findViewById(R.id.imgPicker) as ImageView
                //img.setImageBitmap(b)
                holder.image?.setImageBitmap(b)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

        } else {
            holder.image?.setImageResource(R.drawable.drink);
        }


//        holder.itemView.setOnClickListener {
//            findNavController(holder.itemView).navigate(R.id.action_navigation_notes_to_notes_info)
//            // findNavController().navigate(R.id.action_navigation_dashboard_to_fridge_item_info)
//        }

        holder.itemView.setOnClickListener {
            itemsInner?.get(position)?.let { it1 -> listener(it1) }
        }

        holder.itemView.setOnLongClickListener{

            val myDialogFragment = ConfirmDeleteFragment()

            fun onItemDelete() {
                onDeleteElem(itemsInner?.get(position))
                itemsInner?.removeAt(position)
                notifyDataSetChanged()
            }

            myDialogFragment.callback = ::onItemDelete

            // myDialogFragment.setCallback(this::onItemDelete)

            val manager = (context as FragmentActivity).supportFragmentManager
            myDialogFragment.show(manager, "myDialog")

            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        if (itemsInner?.size!= null) {
            return itemsInner!!.size
        }

        return 0
    }
}

