import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

//package com.example.lms.utils
//
//import android.os.AsyncTask
//
//class CheckSocket() : AsyncTask<Void, Void, String>() {
//    override fun doInBackground(vararg params: Void?): String? {
//        if ()
//
//    }
//
//    override fun onPreExecute() {
//        super.onPreExecute()
//        // ...
//    }
//
//    override fun onPostExecute(result: String?) {
//        super.onPostExecute(result)
//        // ...
//    }
//}


fun <T> MutableLiveData<T>.observeConsuming(viewLifecycleOwner: LifecycleOwner, function: (T) -> Unit) {
    observe(viewLifecycleOwner, Observer<T> {
        function(it ?: return@Observer)
        value = null
    })
}