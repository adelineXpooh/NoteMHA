package com.dforge.whitelabel.PopUps

import android.app.Dialog
import android.content.Context
import androidx.core.content.ContextCompat
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import com.mha.note.R

class PopUpProgressDialog(context: Context, displayMessage: String) : Dialog(context) {
    private var msgView: TextView

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.popup_progress)
        window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)

        setCanceledOnTouchOutside(false)
        setCancelable(false)

        val spinner = findViewById<View>(R.id.progressBar) as ProgressBar
        spinner.indeterminateDrawable.setColorFilter(ContextCompat.getColor(context, R.color.purple_700), android.graphics.PorterDuff.Mode.MULTIPLY)

        msgView = findViewById(R.id.dialog_msg)
        msgView.text = displayMessage
    }

    fun setMessage(displayMessage: String) {
        msgView.text = displayMessage
    }
}