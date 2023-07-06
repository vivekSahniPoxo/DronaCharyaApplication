package com.example.lms.utils

import android.content.Context
import com.example.lms.R
import com.example.lms.model.CacheData

class SharePreference(context: Context) {


    private var sharePreference =  context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)


    companion object {
        const val Email = "email"
    }


    fun clearData(){
        val editor = sharePreference.edit()
        editor.clear()
        editor.apply()
    }

    fun saveList(value: MutableSet<String>) {
        val editor = sharePreference.edit()
        editor.putStringSet(Email, value)
        editor.apply()
    }


    fun fetchdata(): String? {
        return sharePreference.getString(Email, null)
    }




}