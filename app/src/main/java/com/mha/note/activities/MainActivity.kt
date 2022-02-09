package com.mha.note.activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dforge.whitelabel.PopUps.PopUpController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mha.note.R
import com.mha.note.base.BaseContainer
import com.mha.note.base.FirstContainer
import com.mha.note.base.SecondContainer
import com.mha.note.databinding.ActivityMainBinding
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    companion object{
        var mFirstTab = FirstContainer()
        var mSecondTab = SecondContainer()
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var tabsPagerAdapter: TabsPagerAdapter
    private lateinit var mainPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private lateinit var popUpController: PopUpController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        initView()
        initTabs()

        popUpController = PopUpController(this)
    }

    private fun initView(){
        tabsPagerAdapter = TabsPagerAdapter(this)
        mainPager = binding.mainPager
        mainPager.adapter = tabsPagerAdapter
        mainPager.isUserInputEnabled = false //Disable swiping

        tabLayout = binding.tabLayout
    }

    private fun initTabs(){
        mFirstTab = FirstContainer()
        mSecondTab = SecondContainer()

        TabLayoutMediator(tabLayout, mainPager) { tab, position ->
            when(position){
                0 -> {
                    val tabView = layoutInflater.inflate(R.layout.layout_tab, null)
                    val tabIcon = tabView.findViewById<ImageView>(R.id.ivTab)
                    tabIcon.setBackgroundResource(R.drawable.ic_record)
                    val tabLabel = tabView.findViewById<TextView>(R.id.tvTab)
                    tabLabel.text = getString(R.string.title_record)

                    tab.customView = tabView
                }
                1 -> {
                    val tabView = layoutInflater.inflate(R.layout.layout_tab, null)
                    val tabIcon = tabView.findViewById<ImageView>(R.id.ivTab)
                    tabIcon.setBackgroundResource(R.drawable.ic_list)
                    val tabLabel = tabView.findViewById<TextView>(R.id.tvTab)
                    tabLabel.text = getString(R.string.title_list)

                    tab.customView = tabView
                }
            }
        }.attach()
    }

    override fun onBackPressed() {
        var isPopFragment: Boolean
        when (tabLayout.selectedTabPosition) {
            0 -> isPopFragment = (mFirstTab as BaseContainer).popFragment()
            1 -> isPopFragment = (mSecondTab as BaseContainer).popFragment()
            else -> isPopFragment = false
        }

        if (!isPopFragment) {
            finish()
            exitProcess(0)
        }
    }

    fun switchFragment(tag: String, fragment: Fragment, addToBackStack: Boolean, isReplace: Boolean){
        when(tabLayout.selectedTabPosition){
            0 -> {
                if (isReplace) {
                    (mFirstTab as BaseContainer).replaceFragment(tag, fragment, addToBackStack)
                } else {
                    (mFirstTab as BaseContainer).addFragment(tag, fragment, addToBackStack)
                }
            }
            1 -> {
                if (isReplace) {
                    (mSecondTab as BaseContainer).replaceFragment(tag, fragment, addToBackStack)
                } else {
                    (mSecondTab as BaseContainer).addFragment(tag, fragment, addToBackStack)
                }
            }
            else -> {

            }
        }
    }

    fun getPopupController(): PopUpController {
        return popUpController
    }

    class TabsPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when(position){
                0 -> mFirstTab
                1 -> mSecondTab
                else -> mFirstTab
            }
        }
    }
}