package com.example.lms.apies

import com.example.lms.BookDetails.model.BookDetailsModel
import com.example.lms.BookDetails.model.GetRfidTagNoFromClientModel
import com.example.lms.BookDetails.model.RfidNo
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface Apies {

    @POST("BooksInfo/FetchBookByRFIDNo")
    suspend fun bookDetails(@Body getRfidTagNoFromClientModel: GetRfidTagNoFromClientModel):Response<BookDetailsModel>


    @POST("Books/ManageBookTransaction")
    suspend fun confirmBook (@Body getRfidTagNoFromClientModel: String):String

     @POST("BooksInfo/FetchBookByRFIDNo")
     fun getBookDetails(@Body getRfidTagNoFromClientModel: GetRfidTagNoFromClientModel):Call<BookDetailsModel>



}