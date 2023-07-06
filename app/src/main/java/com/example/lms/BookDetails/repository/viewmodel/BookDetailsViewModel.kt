package com.example.lms.BookDetails.repository.viewmodel

import android.provider.SyncStateContract
import androidx.lifecycle.*
import com.example.lms.BookDetails.model.BookDetailsModel
import com.example.lms.BookDetails.model.GetRfidTagNoFromClientModel
import com.example.lms.BookDetails.repository.BookDetailsRepository
import com.example.lms.utils.NetworkResult
import com.example.lms.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class BookDetailsViewModel @Inject constructor(private val bookDetailsRepository: BookDetailsRepository):ViewModel() {

    private var _getBookDetailsResponseLiveData = SingleLiveEvent<NetworkResult<Response<BookDetailsModel>>>()
    val getBookDetailsResponseLiveData: SingleLiveEvent<NetworkResult<Response<BookDetailsModel>>> = _getBookDetailsResponseLiveData



//    val readAllData: LiveData<List<Rfid>>
//    private val repository: BookDetailsRepository

//    init {
//        var context:Context
//        val userDao = RfidDatabase.getDatabase(context).rfidDao()
//        repository = BookDetailsRepository(userDao)
//        readAllData = repository.readAllData
//    }








    fun getBookInfo(getRfidTagNoFromClientModel: GetRfidTagNoFromClientModel){
        viewModelScope.launch {
            bookDetailsRepository.bookDetails(getRfidTagNoFromClientModel).collect{
                _getBookDetailsResponseLiveData.postValue(it)
            }
        }
    }


    private var _confirmBookResponseLiveData = SingleLiveEvent<NetworkResult<String>>()
    val confirmBookResponseLiveData: SingleLiveEvent<NetworkResult<String>> = _confirmBookResponseLiveData

    fun confirm(getRfidTagNoFromClientModel: String){
        viewModelScope.launch {
            bookDetailsRepository.confirmBookDetails(getRfidTagNoFromClientModel).collect {
                _confirmBookResponseLiveData.postValue(it)
            }
        }
    }

    val createEmpResponseLiveData: LiveData<NetworkResult<BookDetailsModel>>
        get() = bookDetailsRepository.userCreateResponseLiveData

    fun createEmpId(getRfidTagNoFromClientModel: GetRfidTagNoFromClientModel){
        viewModelScope.launch {
            bookDetailsRepository.userCreate(getRfidTagNoFromClientModel)
        }
    }


}