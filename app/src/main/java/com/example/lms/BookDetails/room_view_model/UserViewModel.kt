package com.example.lms.BookDetails.room_view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lms.data.BookDetailsModel
import com.example.lms.data.Rfid
import com.example.lms.data.RfidDatabase
import com.example.lms.data.RfidRepository
import com.example.lms.utils.NetworkResult
import com.example.lms.utils.SingleLiveEvent
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RoomDbViewModel(application: Application): AndroidViewModel(application) {

    val readAllData: LiveData<List<Rfid>>

    val readAllBookDetails:LiveData<List<BookDetailsModel>>

    private var repository: RfidRepository





    init {
        val userDao = RfidDatabase.getDatabase(application).rfidDao()
        repository = RfidRepository(userDao)
        readAllData = repository.readAllData
        readAllBookDetails = repository.readAllBookDetails
    }

//
//    private var _allBookDetails = Flow<NetworkResult<BookDetailsModel>>()
//    val allBookDetails:Flow<NetworkResult<BookDetailsModel>> = _allBookDetails
//



    fun addRfid(rfid: Rfid){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRfid(rfid)
        }
    }

    fun addBookDetails(bookDetailsModel: BookDetailsModel){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addBookDetails(bookDetailsModel)

        }
    }


    fun deleteAllRfid(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllRfid()
        }
    }

    fun deleteAllBookDetails(){
        viewModelScope.launch(Dispatchers.Default) {
            repository.deleteAllBookDetails()
        }
    }



    val clientSearchResults: MutableLiveData<List<Rfid>>

    init {
        val clientDB = RfidDatabase.getDatabase(application)
        val clientDao = clientDB.rfidDao()
        repository = RfidRepository(clientDao)
        clientSearchResults = repository.allRfid
    }

    private var _fetchItemFromRoomDb = MutableLiveData<NetworkResult<Rfid>>()
    val fetchItemFromRoomDb:LiveData<NetworkResult<Rfid>> = _fetchItemFromRoomDb
    fun getAllRfid(rfidTagNo: String){
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllRfid(rfidTagNo).collect {
                _fetchItemFromRoomDb.postValue(it)
            }
        }
    }

    suspend fun findRfid(rfid:String):Rfid{
        val deferred: Deferred<Rfid> = viewModelScope.async {
            repository.findRfid(rfid)
        }
        return deferred.await()
    }





}