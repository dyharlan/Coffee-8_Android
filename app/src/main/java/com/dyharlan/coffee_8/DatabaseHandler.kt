package com.dyharlan.coffee_8

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object{
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "data.db"
        private val TABLE_FLAGS = "rpl_flags"
        private val KEY_ID = "crc32Checksum"
        private val KEY_FLAG0 = "flag0"
        private val KEY_FLAG1 = "flag1"
        private val KEY_FLAG2 = "flag2"
        private val KEY_FLAG3 = "flag3"
        private val KEY_FLAG4 = "flag4"
        private val KEY_FLAG5 = "flag5"
        private val KEY_FLAG6 = "flag6"
        private val KEY_FLAG7 = "flag7"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_FLAGS_TABLE = ("CREATE TABLE $TABLE_FLAGS (" +
                "$KEY_ID INT NOT NULL," +
                "$KEY_FLAG0 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG1 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG2 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG3 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG4 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG5 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG6 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG7 INT DEFAULT 0 NOT NULL," +
                "PRIMARY KEY($KEY_ID)" +
                ");")
        db?.execSQL(CREATE_FLAGS_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_FLAGS")
        onCreate(db)
    }

    fun saveFlags(checksum: Long,flags: Array<Int>){
        val writableDb = this.writableDatabase
        val cursor: Cursor = writableDb.query(false,TABLE_FLAGS, Array<String>(1){KEY_ID},"$KEY_ID=?",Array<String>(1){checksum.toString()},null,null,null,null)
        val contentValues = ContentValues()

        for((index, x) in flags.withIndex()){
            contentValues.put("flag${index}", flags[x] and 0xFF)
        }
        if(cursor.moveToFirst()){
            writableDb.update(TABLE_FLAGS, contentValues, "$KEY_ID = ?", Array<String>(1){checksum.toString()})
        }else{
            contentValues.put(KEY_ID, checksum)
            writableDb.insert(TABLE_FLAGS,null, contentValues)
        }
        cursor.close()
        writableDb.close()
    }

    @SuppressLint("Range")
    fun loadFlags(checksum: Long):IntArray{
        val readableDb = this.readableDatabase
        val cursor: Cursor = readableDb.query(false,TABLE_FLAGS,
        arrayOf(
            KEY_ID,
            KEY_FLAG0,
            KEY_FLAG1,
            KEY_FLAG2,
            KEY_FLAG3,
            KEY_FLAG4,
            KEY_FLAG5,
            KEY_FLAG6,
            KEY_FLAG7,
            ),
            "$KEY_ID=?",
            Array<String>(1){checksum.toString()},
            null,
            null,
            null,
            null)
        var flags: IntArray
        if(cursor.moveToFirst()){
            do {
                flags = intArrayOf(
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG0)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG1)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG2)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG3)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG4)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG5)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG6)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG7))
                )
            }while(cursor.moveToNext())
        }else{
            flags = intArrayOf(0,0,0,0,0,0,0,0)
        }
        cursor.close()
        readableDb.close()
        return flags
    }


}