package com.mha.note.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.security.crypto.EncryptedSharedPreferences
import com.mha.note.constants.TagName
import com.mha.note.R
import com.mha.note.databinding.ActivityOnboardBinding
import com.mha.note.utils.AesEncryptor
import com.mha.note.utils.Tools

class OnBoardingActivity  : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardBinding

    private lateinit var mEncryptor: AesEncryptor
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvErrMsg: TextView
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityOnboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        init()
        setOnClickListener()
        //setFakeData()
    }

    private fun init(){
        etUsername = binding.etUsername
        etPassword = binding.etPassword
        tvErrMsg = binding.tvErrMsg
        btnLogin = binding.btnLogin

        mEncryptor = AesEncryptor()

        preferences = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            try {
                EncryptedSharedPreferences.create(
                    this,
                    TagName.SHARED_PREFERENCES,
                    Tools.getMasterKey(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                getSharedPreferences(TagName.SHARED_PREFERENCES, Context.MODE_PRIVATE)
            }
        }else{
            getSharedPreferences(TagName.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        }
    }

    private fun setOnClickListener(){
        etPassword.setOnEditorActionListener{ v, actionId, event ->
            when(actionId){
                EditorInfo.IME_ACTION_DONE -> {
                    login()
                    true
                }
                else -> false
            }
        }

        btnLogin.setOnClickListener {
            login()
        }
    }

    private fun setFakeData(){
        etUsername.setText("HelloWorld")
        etPassword.setText("Password1!")
    }

    private fun login(){
        val username = etUsername.text.toString()
        val password = etPassword.text.toString()

        if(username.isEmpty() || password.isEmpty()){
            showErrMsg(getString(R.string.IncompleteFields))
        }else{
            hideErrMsg()
            if(validateLogin(username, password)){
                saveToSharedPreference(username, password)
                switchToMainActivity()
            }else{
                showErrMsg(getString(R.string.InvalidCredentials))
                //clearSharedPreference()
            }
        }
    }

    private fun showErrMsg(message: String){
        tvErrMsg.text = message
        tvErrMsg.visibility = View.VISIBLE
    }

    private fun hideErrMsg(){
        tvErrMsg.visibility = View.GONE
    }

    private fun validateLogin(username: String, password: String): Boolean{
        val originalUsername = preferences.getString(TagName.SP_LOGIN_ID, "")
        val originalPassword = preferences.getString(TagName.SP_LOGIN_PW, "")
        val decryptedPassword = if(originalPassword!!.isNotEmpty()){
            mEncryptor.decrypt(originalPassword)
        }else{
            ""
        }

        //Log.e("VALIDATE LOGIN", "Password: $password, Encrypted Password: $originalPassword, Decrypted Password: $decryptedPassword")
        return if(originalUsername!!.isEmpty() || decryptedPassword!!.isEmpty()){
            true
        }else if(originalUsername != username){
            true
        }else originalUsername == username && decryptedPassword == password
    }

    private fun saveToSharedPreference(username: String, password: String){
        val encryptedPassword = mEncryptor.encrypt(password)
        //Log.e("SAVING", "Password: $password, Encrypted Password: $encryptedPassword")
        editor = preferences.edit()
        editor.putString(TagName.SP_LOGIN_ID, username)
        editor.putString(TagName.SP_LOGIN_PW, encryptedPassword)
        editor.apply()
    }

    private fun clearSharedPreference(){
        editor = preferences.edit()
        editor.putString(TagName.SP_LOGIN_ID, "")
        editor.putString(TagName.SP_LOGIN_PW, "")
        editor.putString(TagName.SP_AES_IV, "")
        editor.apply()
    }

    private fun switchToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.finish()
    }
}