package com.mha.note.objects

import java.io.File
import java.io.Serializable

data class Recording(var file: File?, var id: Int, var filename: String, var note: String, var timeStamp: Long, var duration: String): Serializable{

    constructor():
            this(null, 0, "", "", 0, "")

    constructor(file: File):
            this(file, 0, "", "", 0, "")

    constructor(id: Int, filename: String, note: String, timeStamp: Long, duration: String):
            this(null, id, filename, note, timeStamp, duration)
}
