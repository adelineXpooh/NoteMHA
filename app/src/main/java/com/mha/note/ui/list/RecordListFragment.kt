package com.mha.note.ui.list

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mha.note.R
import com.mha.note.activities.MainActivity
import com.mha.note.constants.FragmentTags
import com.mha.note.constants.TagName
import com.mha.note.databinding.FragmentListBinding
import com.mha.note.interfaces.StringDelegate
import com.mha.note.objects.Recording
import com.mha.note.viewmodels.RecordViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RecordListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var recordViewModel: RecordViewModel

    private lateinit var rvRecordingList: RecyclerView
    private lateinit var mAdapter: RecordingListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    private var path: String = ""
    private var mRecordingList: ArrayList<Recording> = ArrayList()
    private var mPlayer: MediaPlayer? = null

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            Log.e("PERMISSION RESULT", "${it.key} = ${it.value}")
            if(it.value){

            }else{
                (activity as MainActivity).getPopupController().ShowMessageDialog(getString(R.string.error), getString(
                    R.string.error_permission))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recordViewModel = ViewModelProvider(this)[RecordViewModel::class.java]

        if (ContextCompat.checkSelfPermission(context as Context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            requestMultiplePermissions.launch(permissions)
        }

        init()
        observeViewModel()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("RestrictedApi")
    override fun onResume() {
        super.onResume()
        if (isMenuVisible){
            recordViewModel.getAllRecordingsFromDB()
        }
    }

    override fun onPause() {
        super.onPause()
        stopPlaying()
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if(menuVisible && isResumed){
            onResume()
        }else if(!menuVisible){
            stopPlaying()
        }
    }

    private fun init(){
        rvRecordingList = binding.rvRecordingList
        mLayoutManager = LinearLayoutManager(context)
        rvRecordingList.setHasFixedSize(true)
        rvRecordingList.layoutManager = mLayoutManager

        mAdapter = RecordingListAdapter(context as Context, mRecordingList, object : StringDelegate {
            override fun action(string: String) {
                playRecording(string.toInt())
            }
        }, object : StringDelegate {
            override fun action(string: String) {
                stopPlaying()
                (activity as MainActivity).switchFragment(FragmentTags.FRAGMENT_RECORD_DETAILS,
                    RecordDetailsFragment.newInstance(mRecordingList[string.toInt()]),
                    addToBackStack = true,
                    isReplace = false)
            }
        })
        rvRecordingList.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    private fun observeViewModel(){
        Log.e("CHECK", "Entered Observe View Model")
        recordViewModel.getRecordingList().observe(viewLifecycleOwner){ recordings ->
            mRecordingList.clear()
            mRecordingList.addAll(recordings)
            if (mRecordingList.size > 0) {
                // Check Recording List in SQLite
                /*for (i in mRecordingList.indices){
                    Log.e("Recording", mRecordingList[i].toString())
                }*/
                getFilesListFromStorage()
            }
        }
    }

    private fun getFilesListFromStorage(){
        path = requireContext().getExternalFilesDir(null)!!.absolutePath
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            requireContext().getExternalFilesDir(null)!!.absolutePath
        }else{
            Environment.getExternalStorageDirectory().absolutePath
        }*/
        Log.e("FILE LIST", "Path: $path")

        val directory = File(path)
        val files: Array<File> = directory.listFiles()
        //Log.e("FILE LIST", "Size: " + files.size)
        matchFileToRecording(files)
        /*for (i in files.indices) {
            Log.e("Files", "FileName:" + files[i].name)
            mRecordingList[i].file = files[i]
        }*/
    }

    private fun matchFileToRecording(files: Array<File>){
        for(i in mRecordingList.indices){
            var found = false
            var j = 0
            do{
                //Log.e("Files", "FileName:" + files[j].name)
                if(files[j].name.equals(mRecordingList[i].filename)){
                    found = true
                    mRecordingList[i].file = files[j]
                }else{
                    ++j
                }
            }while(!found && j < files.size)
        }

        mAdapter.notifyDataSetChanged()
    }

    private fun playRecording(idx: Int){
        // media player class for playing our recorded audio
        mPlayer = MediaPlayer()
        try {
            //Log.e("PATH", "$path/${mRecordingList[idx].file?.name}")
            // set the data source
            mPlayer!!.setDataSource("$path/${mRecordingList[idx].file?.name}")
            // prepare media player
            mPlayer!!.prepare()
            // start media player.
            mPlayer!!.start()
        } catch (e: IOException) {
            Log.e("PLAY RECORD IN LIST", "prepare() failed")
        }
    }

    private fun stopPlaying(){
        if(mPlayer != null){
            mPlayer!!.release()
            mPlayer = null
        }
    }

    inner class RecordingListAdapter(var context: Context, var arrayList: ArrayList<Recording>,
                                 var mCallback: StringDelegate, var mCallback2: StringDelegate
    ): RecyclerView.Adapter<RecordingListAdapter.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val mInflater = LayoutInflater.from(parent.context)
            val mainGroup = mInflater.inflate(R.layout.item_recording, parent, false)
            return ViewHolder(mainGroup)
        }

        override fun getItemCount(): Int {
            return if (arrayList != null) arrayList.size else 0
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = arrayList[position]

            holder.tvFileName.text = item.filename
            holder.tvDuration.text = item.duration

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = item.timeStamp
            //holder.tvDate.text = SimpleDateFormat(TagName.dateFormatDisplay).format(SimpleDateFormat(TagName.dateFormatSQLite).parse(item.timeStamp))
            holder.tvDate.text = SimpleDateFormat(TagName.dateFormatDisplay).format(calendar.time)

            if(item.file == null){
                holder.btnPlay.visibility = View.GONE
            }

            holder.btnPlay.setOnClickListener {
                mCallback.action(position.toString())
                /*when(holder.mStatus){
                    PlayStatus.PLAYING -> {

                    }
                    PlayStatus.STOP ->{

                    }
                }*/
            }

            holder.itemLayout.setOnClickListener {
                mCallback2.action(position.toString())
                //Toast.makeText(context, "Position: $position", Toast.LENGTH_SHORT).show()
            }
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            var itemLayout: ConstraintLayout = itemView.findViewById(R.id.itemContainer)
            var tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
            var tvDate: TextView = itemView.findViewById(R.id.tvDate)
            var tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
            var btnPlay: ImageView = itemView.findViewById(R.id.btnPlay)

            var mStatus: PlayStatus = PlayStatus.STOP
        }
    }
}