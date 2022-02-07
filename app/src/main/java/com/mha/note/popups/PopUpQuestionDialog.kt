package com.dforge.whitelabel.PopUps

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.mha.note.R
import com.mha.note.objects.DialogOption
import com.mha.note.utils.Tools

class PopUpQuestionDialog(context: Context) : Dialog(context) {
    private var mTitle: TextView
    private var mMsg: TextView

    private var btnLeft: Button
    private var btnRight: Button

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.popup_qnsdialog)
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        mTitle = findViewById(R.id.dialog_title)
        mMsg = findViewById(R.id.dialog_msg)

        btnLeft = findViewById(R.id.button_no)
        btnRight = findViewById(R.id.btnDone)

        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

    fun show(title: String, message: String, leftOption: DialogOption, rightOption: DialogOption) {
        mTitle.text = title
        mMsg.text = Tools.fromHtml(message)

        btnLeft.text = leftOption.text
        btnLeft.setOnClickListener {
            leftOption.action.action()
            dismiss()
        }

        btnRight.text = rightOption.text
        btnRight.setOnClickListener {
            rightOption.action.action()
            dismiss()
        }

        show()
    }
}