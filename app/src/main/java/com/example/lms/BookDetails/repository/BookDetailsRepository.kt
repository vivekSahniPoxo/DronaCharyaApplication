package com.example.lms.BookDetails.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.lms.BookDetails.model.BookDetailsModel
import com.example.lms.BookDetails.model.GetRfidTagNoFromClientModel
import com.example.lms.BookDetails.model.RfidNo
import com.example.lms.apies.Apies

import com.example.lms.utils.NetworkResult
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class BookDetailsRepository @Inject constructor(private val apies: Apies) {


    //val readAllData: LiveData<List<Rfid>> = userDao.readAllData()

     fun bookDetails(getRfidTagNoFromClientModel: GetRfidTagNoFromClientModel) = flow{
        emit(NetworkResult.Loading())
        val response = apies.bookDetails(getRfidTagNoFromClientModel)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error(e.message ?: "UnknownError"))
    }



    suspend fun confirmBookDetails(getRfidTagNoFromClientModel: String) = flow{
        emit(NetworkResult.Loading())
        val response = apies.confirmBook(getRfidTagNoFromClientModel)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error(e.message ?: "UnknownError"))
    }


    private val _userCreateResponseLiveData = MutableLiveData<NetworkResult<BookDetailsModel>>()
    val userCreateResponseLiveData: LiveData<NetworkResult<BookDetailsModel>> get() = _userCreateResponseLiveData



     suspend fun userCreate(getRfidTagNoFromClientModel: GetRfidTagNoFromClientModel){
        _userCreateResponseLiveData.postValue(NetworkResult.Loading())
        val response = apies.bookDetails(getRfidTagNoFromClientModel)
        handleUserCreate(response)
    }


    private fun handleUserCreate(response: Response<BookDetailsModel>){
        try {
            if (response.isSuccessful && response.body() !=null){
                _userCreateResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
            }
            else if (response.errorBody()!=null){
                val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                _userCreateResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("Something Went Wrong")))
            }
            else {
                _userCreateResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
            }

        }
        catch (e:Exception){


        }
    }





}