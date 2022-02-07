package com.mha.note.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.mha.note.R
import com.mha.note.constants.FragmentTags
import com.mha.note.ui.list.RecordListFragment

class SecondContainer : BaseContainer(), LifecycleObserver {
    private var mIsViewInitiated: Boolean = false

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.layout_container, null)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreated(){
        activity?.lifecycle?.removeObserver(this)
        if (!mIsViewInitiated) {
            mIsViewInitiated = true
            initView()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycle?.addObserver(this)
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!mIsViewInitiated) {
            mIsViewInitiated = true
            initView()
        }
    }*/

    private fun initView() {
        replaceFragment(FragmentTags.FRAGMENT_RECORD_LISTING, RecordListFragment(), false)
    }
}