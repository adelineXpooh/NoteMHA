package com.mha.note.viewmodels

import android.app.Application
import android.content.ContentValues
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mha.note.database.DatabaseHelper
import com.mha.note.objects.Recording
import java.lang.String

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "Recording ViewModel"

    private val dbHelper: DatabaseHelper = DatabaseHelper.getInstance(application.applicationContext)

    //private val recordingListLiveData = MutableLiveData<ArrayList<Recording>>()

    /*fun getRecordingListLiveData(): MutableLiveData<ArrayList<Recording>>{
        return recordingListLiveData
    }*/

    fun addRecording(newRecording: Recording): Long{
        return dbHelper.addRecording(newRecording)
    }

    fun getRecording(id: Long): Recording?{
        return dbHelper.getRecording(id)
    }

    fun getAllRecordings(): ArrayList<Recording>{
        //recordingListLiveData.postValue(dbHelper.getAllRecordings())
        return dbHelper.getAllRecordings()
    }

    fun getRecordingsCount(): Int{
        return dbHelper.getRecordingsCount()
    }

    fun deleteRecording(recording: Recording){
        dbHelper.deleteRecording(recording)
    }
}