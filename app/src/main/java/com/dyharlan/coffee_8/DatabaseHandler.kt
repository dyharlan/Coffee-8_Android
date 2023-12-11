package com.dyharlan.coffee_8

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.dyharlan.coffee_8.Backend.MachineType


class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object{
        private val DATABASE_VERSION = 7
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
        private val KEY_FLAG8 = "flag8"
        private val KEY_FLAG9 = "flag9"
        private val KEY_FLAG10 = "flag10"
        private val KEY_FLAG11 = "flag11"
        private val KEY_FLAG12 = "flag12"
        private val KEY_FLAG13 = "flag13"
        private val KEY_FLAG14 = "flag14"
        private val KEY_FLAG15 = "flag15"

        private val TABLE_ROM_CONFIGS = "rom_configs"

        private val KEY_MACHINECODE = "machine_code"
        private val KEY_CYCLES = "cycle_count"

        private val TABLE_MACHINE_TYPES = "machine_types"
        private val KEY_MACHINENAME = "machine_name"

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
                "$KEY_FLAG8 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG9 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG10 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG11 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG12 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG13 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG14 INT DEFAULT 0 NOT NULL," +
                "$KEY_FLAG15 INT DEFAULT 0 NOT NULL," +
                "PRIMARY KEY($KEY_ID)" +
                ");")
        db?.execSQL(CREATE_FLAGS_TABLE)

        val CREATE_MACHINE_TYPES_TABLE = ("CREATE TABLE $TABLE_MACHINE_TYPES (" +
                "$KEY_MACHINECODE INT NOT NULL," +
                "$KEY_MACHINENAME VARCHAR(32) NOT NULL," +
                "PRIMARY KEY($KEY_MACHINECODE)" +
                ");")
        db?.execSQL(CREATE_MACHINE_TYPES_TABLE)
        val contentValues = ContentValues()
        contentValues.put(KEY_MACHINECODE, 0)
        contentValues.put(KEY_MACHINENAME, MachineType.COSMAC_VIP.machineName)
        db?.insert(TABLE_MACHINE_TYPES, null, contentValues)

        contentValues.clear()
        contentValues.put(KEY_MACHINECODE, 1)
        contentValues.put(KEY_MACHINENAME, MachineType.SUPERCHIP_1_1.machineName)
        db?.insert(TABLE_MACHINE_TYPES, null, contentValues)

//        contentValues.clear()
//        contentValues.put(KEY_MACHINECODE, 2)
//        contentValues.put(KEY_MACHINENAME, MachineType.SUPERCHIP_1_1_COMPAT.machineName)
//        db?.insert(TABLE_MACHINE_TYPES, null, contentValues)

        contentValues.clear()
        contentValues.put(KEY_MACHINECODE, 2)
        contentValues.put(KEY_MACHINENAME, MachineType.XO_CHIP.machineName)
        db?.insert(TABLE_MACHINE_TYPES, null, contentValues)

        contentValues.clear()

        val CREATE_ROM_CONFIGS_TABLE = ("CREATE TABLE $TABLE_ROM_CONFIGS (" +
                "$KEY_ID INT NOT NULL," +
                "$KEY_MACHINECODE INT NOT NULL," +
                "$KEY_CYCLES INT NOT NULL," +
                "PRIMARY KEY ($KEY_ID)," +
                "FOREIGN KEY ($KEY_MACHINECODE) REFERENCES $TABLE_MACHINE_TYPES($KEY_MACHINECODE)" +
                ");")
        db?.execSQL(CREATE_ROM_CONFIGS_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
       if(db != null){
           db.execSQL("DROP TABLE IF EXISTS $TABLE_FLAGS")
           db.execSQL("DROP TABLE IF EXISTS $TABLE_ROM_CONFIGS")
           db.execSQL("DROP TABLE IF EXISTS $TABLE_MACHINE_TYPES")
           onCreate(db)
       }

    }
    fun saveConfigs(config: RomConfigClass):Boolean{
        var status = false
        val writableDb = this.writableDatabase
        val cursor: Cursor = writableDb.query(false, TABLE_ROM_CONFIGS, Array<String>(1){KEY_ID}, "$KEY_ID=?",Array<String>(1){config.checksum.toString()}, null,null,null,null)
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, config.checksum)

        when(config.machineType){
            MachineType.COSMAC_VIP -> contentValues.put(KEY_MACHINECODE, 0)
            MachineType.SUPERCHIP_1_1 -> contentValues.put(KEY_MACHINECODE, 1)
            //MachineType.SUPERCHIP_1_1_COMPAT -> contentValues.put(KEY_MACHINECODE, 2)
            MachineType.XO_CHIP -> contentValues.put(KEY_MACHINECODE, 2)
            else -> contentValues.put(KEY_MACHINECODE, 2)
        }

        contentValues.put(KEY_CYCLES, config.cycles)

        try{
            if(cursor.moveToFirst()){
                writableDb.update(TABLE_ROM_CONFIGS, contentValues, "$KEY_ID = ?", Array<String>(1){config.checksum.toString()})
            }else {
                writableDb.insertOrThrow(TABLE_ROM_CONFIGS, null, contentValues)
            }
            status = true
        }catch(sqle: SQLiteException){
            throw sqle
        }finally {
            cursor.close()
            writableDb.close()
        }


        return status
    }

    @SuppressLint("Range")
    fun loadConfigs(checksum: Long): RomConfigClass{
        val readableDb = this.readableDatabase

        val cursor: Cursor = readableDb.query(false, TABLE_ROM_CONFIGS, arrayOf(KEY_ID, KEY_MACHINECODE, KEY_CYCLES), "$KEY_ID=?",Array<String>(1){checksum.toString()}, null,null,null,null)
        val romConfig: RomConfigClass
        if(cursor.moveToFirst()){
            val machineType:MachineType = when(cursor.getInt(cursor.getColumnIndex(KEY_MACHINECODE))){
                0 -> MachineType.COSMAC_VIP
                1 -> MachineType.SUPERCHIP_1_1
                //2 -> MachineType.SUPERCHIP_1_1_COMPAT
                2 -> MachineType.XO_CHIP
                else -> MachineType.XO_CHIP
            }
            val cycles = cursor.getInt(cursor.getColumnIndex(KEY_CYCLES))
            romConfig = RomConfigClass(checksum, machineType, cycles)
        }else{
            romConfig = RomConfigClass(checksum, MachineType.NONE, -1)
        }
        cursor.close()
        readableDb.close()
        return romConfig
    }
    fun saveFlags(checksum: Long,flags: Array<Int>){
        val writableDb = this.writableDatabase
        val cursor: Cursor = writableDb.query(false,TABLE_FLAGS, Array<String>(1){KEY_ID},"$KEY_ID=?",Array<String>(1){checksum.toString()},null,null,null,null)
        val contentValues = ContentValues()

        for(index in flags.indices){
            contentValues.put("flag${index}", flags[index] and 0xFF)
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
            KEY_FLAG8,
            KEY_FLAG9,
            KEY_FLAG10,
            KEY_FLAG11,
            KEY_FLAG12,
            KEY_FLAG13,
            KEY_FLAG14,
            KEY_FLAG15,
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
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG7)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG8)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG9)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG10)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG11)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG12)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG13)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG14)),
                    cursor.getInt(cursor.getColumnIndex(KEY_FLAG15)),
                )
            }while(cursor.moveToNext())
        }else{
            flags = intArrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
        }
        cursor.close()
        readableDb.close()
        return flags
    }


}