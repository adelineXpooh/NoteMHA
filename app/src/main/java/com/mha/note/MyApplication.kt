package com.mha.note

import android.app.Application
import android.content.Context
import com.stringcare.library.SC

class MyApplication: Application() {

    companion object{
        private lateinit var mContext: Context

        fun getContext(): Context{
            return mContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
        SC.init { applicationContext }
    }

}