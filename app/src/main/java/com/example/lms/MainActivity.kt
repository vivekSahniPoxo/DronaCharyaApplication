package com.example.lms

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

import com.example.lms.databinding.ActivityMainBinding
import com.example.lms.utils.SharePreference
import dagger.hilt.android.AndroidEntryPoint

import java.net.Socket
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //lateinit var serverClass: ServerClass
    lateinit var sharePref:SharePreference
    lateinit var binding: ActivityMainBinding
    lateinit var socket: Socket

    var str1 = ""
    val strArray = arrayListOf<String>("50")
    var index2 = 0

    lateinit var cache:MutableSet<String>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.root)



        socket = Socket()

        cache = mutableSetOf()

        //serverClass = ServerClass()

        sharePref = SharePreference(this)

//        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val ipAddress: String = formatIpAddress(wifiManager.connectionInfo.ipAddress)
//        binding.tvIp.text = "Your Device IP Address: $ipAddress"

//        binding.btnStart.setOnClickListener {
//            val intent = Intent(this@MainActivity, BookDetailsActivity::class.java)
////            val bundle = Bundle()
////            bundle.putString("key","true")
////            intent.putExtras(bundle)
//            startActivity(intent)
//        }






    }

    private fun getRealtimeDataIntegrated( temp:String,  port:String,  IP:String) = try {
        do
        {   val startIndex = str1.indexOf("1100EE00",0)
            if (startIndex < 0) {
                break
            }
            strArray[index2] = str1.substring(startIndex + 8, 24)
            str1 = str1.removeRange(startIndex, 28);
            ++index2
        }
        while (str1.length > 28)



        for (index3 in 0 until index2-1) {
            val str2 = strArray[index3]
            if (!cache.contains(str2)) {
                cache.add(str2)
                cache.add(Calendar.getInstance().time.toString())
                sharePref.saveList(cache)
                Handler(Looper.getMainLooper()).postDelayed({
                    kotlin.run {
                        if (cache.isNotEmpty()) {
                            sharePref.clearData()
                        }

                    }

                },20000)
                // AbsoluteExpiration = DateTime.Now.AddSeconds(200)
            }

        }
    }catch (e:Exception){

    }







    @SuppressLint("SetTextI18n")
    private fun getRealtiemeDataIntegrated(temp:String) {
        var str1 = temp
        val strArray =  arrayOf("50")
        var index2 = 0

        try
        {
            do {
                val startIndex = str1.indexOf("1100EE00", 0)
                if (startIndex < 0) {
                    break
                }
                strArray[index2] = str1.substring(startIndex + 8, 24)
                //Console.WriteLine(strArray[index2].ToString());
                binding.message.text = strArray[index2]
                    str1 = str1.removeRange(startIndex, 28)
                ++index2



            }
            while (str1.length > 28)

            for (index3 in 0 until index2-1) {
                val str2 = strArray[index3]
                if (!cache.contains(str2)) {
                    cache.add(str2)

                    cache.add(Calendar.getInstance().time.toString())
                    sharePref.saveList(cache)
                    Handler(Looper.getMainLooper()).postDelayed({
                        kotlin.run {
                            if (cache.isNotEmpty()) {
                                sharePref.clearData()
                            }

                        }

                    },20000)
                    // AbsoluteExpiration = DateTime.Now.AddSeconds(200)
                }

            }

        }catch (e:Exception){

        }

    }

//    override fun passRfidTag(item: String) {
//        getRealtiemeDataIntegrated(item)
//    }




//        try {
//            do
//            {   val startIndex = str1.indexOf("1100EE00",0)
//                if (startIndex < 0) {
//                    break
//                }
//                strArray[index2] = str1.substring(startIndex + 8, 24)
//                this.str1 = str1.removeRange(startIndex, 28)
//                ++index2
////                binding.message.text = str1
////                Log.d("str1",str1)
//            }
//            while (str1.length > 28)
//
//
//
//            for (index3 in 0 until index2-1) {
//                val str2 = strArray[index3]
//                binding.message.text = str2
//
//                if (!cache.contains(str2)) {
//                    cache.add(str2)
//                    cache.add(Calendar.getInstance().time.toString())
//                    sharePref.saveList(cache)
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        kotlin.run {
//                            if (cache.isNotEmpty()) {
//                                sharePref.clearData()
//                            }
//
//                        }
//
//                    },20000)
//                    // AbsoluteExpiration = DateTime.Now.AddSeconds(200)
//                }
//
//            }
//        }catch (e:Exception){
//
//        }

}