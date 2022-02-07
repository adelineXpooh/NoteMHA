package com.mha.note.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import com.mha.note.constants.TagName
import com.mha.note.MyApplication
import com.stringcare.library.SC
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesEncryptor {
    private val secretKeyString = SC.obfuscate("9hfidghsy7aui4g5yr2rfhgfdg")

    private var secretKeySpec: SecretKeySpec? = null
    //private val secretKey: SecretKey
    //"AES/ECB/PKCS5PADDING" vs "AES/CBC/PKCS5PADDING"
    private val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")

    private var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    init {
        //val keygen = KeyGenerator.getInstance("AES")
        //keygen.init(256)
        //secretKey = keygen.generateKey()
        setKey()

        preferences = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            try {
                EncryptedSharedPreferences.create(
                    MyApplication.getContext(),
                    TagName.SHARED_PREFERENCES,
                    Tools.getMasterKey(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                MyApplication.getContext().getSharedPreferences(TagName.SHARED_PREFERENCES, Context.MODE_PRIVATE)
            }
        }else{
            MyApplication.getContext().getSharedPreferences(TagName.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        }
    }

    // set Key
    private fun setKey() {
        var sha: MessageDigest? = null
        try {
            //Log.e("AES KEY", "BEFORE: $secretKeyString, After: ${SC.reveal(secretKeyString)}")
            var key = SC.reveal(secretKeyString).toByteArray(charset("UTF-8"))
            sha = MessageDigest.getInstance("SHA-1")
            key = sha.digest(key)
            key = key.copyOf(16)
            secretKeySpec = SecretKeySpec(key, "AES")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    // method to encrypt the secret text using key
    fun encrypt(strToEncrypt: String): String? {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val cipherText = cipher.doFinal(strToEncrypt.toByteArray(charset("UTF-8")))

            editor = preferences.edit()
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                editor.putString(TagName.SP_AES_IV, Base64.getEncoder().encodeToString(cipher.iv))
                editor.apply()
                Base64.getEncoder().encodeToString(cipherText)
            } else {
                editor.putString(TagName.SP_AES_IV, android.util.Base64.encodeToString(cipher.iv, android.util.Base64.DEFAULT))
                editor.apply()
                android.util.Base64.encodeToString(cipherText, android.util.Base64.DEFAULT)
            }
        } catch (e: Exception) {
            println("Error while encrypting: $e")
        }
        return null
    }

    // method to encrypt the secret text using key
    fun decrypt(strToDecrypt: String?): String? {
        try {
            val ivString = preferences.getString(TagName.SP_AES_IV, "")
            val iv = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getDecoder().decode(ivString)
            }else{
                android.util.Base64.decode(ivString, android.util.Base64.DEFAULT)
            }
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(iv))
            //cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)))
            } else {
                String(cipher.doFinal(android.util.Base64.decode(strToDecrypt, android.util.Base64.DEFAULT)))
            }
        } catch (e: Exception) {
            println("Error while decrypting: $e")
        }
        return null
    }
}