package com.example.lms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.example.lms.BookDetails.model.GetRfidTagNoFromClientModel
import com.example.lms.databinding.ActivityBookDetailsBinding
import com.example.lms.getrfid.PassData
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean


class ServerClass(val passData: PassData,val tcpConnection:PassData) : Service(),Runnable{
    private val thread: Thread = Thread(this)
    lateinit var serverSocket: ServerSocket
    lateinit var binding: ActivityBookDetailsBinding
     var inputStream: InputStream?=null
     var  outputStream: OutputStream?=null
     var socket: Socket?=null
     //var passData: PassData?=null
    private val working = AtomicBoolean(true)
    val Location_Channel_ID = "notification location id"
    val CHANNEL_ID = "ForegroundServiceChannel"
    lateinit var service: Service
    lateinit var status:String

    init {
        thread.priority = Thread.NORM_PRIORITY
        thread.start()
    }



    override fun run() {
        try {
            serverSocket = ServerSocket(10001)
            socket = serverSocket.accept()
            inputStream =socket?.getInputStream()
            outputStream = socket?.getOutputStream()
             status = String()

        }catch (ex: IOException){
            ex.printStackTrace()
        }

        try{
            if (socket?.isConnected!!){
                Log.d("TrueSocket", socket?.isConnected.toString())
                tcpConnection.tcpConnection(true)
            }
            else{
                tcpConnection.tcpConnection(false)
                Log.d("falseSocket", socket?.isConnected.toString()) }
        }catch (ex: Exception){
            ex.printStackTrace()
        }






        val executors = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executors.execute(Runnable{
            kotlin.run {

                val buffer = ByteArray(1024)
                var byte:Int
                while (true){
                    try {
                        byte =  inputStream!!.read(buffer)

                        if(byte > 0){
                            val finalByte = byte
                            Log.d("byteArray", buffer.toString())
                            handler.post(Runnable{
                                kotlin.run {
                                    val tmpMeassage = String(buffer,0,finalByte)
                                     val charset = Charsets.UTF_8
                                     var str = tmpMeassage.toByteArray(charset).toString(Charsets.ISO_8859_1)
                                       Log.d("v1",str)
                                       Log.d("v2",buffer.joinToString(","){ "$it" })
                                       Log.d("v3kByteToHex", bytesToHex(buffer).toUpperCase())
                                       val RfidTagNot = bytesToHex(buffer).toUpperCase().replace("\u0000", "")
                                    Log.d("rfid",buffer.toHex2())

                                    Log.d("rifdByteArray",bytesToHex(byte.to2ByteArray()))

                                     val temp = bytesToHex(buffer).toUpperCase()
                                    Log.d("tkemrkgvmbr",temp)
                                    var str1 = temp
                                    //val strArray =  ArrayList<GetRfidTagNoFromClientModel>()
                                    val strArray =  ArrayList<GetRfidTagNoFromClientModel>()

                                    try{
                                        while (str1.length>=32) {
                                            val startIndex = str1.indexOf("1100EE00", 0)
                                            if(startIndex>=0) {
                                                strArray.add(GetRfidTagNoFromClientModel(str1.substring(startIndex + 8, 32)))
                                                str1 = str1.removeRange(startIndex, 36)
                                            } else{
                                                break

                                            }

                                        }
                                    } catch (e:Exception){
                                        println(e)
                                    }
                                   val strRfid =  strArray.distinctBy { it.RFIDNo

                                    }
                                    for(i in strRfid )
                                    passData.passRfidTag(i.RFIDNo)
                                    //passData.passRfidTag(bytesToHex(buffer).toUpperCase())

                                    Log.d("Server class", (bytesToHex(buffer).toUpperCase()))
//                                    try{
//                                        if (socket?.isConnected!!){
//                                            Log.d("TrueSocket", socket?.isConnected.toString())
//                                            tcpConnection.tcpConnection(true)
//                                        }
//                                        else{
//                                            tcpConnection.tcpConnection(false)
//                                            Log.d("falseSocket", socket?.isConnected.toString()) }
//                                    }catch (ex: Exception){
//                                        ex.printStackTrace()
//                                    }
                                }
                            })
                        }
                    }catch (ex:Exception){
                        ex.printStackTrace()
                    }
                }
            }
        })
    }







    fun write(byteArray: String){
        try {
            Log.i("Server write","$byteArray sending")
            outputStream?.writer()
        }catch (ex:Exception ){
            ex.printStackTrace()
        }
    }

     fun bytesToHex(byte:ByteArray):String {
        val  builder:StringBuilder =  StringBuilder()
        for(i in byte) {
            builder.append(String.format("%02x", i))
        }
        return builder.toString()
    }




    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    override fun onDestroy() {
        working.set(false)
    }

    fun ByteArray.toHex2(): String = asUByteArray().joinToString("") { it.toString(radix = 16).padStart(2, '0') }

    fun Int.to2ByteArray() : ByteArray = byteArrayOf(toByte(), shr(8).toByte())

    @Override
    override fun onCreate() {
        super.onCreate()
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this,Location_Channel_ID)
            .setOngoing(false)
            .setSmallIcon(R.drawable.ic_launcher_background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                Location_Channel_ID,
                Location_Channel_ID, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.description = Location_Channel_ID
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
            startForeground(1, builder.build())
        }
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        val intent =  intent?.getStringExtra("Key")
        musicNotificationChannel()
        val notificationIntent = Intent(this,MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0)
        val notification = NotificationCompat.Builder(this,CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("service Stated")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent).build()
        startForeground(1,notification)


    }







    fun musicNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)

        }


    }











}