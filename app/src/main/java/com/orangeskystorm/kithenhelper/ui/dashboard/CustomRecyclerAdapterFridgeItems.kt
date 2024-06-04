package com.orangeskystorm.kithenhelper.ui.dashboard

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.orangeskystorm.kithenhelper.FridgeItemDbInstance
import com.orangeskystorm.kithenhelper.R
import com.orangeskystorm.kithenhelper.db.StoredFridgeItem
import com.orangeskystorm.kithenhelper.db.StoredFridgeItemDAO
import com.orangeskystorm.kithenhelper.db.StoredFridgeItemsDataBase
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

class ConfirmDeleteFragment : DialogFragment() {

    var callback: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val caution: String = getString(R.string.deleteItemCaution)
            val yesString: String = getString(R.string.yes)
            val noString: String = getString(R.string.no)
            builder.setTitle(caution)
                // .setMessage("Выбери пищу")
                .setCancelable(true)
                .setPositiveButton(yesString) { _, _ ->
                    callback?.let { it1 -> it1() }
                }
                .setNegativeButton(
                    noString
                ) { _, _ ->
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

class CustomRecyclerAdapterFridgeItems(private var context: Context, private val items: MutableList<StoredFridgeItem?>?, private val listener: (StoredFridgeItem) -> Unit, private val onDeleteElem: (StoredFridgeItem?) -> Unit) :
    RecyclerView.Adapter<CustomRecyclerAdapterFridgeItems.MyViewHolder>()  {
    var itemsInner = items

    val db: StoredFridgeItemsDataBase? = (context as FridgeItemDbInstance).getDatabase()
    val storedFridgeItemDao: StoredFridgeItemDAO? = db?.storedFridgeItemDao()
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val largeTextView: TextView = itemView.findViewById(R.id.titleFridge)
        val decriptionTextView: TextView = itemView.findViewById(R.id.description)
        var dateTextView: TextView = itemView.findViewById(R.id.date)

        val image: ImageView = itemView.findViewById(R.id.pic)
        val wrapper: View = itemView.findViewById(R.id.elemWrapper)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.fridge_item, parent, false)
        return MyViewHolder(itemView)
    }

    fun setData(items: MutableList<StoredFridgeItem?>?) {
        itemsInner = items
    }





    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val storedFridgeItem: StoredFridgeItem = itemsInner?.get(position) ?: StoredFridgeItem()

        var formatter  = SimpleDateFormat("dd/MM/yyyy");
        var dateString = ""
        if (storedFridgeItem?.alarmTime != null) {
            dateString = formatter.format(storedFridgeItem?.alarmTime?.let { Date(it) })
        }

        holder.dateTextView.text = dateString

        holder.largeTextView.text = storedFridgeItem?.name

        holder.decriptionTextView.text = storedFridgeItem?.description

        if (storedFridgeItem?.itemUri != null) {
            try {
                //val f = storedFridgeItem?.itemUri
                val cw = ContextWrapper(holder.itemView.context)
                val directory: File = cw.getDir("imageDir", Context.MODE_PRIVATE)
                // Create imageDir
                val mypath = File(directory, storedFridgeItem?.itemUri)
                val b = BitmapFactory.decodeStream(FileInputStream(mypath))

                //val img: ImageView = findViewById(R.id.imgPicker) as ImageView
                //img.setImageBitmap(b)
                holder.image?.setImageBitmap(b)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

        } else {
            holder.image?.setImageResource(R.drawable.drink);
        }


        holder.itemView.setOnClickListener {
            itemsInner?.get(position)?.let { it1 -> listener(it1) }
        }

        val currentTime = System.currentTimeMillis()

        if (storedFridgeItem?.alarmTime != null && currentTime > storedFridgeItem?.alarmTime!!) {
            val anim: Animation = AlphaAnimation(1.0f, 1.0f)
            anim.duration = 2000 //You can manage the blinking time with this parameter

            anim.startOffset = 20
            anim.repeatMode = Animation.REVERSE
            anim.repeatCount = Animation.INFINITE
            holder.wrapper.startAnimation(anim)
            holder.wrapper.setBackgroundColor(Color.RED)
        }


        holder.itemView.setOnLongClickListener{

            val myDialogFragment = ConfirmDeleteFragment()

            fun onItemDelete() {
                onDeleteElem(itemsInner?.get(position))
                itemsInner?.removeAt(position)
                notifyDataSetChanged()
            }

            myDialogFragment.callback = ::onItemDelete

            val manager = (context as FragmentActivity).supportFragmentManager
            myDialogFragment.show(manager, "myDialog")

            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        // Log.d("hjh", "itemsInner.size = " + itemsInner?.size)
        if (itemsInner?.size!= null) {
            return itemsInner!!.size
        }

        return 0
    }
}

