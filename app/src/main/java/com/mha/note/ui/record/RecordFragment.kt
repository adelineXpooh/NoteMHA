package com.mha.note.ui.record

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mha.note.R
import com.mha.note.activities.MainActivity
import com.mha.note.databinding.FragmentRecordBinding
import com.mha.note.objects.Recording
import com.mha.note.viewmodels.RecordViewModel
import java.io.IOException
import java.util.*

class RecordFragment : Fragment() {

    private var _binding: FragmentRecordBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var recordViewModel: RecordViewModel

    private lateinit var etFileName: EditText
    private lateinit var etNote: EditText
    private lateinit var tvTimer: TextView
    private lateinit var btnRecord: Button
    private lateinit var btnSave: Button

    private var mStatus: RecordStatus = RecordStatus.NONE

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private var mFileName: String = ""
    private var timeStamp: Long = 0

    private val handler = Handler(Looper.getMainLooper())
    private var timerSeconds = 0
    private var isRunning = false

    private val runTimer = object : Runnable {
        override fun run() {
            val hours = timerSeconds/3600
            val mins = (timerSeconds % 3600)/60
            val secs = timerSeconds % 60

            val timeStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, mins, secs)
            tvTimer.text = timeStr

            if(isRunning){
                timerSeconds++
            }

            handler.postDelayed(this, 1000)
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            Log.e("PERMISSION RESULT", "${it.key} = ${it.value}")
            if(it.value){

            }else{
                (activity as MainActivity).getPopupController().ShowMessageDialog(getString(R.string.error), getString(R.string.error_permission))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recordViewModel = ViewModelProvider(this)[RecordViewModel::class.java]

        if (ContextCompat.checkSelfPermission(context as Context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestMultiplePermissions.launch(permissions)
        }

        init()
        setUpOnClickListener()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runTimer)
        if(isRunning){
            stopRecording()
            saveRecording()
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if(menuVisible){
            if (ContextCompat.checkSelfPermission(context as Context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestMultiplePermissions.launch(permissions)
            }
        }
    }

    private fun init(){
        etFileName = binding.etFileName
        etNote = binding.etNote
        tvTimer = binding.tvTimer
        btnRecord = binding.btnRecord
        btnSave = binding.btnSave

        btnSave.isEnabled = false
    }

    private fun setUpOnClickListener(){
        btnRecord.setOnClickListener {
            when(mStatus){
                RecordStatus.NONE -> {
                    mStatus = RecordStatus.RECORDING
                    btnRecord.text = getString(R.string.stop_recording)
                    startRecording()
                }
                RecordStatus.RECORDING -> {
                    mStatus = RecordStatus.STOP
                    btnRecord.text = getString(R.string.play_recording)
                    stopRecording()
                    btnSave.isEnabled = true
                }
                RecordStatus.STOP,
                RecordStatus.PAUSE -> {
                    mStatus = RecordStatus.PLAY
                    btnRecord.text = getString(R.string.pause)
                    playRecording()
                }
                RecordStatus.PLAY -> {
                    mStatus = RecordStatus.PAUSE
                    btnRecord.text = getString(R.string.play_recording)
                    stopPlaying()
                }
            }
        }

        btnSave.setOnClickListener {
            saveRecording()
        }
    }

    private fun startRecording() {
        timeStamp = System.currentTimeMillis() //SimpleDateFormat(TagName.dateFormatSQLite).format(Date())
        startTimer()

        // initializing our filename variable with the path of the recorded audio file
        mFileName = "${requireContext().getExternalFilesDir(null)!!.absolutePath}/${getString(R.string.app_name)}_$timeStamp.3gp"
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            requireContext().getExternalFilesDir(null)!!.absolutePath
        }else{
            Environment.getExternalStorageDirectory().absolutePath
        }*/
        Log.e("START RECORD", "Filename: $mFileName")

        // initialize media recorder class
        mRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(requireContext())
        }else{
            MediaRecorder()
        }
        // set the audio source
        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        // set the output format of the audio.
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        // set the audio encoder for our recorded audio.
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        // set the output file location for our recorded audio
        mRecorder!!.setOutputFile(mFileName)

        try {
            // prepare audio recorder class
            mRecorder!!.prepare()
        } catch (e: IOException) {
            Log.e("START RECORD", "prepare() failed")
        }

        // start the audio recording.
        mRecorder!!.start()
    }

    private fun stopRecording(){
        stopTimer()

        mRecorder!!.stop()
        mRecorder!!.release()
        mRecorder = null

        btnSave.isEnabled = true
    }

    private fun playRecording(){
        // media player class for playing our recorded audio
        mPlayer = MediaPlayer()
        try {
            // set the data source
            mPlayer!!.setDataSource(mFileName)
            // prepare media player
            mPlayer!!.prepare()
            // start media player.
            mPlayer!!.start()
        } catch (e: IOException) {
            Log.e("PLAY RECORD", "prepare() failed")
        }
    }

    private fun stopPlaying(){
        mPlayer!!.release()
        mPlayer = null
    }

    private fun saveRecording(){
        // Save Recording
        //val title = etFileName.text.toString()
        val description = etNote.text.toString()
        val duration = "${timerSeconds/60} mins ${timerSeconds%60} secs"

        val recording = Recording(0, "${getString(R.string.app_name)}_$timeStamp.3gp", description, timeStamp, duration)

        // Save information to SQLite
        recordViewModel.addRecording(recording)
        Log.e("SAVE RECORDING", "ID: $id")

        if(mPlayer != null){
            stopPlaying()
        }

        timerSeconds = 0
        btnRecord.text = getString(R.string.start_recording)
        etNote.setText("")
        btnSave.isEnabled = false
    }

    private fun startTimer(){
        isRunning = true
        handler.post(runTimer)
    }

    private fun stopTimer(){
        isRunning = false
    }

    enum class RecordStatus{
        NONE, RECORDING, STOP, PLAY, PAUSE
    }
}