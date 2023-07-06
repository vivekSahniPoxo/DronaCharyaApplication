package com.example.lms.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "book_details_table")
data class BookDetailsModel( @PrimaryKey(autoGenerate = true)
                             val id: Int,val Title:String,val AccessNo:String)
