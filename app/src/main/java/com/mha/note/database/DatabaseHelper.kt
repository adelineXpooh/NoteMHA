package com.mha.note.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mha.note.objects.Recording
import java.lang.String
import kotlin.Int
import kotlin.Long
import kotlin.arrayOf

val DATABASE_VERSION = 1
val DATABASE_NAME = "recordings_db"

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private lateinit var mInstance: DatabaseHelper

        fun getInstance(context: Context): DatabaseHelper {
            if (!this::mInstance.isInitialized) {
                mInstance = DatabaseHelper(context)
            }
            return mInstance
        }
    }

    private val TABLE_NAME = "Recordings"

    private val COLUMN_ID = "id"
    private val COLUMN_FILENAME = "file_name"
    private val COLUMN_DESCRIPTION = "description"
    private val COLUMN_TIMESTAMP = "timestamp"
    private val COLUMN_DURATION = "duration"

    // Create table SQL query
    private val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FILENAME + " TEXT,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_TIMESTAMP + " INTEGER," //" DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COLUMN_DURATION + " TEXT"
            + ")")

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        // Create tables again
        onCreate(db)
    }

    fun addRecording(newRecording: Recording): Long{
        // get writable database as we want to write data
        val db = this.writableDatabase

        val values = ContentValues()
        // 'id' and 'timestamp' will be inserted automatically
        values.put(COLUMN_FILENAME, newRecording.filename)
        values.put(COLUMN_DESCRIPTION, newRecording.note)
        values.put(COLUMN_TIMESTAMP, newRecording.timeStamp)
        values.put(COLUMN_DURATION, newRecording.duration)

        // insert row
        val id = db.insert(TABLE_NAME, null, values)

        // close db connection
        db.close()

        // return newly inserted row id
        return id
    }

    @SuppressLint("Range")
    fun getRecording(id: Long): Recording?{
        // get readable database as we are not inserting anything
        val db = this.readableDatabase

        val cursor: Cursor? = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_FILENAME, COLUMN_DESCRIPTION, COLUMN_TIMESTAMP, COLUMN_DURATION),
            "$COLUMN_ID=?",
            arrayOf(
                String.valueOf(id)
            ),
            null,
            null,
            null,
            null
        )

        cursor?.moveToFirst()

        val recording = cursor?.getInt(cursor.getColumnIndex(COLUMN_ID))?.let {
            Recording(
                it,
                cursor.getString(cursor.getColumnIndex(COLUMN_FILENAME)),
                cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)),
                cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP)),
                cursor.getString(cursor.getColumnIndex(COLUMN_DURATION))
            )
        }

        // close the db connection
        cursor?.close()

        return recording
    }

    @SuppressLint("Range", "Recycle")
    fun getAllRecordings(): ArrayList<Recording>{
        val mRecordings = ArrayList<Recording>()

        // Select All Query
        val selectQuery = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_TIMESTAMP DESC"

        val db = this.writableDatabase;
        val cursor = db.rawQuery(selectQuery, null)

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                val recording = Recording()
                recording.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                recording.filename = cursor.getString(cursor.getColumnIndex(COLUMN_FILENAME))
                recording.note = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION))
                recording.timeStamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP))
                recording.duration = cursor.getString(cursor.getColumnIndex(COLUMN_DURATION))

                mRecordings.add(recording)
            } while (cursor.moveToNext())
        }

        // close db connection
        db.close()

        return mRecordings
    }

    fun getRecordingsCount(): Int{
        val countQuery = "SELECT * FROM $TABLE_NAME"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)

        val count = cursor.count
        cursor.close()

        return count
    }

    fun deleteRecording(recording: Recording){
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(
            String.valueOf(recording.id)
        ))
        db.close()
    }
}