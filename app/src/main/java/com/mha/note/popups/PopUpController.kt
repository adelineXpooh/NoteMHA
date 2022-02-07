package com.dforge.whitelabel.PopUps

import android.content.Context
import com.mha.note.interfaces.GenericDelegate
import com.mha.note.objects.DialogOption

class PopUpController(context: Context){
    private var progressDialog: PopUpProgressDialog = PopUpProgressDialog(context, "")
    private var messageDialog: PopUpMessageDialog = PopUpMessageDialog(context, "", "")
    private var questionDialog: PopUpQuestionDialog = PopUpQuestionDialog(context)

    fun ShowProgressDialog(message: String) {
        progressDialog.setMessage(message)
        if(!progressDialog.isShowing){
            progressDialog.show()
        }
    }

    fun DismissProgressDialog() {
        progressDialog.dismiss()
    }

    fun ShowMessageDialog(title: String, message: String) {
        messageDialog.show(title, message)
    }

    fun ShowMessageDialog(title: String, message: String, callback: GenericDelegate) {
        messageDialog.show(title, message, callback)
    }

    fun ShowMessageDialog(title: String, message: String, option: DialogOption) {
        messageDialog.show(title, message, option)
    }

    fun DismissMessageDialog() {
        messageDialog.dismiss()
    }

    fun ShowQuestionDialog(title: String, message: String, leftOption: DialogOption, rightOption: DialogOption) {
        questionDialog.show(title, message, leftOption, rightOption)
    }

    fun DismissQuestionDialog() {
        questionDialog.dismiss()
    }
}