package com.orangeskystorm.kithenhelper.ui.home
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.orangeskystorm.kithenhelper.R

public class CustomGridRecepiesCategoriesAdaptor(private val context: Activity, private val title:
MutableLiveData<List<String>>, viewLifecycleOwner: LifecycleOwner, private val images:MutableLiveData<List<String>>,  private val description: Array<String>, private val imgid: Array<Int>)
    : ArrayAdapter<String>(context, R.layout.recepies_categories_list, description) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.recepies_categories_list, null, true)

        val titleText = rowView.findViewById(R.id.title) as TextView
        val imageView = rowView.findViewById(R.id.icon) as ImageView
        val subtitleText = rowView.findViewById(R.id.description) as TextView

        titleText.text = title.value?.get(position)

        Glide.with(context).load(images.value?.get(position)).into(imageView);
        subtitleText.text = description[position]

        return rowView
    }
}