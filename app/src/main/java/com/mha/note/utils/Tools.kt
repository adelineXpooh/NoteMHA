package com.mha.note.utils

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import android.content.Context.CLIPBOARD_SERVICE

import androidx.security.crypto.MasterKey
import com.mha.note.MyApplication
import java.security.GeneralSecurityException

object Tools{

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun calculateInSampleSize(bitmap: Bitmap, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = bitmap.height
        val width = bitmap.width
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun scaleBitmap(bitmap: Bitmap, wantedWidth: Int, wantedHeight: Int): Bitmap {
        val originalWidth = bitmap.width.toFloat()
        val originalHeight = bitmap.height.toFloat()
        val output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val m = Matrix()

        val scalex = wantedWidth / originalWidth
        val scaley = wantedHeight / originalHeight
        val xTranslation = 0.0f
        val yTranslation = (wantedHeight - originalHeight * scaley) / 2.0f

        m.postTranslate(xTranslation, yTranslation)
        m.preScale(scalex, scaley)
        // m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        val paint = Paint()
        paint.isFilterBitmap = true
        canvas.drawBitmap(bitmap, m, paint)

        return output
    }

    fun decodeSampledBitmapFromResource(res: Resources, resId: Int, reqWidth: Int, reqHeight: Int): Bitmap {

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)
    }

    fun decodeSampledBitmapFromStream(urlDisplay: String, reqWidth: Int, reqHeight: Int): Bitmap? {

        var bitmap: Bitmap? = null

        val connection: HttpURLConnection? = null
        try {
            var stream: InputStream? = java.net.URL(urlDisplay).openStream()

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            bitmap = BitmapFactory.decodeStream(stream, null, options)

            // reset the stream
            stream = java.net.URL(urlDisplay).openStream()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            bitmap = BitmapFactory.decodeStream(stream, null, options)

            stream?.close()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        return bitmap
    }

    fun decodeSampledBitmapFromStream(urlDisplay: String): Bitmap? {

        var bitmap: Bitmap? = null

        val connection: HttpURLConnection? = null
        try {
            val stream = java.net.URL(urlDisplay).openStream()

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            bitmap = BitmapFactory.decodeStream(stream, null, options)

            stream?.close()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        return bitmap
    }

    fun getSquareBitmap(srcBmp: Bitmap): Bitmap {
        val dstBmp: Bitmap
        if (srcBmp.width >= srcBmp.height) {
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.width / 2 - srcBmp.height / 2,
                    0,
                    srcBmp.height,
                    srcBmp.height
            )
        } else {
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.height / 2 - srcBmp.width / 2,
                    srcBmp.width,
                    srcBmp.width
            )
        }
        return dstBmp
    }

    fun bitmapToBase64String(bitmap: Bitmap?): String {
        var bitmap = bitmap
        val outputStream = ByteArrayOutputStream()
        try {
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // 0 - Max Compression, 100 - Least Compression
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val byteArrayImage = outputStream.toByteArray()

        //prevent out of memory
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
            bitmap = null
        }

        return Base64.encodeToString(byteArrayImage, Base64.DEFAULT) //Base64.NO_WRAP
    }

    fun Base64StringToBitmap(encodedBitmap: String): Bitmap {
        val byteArrayImage = Base64.decode(encodedBitmap, 0)
        return BitmapFactory.decodeByteArray(byteArrayImage, 0, byteArrayImage.size)
    }

    fun bitmapToByteArray(mBitmap: Bitmap): ByteArray{
        val stream = ByteArrayOutputStream()
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap{
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun haveNetworkConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo
        if (netInfo != null && netInfo.isConnected
                && netInfo.isConnectedOrConnecting
                && netInfo.isAvailable) {
            return true
        }
        return false
    }

    fun isValidEmail(target: CharSequence): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    fun isValidMobile(mobileNo: String): Boolean{
        if(mobileNo.length != 8)
            return false

        if(!mobileNo.startsWith("8") && !mobileNo.startsWith("9"))
            return false

        return true
    }

    fun isValidNRIC(nric: String): Boolean {
        if (nric.length != 9) {
            return false
        }

        val nricCharacters = nric.toUpperCase(Locale.ROOT).toCharArray()

        if (nricCharacters[0] != 'S' && nricCharacters[0] != 'T' && nricCharacters[0] != 'F' && nricCharacters[0] != 'G') {
            return false
        }

        var total = 0
        for (i in 1..7) {
            val no = Character.getNumericValue(nricCharacters[i])
            when (i) {
                1, 7 -> total += no * 2
                2 -> total += no * 7
                3 -> total += no * 6
                4 -> total += no * 5
                5 -> total += no * 4
                6 -> total += no * 3
            }
        }

        if (nricCharacters[0] == 'T' || nricCharacters[0] == 'G') {
            total += 4
        }

        val remainder = total % 11

        var finalChar = ' '
        if (nricCharacters[0] == 'S' || nricCharacters[0] == 'T') {
            when (remainder) {
                0 -> finalChar = 'J'
                1 -> finalChar = 'Z'
                2 -> finalChar = 'I'
                3 -> finalChar = 'H'
                4 -> finalChar = 'G'
                5 -> finalChar = 'F'
                6 -> finalChar = 'E'
                7 -> finalChar = 'D'
                8 -> finalChar = 'C'
                9 -> finalChar = 'B'
                10 -> finalChar = 'A'
            }
        } else if (nricCharacters[0] == 'F' || nricCharacters[0] == 'G') {
            when (remainder) {
                0 -> finalChar = 'X'
                1 -> finalChar = 'W'
                2 -> finalChar = 'U'
                3 -> finalChar = 'T'
                4 -> finalChar = 'R'
                5 -> finalChar = 'Q'
                6 -> finalChar = 'P'
                7 -> finalChar = 'N'
                8 -> finalChar = 'M'
                9 -> finalChar = 'L'
                10 -> finalChar = 'K'
            }
        }

        return (finalChar == nricCharacters[nricCharacters.size - 1])

    }

    fun isValidPassword(password: String): Boolean {
        if(password.length < 8)
            return false

        if(!checkForSpecialCharacters(password))
            return false

        if(containsNumberOnly(password))
            return false

        if(containsAlphabetOnly(password))
            return false

        if(password == password.toLowerCase(Locale.ROOT))
        return false

        if(password == password.toUpperCase(Locale.ROOT))
        return false

        return true
    }

    fun checkStringWithRegex(str: String, regex: String): Boolean{
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(str)
        return matcher.find()
    }

    fun LogString(TAG: String, message: String) {
        val maxLogSize = 1000
        for (i in 0..message.length / maxLogSize) {
            val start = i * maxLogSize
            var end = (i + 1) * maxLogSize
            end = if (end > message.length) message.length else end
            Log.e(TAG, message.substring(start, end))
        }
    }

    fun fromHtml(html: String): Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }

    fun containsHtml(strToCheck: String): Boolean{
        return strToCheck.contains("</p>") || strToCheck.contains("<br>") ||
                strToCheck.contains("</span>") || strToCheck.contains("</style>") ||
                strToCheck.contains("</table>") || strToCheck.contains("</tr>")||
                strToCheck.contains("</tbody>") || strToCheck.contains("</td>")
        //return checkStringWithRegex(strToCheck, "<(\"[^\"]*\"|'[^']*'|[^'\">])*>")
    }

    fun getScreenWidth(context: Context): Int{
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }

    fun sha256Hash(text: String): String {
        var md: MessageDigest? = null
        try {
            md = MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        md!!.update(text.toByteArray())

        val byteData = md.digest()

        //convert the byte to hex format
        val hexString = StringBuffer()
        for (i in byteData.indices) {
            val hex = Integer.toHexString(0xff and byteData[i].toInt())
            if (hex.length == 1)
                hexString.append('0')
            hexString.append(hex)
        }

        return hexString.toString()
    }

    fun checkForSpecialCharacters(strToCheck: String): Boolean {
        val p = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE)
        val m = p.matcher(strToCheck)
        return m.find()
    }

    fun containsNumberOnly(strToCheck: String): Boolean {
        val p = Pattern.compile("^[0-9]+$", Pattern.CASE_INSENSITIVE)
        val m = p.matcher(strToCheck)
        return m.find()
    }

    fun containsAlphabetOnly(strToCheck: String): Boolean{
        for(i in 0 until strToCheck.length){
            if(!Character.isLetter(strToCheck[i])){
                return false
            }
        }
        return true
    }

    fun dpToPx(context: Context, dp: Int): Int {
        return Math.round(dp * getPixelScaleFactor(context))
    }

    fun pxToDp(context: Context, px: Int): Int {
        return Math.round(px / getPixelScaleFactor(context))
    }

    fun getDimenInDP(context: Context, px: Float): Float {
        return (px/context.resources.displayMetrics.density)
    }

    private fun getPixelScaleFactor(context: Context): Float {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT
    }

    fun getDate(dateStr: String): Date? {
        if (dateStr.isNotEmpty() && dateStr != "null") {
            var dateToFormat = ""
            for (i in 0 until dateStr.length) {
                if (i > 5 && i < dateStr.length - 2) {
                    dateToFormat += dateStr[i]
                }
            }

            val date = java.lang.Long.parseLong(dateToFormat)
            val timeZone = TimeZone.getTimeZone("GMT")

            val calendar = Calendar.getInstance(timeZone)
            calendar.timeInMillis = date
            calendar.add(Calendar.HOUR, 8)

            return calendar.time
        }

        return null
    }

    fun getCurrentDateWithoutTime(): Date{
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    fun formatDate(dateToFormat: String, strDateFormat: String, resultDateFormat: String): String {
        if (dateToFormat.isEmpty() || strDateFormat.isEmpty() || resultDateFormat.isEmpty()) {
            return ""
        }

        return try {
            val dateFormat = SimpleDateFormat(strDateFormat, Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("Asia/Singapore")
            val date = dateFormat.parse(dateToFormat)
            dateFormat.applyPattern(resultDateFormat)
            dateFormat.format(date)
        } catch (pe: ParseException) {
            pe.printStackTrace()
            ""
        } catch (e: Exception){
            e.printStackTrace()
            ""
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun isBirthdayMonth(birthStr: String): Boolean {
        val calendar = Calendar.getInstance()
        val currMonth = calendar.get(Calendar.MONTH)

        var birthdate: Date? = null
        try {
            birthdate = SimpleDateFormat("yyyy-MM-dd").parse(birthStr)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (birthdate != null) {
            calendar.time = birthdate
            val birthMonth = calendar.get(Calendar.MONTH)
            return currMonth == birthMonth
        }

        return false
    }

    fun getYearAge(dobStr: String, strDateFormat: String): Int{
        val calendar = Calendar.getInstance()
        val currYear = calendar.get(Calendar.YEAR)

        val dateFormat = SimpleDateFormat(strDateFormat, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Singapore")
        val dob = dateFormat.parse(dobStr)
        calendar.time = dob
        val birthYear = calendar.get(Calendar.YEAR)

        return currYear - birthYear
    }

    fun readJsonFile(context: Context, fileName: String): String {
        var jsonStr = ""
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            jsonStr = String(buffer, charset("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return jsonStr
    }

    fun getValueFromList(key: String, arrayList: ArrayList<HashMap<String, String>>?, keyTag: String, targetTag: String): String? {
        var value: String? = ""
        var found = false
        var idx = 0

        if (arrayList != null) {
            while (!found && idx < arrayList.size) {
                val hashMap = arrayList[idx]
                if (hashMap[keyTag] == key) {
                    found = true
                    value = hashMap[targetTag]
                } else {
                    ++idx
                }
            }
        }

        return value
    }

    fun getUUIDString(): String {
        return UUID.randomUUID().toString()
    }

    fun postalCodeLookUp(context: Context, postalCode: String): Address?{
        val geocoder = Geocoder(context, Locale.getDefault())

        return try{
            val originalAddressList = geocoder.getFromLocationName(postalCode, 1)
            val firstAddress = originalAddressList[0]
            val filteredAddressList = geocoder.getFromLocation(firstAddress.latitude, firstAddress.longitude, 1)

            filteredAddressList[0]
        }catch(e: java.lang.Exception){
            e.printStackTrace()
            null
        }
    }

    fun getAddress(address: Address?): String{
        return if(address == null){
            ""
        }else{
            LogString("ADDRESS", address.toString())
            var addressStr = if(!address.featureName.isNullOrBlank()){
                "${address.thoroughfare} ${address.featureName}"
            }else {
                address.thoroughfare
            }

            if(!address.premises.isNullOrBlank() || address.premises.isNotEmpty()){
                addressStr += " ${address.premises}"
            }

            if(!address.subThoroughfare.isNullOrBlank() || address.subThoroughfare.isNotEmpty()){
                addressStr += " ${address.subThoroughfare}"
            }

            addressStr
        }
    }

    fun copyToClipboard(context: Context, label:String, textToCopy: String){
        val clipboard: ClipboardManager? = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        val clip: ClipData = ClipData.newPlainText(label, textToCopy)
        clipboard?.setPrimaryClip(clip)
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    fun getMasterKey(): MasterKey {
        return MasterKey.Builder(MyApplication.getContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    @SuppressLint("SimpleDateFormat")
    fun isAppUpdateRequired(publishedDate: String, noOfDays: Int): Boolean {
        val sdf = SimpleDateFormat("MMMMM dd, yyyy")  //Date format: July 25, 2016
        val cal = Calendar.getInstance()
        val currentDate = cal.time

        var needsUpdate = false
        try {
            val updateDate = sdf.parse(publishedDate)
            cal.time = updateDate
            cal.add(Calendar.DATE, noOfDays)
            val dateLimit = cal.time
            if (currentDate.after(dateLimit))
                needsUpdate = true
        } catch (pe: ParseException) {
            pe.printStackTrace()
        }

        return needsUpdate
    }

    fun isStoreAppVersionLarger(currVersion: String, storeVersion: String): Boolean {
        var isLarger = false

        var currVersionStr:String = if(currVersion.contains("(")){
             currVersion.split("(").toTypedArray()[0]
        }else{
            currVersion
        }

        //Log.e("CHECK", currVersionStr)

        val currStr = currVersionStr.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val newStr = storeVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val cVersion = ArrayList<Int>()
        val sVersion = ArrayList<Int>()

        for (i in currStr.indices) {
            cVersion.add(Integer.parseInt(currStr[i]))
        }

        for (i in newStr.indices) {
            sVersion.add(Integer.parseInt(newStr[i]))
        }

        if (cVersion.size != sVersion.size) {
            if (cVersion.size < sVersion.size) {
                val diff = sVersion.size - cVersion.size
                for (i in 0 until diff) {
                    cVersion.add(0)
                }
            } else {
                val diff = cVersion.size - sVersion.size
                for (i in 0 until diff) {
                    sVersion.add(0)
                }
            }
        }

        var i = 0
        do {
            if (cVersion[i] > sVersion[i]) {
                i = cVersion.size
            } else if (cVersion[i] < sVersion[i]) {
                isLarger = true
            } else {
                ++i
            }
        } while (i < cVersion.size && !isLarger)

        return isLarger
    }
}