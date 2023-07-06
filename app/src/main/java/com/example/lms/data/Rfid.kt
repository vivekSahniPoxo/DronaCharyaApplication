package com.example.lms.data

import androidx.room.*

@Entity(tableName = "rfid_table",indices = [Index(value = ["rfidTagNo"], unique = true)])
data class Rfid(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "rfidTagNo")
    val rfidTagNo:String)

