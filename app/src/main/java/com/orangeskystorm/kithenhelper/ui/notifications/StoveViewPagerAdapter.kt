package com.orangeskystorm.kithenhelper.ui.notifications

import android.util.Log
import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter

class StoveViewPagerAdapter(activity: FragmentActivity?) :
    FragmentStateAdapter(activity!!) {

    // declare arrayList to contain fragments and its title
    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

    fun getTabTitle(position : Int): String {
        //Log.d("hjh", "mFragmentList = " + mFragmentList)
        // return a particular fragment page
        return mFragmentTitleList[position]
    }

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        // add each fragment and its title to the array list
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getItemCount(): Int {
        return mFragmentList.size
    }
}