package com.mha.note.base

import androidx.fragment.app.Fragment
import com.mha.note.R

open class BaseContainer : Fragment(){

    fun replaceFragment(tag: String, fragment: Fragment, addToBackStack: Boolean) {

        val transaction = childFragmentManager.beginTransaction()

        if (addToBackStack) {
            transaction.addToBackStack(tag)
        }

        transaction.replace(R.id.container, fragment, tag)
        transaction.commit()
        childFragmentManager.executePendingTransactions()
    }

    fun addFragment(tag: String, fragment: Fragment, addToBackStack: Boolean) {

        val transaction = childFragmentManager.beginTransaction()

        if (addToBackStack) {
            transaction.addToBackStack(tag)
        }

        transaction.add(R.id.container, fragment, tag)
        transaction.commit()
        childFragmentManager.executePendingTransactions()
    }

    fun popFragment(): Boolean {

        var isPop = false

        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStackImmediate()
            isPop = true
        }

        return isPop
    }

    fun popToBaseFragment(){
        var count = childFragmentManager.backStackEntryCount
        while (count > 0) {
            popFragment()
            --count
        }
    }

    fun getBackStackCount(): Int {
        return childFragmentManager.backStackEntryCount
    }

    fun getFragmentTag(): String? {
        return childFragmentManager.getBackStackEntryAt(childFragmentManager.backStackEntryCount - 1).name
    }

}