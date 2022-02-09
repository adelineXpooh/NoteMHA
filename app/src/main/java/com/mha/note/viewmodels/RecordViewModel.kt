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

    private val recordingList = MutableLiveData<ArrayList<Recording>>()

    fun getRecordingList(): LiveData<ArrayList<Recording>>{
        return recordingList
    }

    fun addRecording(newRecording: Recording): Long{
        return dbHelper.addRecording(newRecording)
    }

    fun getRecording(id: Long): Recording?{
        return dbHelper.getRecording(id)
    }

    fun getAllRecordingsFromDB(){
        recordingList.value = dbHelper.getAllRecordings()
    }

    fun getRecordingsCount(): Int{
        return dbHelper.getRecordingsCount()
    }

    fun deleteRecording(recording: Recording){
        dbHelper.deleteRecording(recording)
    }
}