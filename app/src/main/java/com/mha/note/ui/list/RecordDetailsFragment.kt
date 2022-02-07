package com.mha.note.ui.list

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mha.note.constants.TagName
import com.mha.note.databinding.FragmentRecordDetailsBinding
import com.mha.note.objects.Recording
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RecordDetailsFragment : Fragment() {

    private var _binding: FragmentRecordDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var tvFilename: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvDuration: TextView
    private lateinit var btnPlay: ImageView
    private lateinit var tvNote: TextView

    private lateinit var mRecording: Recording
    private var mPlayer: MediaPlayer? = null

    private var mStatus: PlayStatus = PlayStatus.STOP

    companion object{
        fun newInstance(mRecording: Recording) = RecordDetailsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(TagName.BUNDLE_RECORDING, mRecording)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        init()
        setInformation()
        setUpOnClickListener()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        if(mPlayer != null){
            stopPlaying()
        }
    }

    private fun init(){
        tvFilename = binding.recordingLayout.tvFileName
        tvDate = binding.recordingLayout.tvDate
        tvDuration = binding.recordingLayout.tvDuration
        btnPlay = binding.recordingLayout.btnPlay
        tvNote = binding.tvNote

        if(arguments?.getSerializable(TagName.BUNDLE_RECORDING) != null) {
            @Suppress("UNCHECKED_CAST")
            mRecording = arguments?.getSerializable(TagName.BUNDLE_RECORDING) as Recording
        }
    }

    private fun setUpOnClickListener(){
        btnPlay.setOnClickListener {
            when(mStatus){
                PlayStatus.PLAYING -> {
                    mStatus = PlayStatus.STOP
                    stopPlaying()
                    btnPlay.isSelected = false
                }
                PlayStatus.STOP -> {
                    mStatus = PlayStatus.PLAYING
                    playRecording()
                    btnPlay.isSelected = true
                }
            }
        }
    }

    private fun setInformation(){
        tvFilename.text = mRecording.filename
        tvDuration.text = mRecording.duration
        tvNote.text = mRecording.note

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = mRecording.timeStamp
        tvDate.text = SimpleDateFormat(TagName.dateFormatDisplay, Locale.getDefault()).format(calendar.time)

        if(mRecording.file == null){
            btnPlay.visibility = View.GONE
        }
    }

    private fun playRecording(){
        val path = requireContext().getExternalFilesDir(null)!!.absolutePath
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            requireContext().getExternalFilesDir(null)!!.absolutePath
        }else{
            Environment.getExternalStorageDirectory().absolutePath
        }*/

        // media player class for playing our recorded audio
        mPlayer = MediaPlayer()
        try {
            Log.e("PATH", "$path/${mRecording.file?.name}")
            // set the data source
            mPlayer!!.setDataSource("$path/${mRecording.file?.name}")
            // prepare media player
            mPlayer!!.prepare()
            // start media player.
            mPlayer!!.start()
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Unable to play file", Toast.LENGTH_SHORT).show()
            Log.e("PLAY RECORD IN DETAIL", "prepare() failed")
        }
    }

    private fun stopPlaying(){
        mPlayer!!.release()
        mPlayer = null
    }
}