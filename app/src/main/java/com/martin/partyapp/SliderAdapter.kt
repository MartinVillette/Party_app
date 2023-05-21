package com.martin.partyapp

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class SliderAdapter (activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> EventDescriptionFragment()
            1 -> EventMembersFragment()
            else -> throw IllegalAccessException("Invalid position : $position")
        }
    }
}