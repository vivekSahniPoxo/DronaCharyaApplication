package com.example.lms.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface RfidDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
     suspend fun addRfid(rfid: Rfid)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBookDetails(bookDetailsModel: BookDetailsModel)

    @Query("SELECT COUNT(*) FROM book_details_table")
    suspend fun getCountOfBooks(): Int

    @Query("SELECT * FROM rfid_table ORDER BY id ASC")
      fun readAllRfid(): LiveData<List<Rfid>>

    @Query("SELECT * FROM book_details_table ORDER BY id ASC")
    fun readAllBookDetails(): LiveData<List<BookDetailsModel>>

    @Query("DELETE FROM book_details_table")
    suspend fun deleteAllBookDetails()

    @Query("SELECT * FROM rfid_table WHERE rfidTagNo = :rfidTagNo")
    suspend fun getAllRfid(rfidTagNo: String): Rfid
    //suspend fun getAllRfid(rfidTagNo: String): LiveData<List<Rfid>>

    @Query("DELETE FROM rfid_table")
    suspend fun deleteAllRfid()

}