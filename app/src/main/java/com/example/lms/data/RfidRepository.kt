package com.example.lms.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.lms.utils.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class RfidRepository(private val rfidDao: RfidDao) {

    val readAllData: LiveData<List<Rfid>> = rfidDao.readAllRfid()

    val readAllBookDetails: LiveData<List<BookDetailsModel>> = rfidDao.readAllBookDetails()


    val allRfid = MutableLiveData<List<Rfid>>()

     suspend fun addRfid(rfid: Rfid){
        rfidDao.addRfid(rfid)
    }

    suspend fun addBookDetails(bookDetailsModel: BookDetailsModel){
        rfidDao.addBookDetails(bookDetailsModel)
    }

    suspend fun deleteAllRfid(){
        rfidDao.deleteAllRfid()
    }


    suspend fun deleteAllBookDetails(){
        rfidDao.deleteAllBookDetails()
    }

    suspend fun getAllRfid(rfidTagNo: String) = flow{
        emit(NetworkResult.Loading())
        val response = rfidDao.getAllRfid(rfidTagNo)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error("No Data Found"?: "UnknownError"))
    }


    suspend fun findRfid(rfid:String):Rfid{
        return rfidDao.getAllRfid(rfid)
    }



}