package com.dforge.whitelabel.PopUps

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.mha.note.R
import com.mha.note.interfaces.GenericDelegate
import com.mha.note.objects.DialogOption

class PopUpMessageDialog(context: Context, title: String, message: String) : Dialog(context) {

    private var mTitle: TextView
    private var mMessage: TextView
    private var mButton: Button

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.popup_msgdialog)
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        setCanceledOnTouchOutside(true)
        setCancelable(true)

        mTitle = findViewById(R.id.dialog_title)
        mTitle.text = title

        mButton = findViewById(R.id.btn_ok)
        mButton.setOnClickListener { dismiss() }

        mMessage = findViewById(R.id.dialog_msg)
        mMessage.text = message
    }

    fun show(title: String, message: String) {
        mTitle.text = title
        mMessage.text = message
        mButton.setOnClickListener { dismiss() }

        show()
    }

    fun show(title: String, message: String, callback: GenericDelegate?) {
        mTitle.text = title
        mMessage.text = message
        mButton.setOnClickListener {
            callback?.action()
            dismiss()
        }

        show()
    }

    fun show(title: String, message: String, option: DialogOption){
        mTitle.text = title
        mMessage.text = message
        mButton.text = option.text

        mButton.setOnClickListener {
            option.action.action()
            dismiss()
        }

        show()
    }
}