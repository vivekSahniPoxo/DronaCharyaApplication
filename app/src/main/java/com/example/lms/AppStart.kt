package com.example.lms

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import com.csnprintersdk.csnio.CSNPOS
import com.csnprintersdk.csnio.CSNUSBPrinting
import com.csnprintersdk.csnio.csnbase.CSNIOCallBack
import com.example.lms.BookDetails.ConnectUSBActivity
import com.example.lms.BookDetails.room_view_model.RoomDbViewModel
import com.example.lms.getrfid.PassData
import com.example.lms.utils.Cons
import com.example.lms.utils.TaskOpen
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.experimental.and


class AppStart :  AppCompatActivity(), View.OnClickListener,CSNIOCallBack {
    private var linearlayoutdevices: LinearLayout? = null
    lateinit var clearButton:MaterialButton
    private var radio58: RadioButton? = null
    private var radio80: RadioButton? = null
    private var radioPrintCount1: RadioButton? = null
    private var radioPrintCount10: RadioButton? = null
    private var radioPrintCount100: RadioButton? = null
    private var radioPrintCount1000: RadioButton? = null
    private var radioPrintContentS: RadioButton? = null
    private var radioPrintContentM: RadioButton? = null
    private var radioPrintContentL: RadioButton? = null
    private var chkCutter: CheckBox? = null
    private var chkDrawer: CheckBox? = null
    private var chkBeeper: CheckBox? = null
    private var chkPictureCompress: CheckBox? = null
    private var chkAutoPrint: CheckBox? = null
    private val nPrintCount1 = 0
    var mPos = CSNPOS()
    private val roomDbViewModel: RoomDbViewModel by viewModels()
    lateinit var dialog:Dialog
    var mActivity: AppStart? = null
    var es: ExecutorService = Executors.newScheduledThreadPool(30)

//    lateinit var socket: Socket
    lateinit var next:Button
    var mUsb = CSNUSBPrinting()

    private var pressedTime: Long = 0


     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.start_private)

         next = findViewById(R.id.btn_next)

         clearButton = findViewById(R.id.btn_clear)

         linearlayoutdevices = findViewById<View>(R.id.linearlayoutdevices) as LinearLayout

         //hideSystemUI()

//        roomDbViewModel.readAllBookDetails.observe(this, Observer {
//            it.forEach {
//                Log.d("bookDetails",it.Title)
//                Log.d("bookDetails",it.AccessNo)
//            }
//
//        })



         clearButton.setOnClickListener {
             alertBox()
         }





         next.setOnClickListener {
             val intent = Intent(this@AppStart, ConnectUSBActivity::class.java)
             startActivity(intent)
         }

//          socket = Socket()
//
//           socket.close()


         mPos.Set(mUsb)
        // mUsb.SetCallBack(this)
         probe()
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {

         } else {
             finish()
         }





        /* 启动WIFI */
//        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
//        when (wifiManager.wifiState) {
//            WifiManager.WIFI_STATE_DISABLED -> wifiManager.isWifiEnabled =
//                true
//            else -> {}
//        }

        roomDbViewModel.readAllData.observe(this , Observer {
            if (it.size<=50){

            } else{

                if (it.size>=55) {
                    alertBox()
                }

            }
//                    if (it[0].rfidTagNo!=binding.tvRfid.text){




        })
        radio58 = findViewById<View>(R.id.radioButtonTicket58) as RadioButton
        radio80 = findViewById<View>(R.id.radioButtonTicket80) as RadioButton
        radioPrintCount1 = findViewById<View>(R.id.radioButtonPrintCount1) as RadioButton
        radioPrintCount10 = findViewById<View>(R.id.radioButtonPrintCount10) as RadioButton
        radioPrintCount100 = findViewById<View>(R.id.radioButtonPrintCount100) as RadioButton
        radioPrintCount1000 = findViewById<View>(R.id.radioButtonPrintCount1000) as RadioButton
        radioPrintContentS = findViewById<View>(R.id.radioButtonPrintContentS) as RadioButton
        radioPrintContentM = findViewById<View>(R.id.radioButtonPrintContentM) as RadioButton
        radioPrintContentL = findViewById<View>(R.id.radioButtonPrintContentL) as RadioButton
        chkCutter = findViewById<View>(R.id.checkBoxCutter) as CheckBox
        chkDrawer = findViewById<View>(R.id.checkBoxDrawer) as CheckBox
        chkBeeper = findViewById<View>(R.id.checkBoxBeeper) as CheckBox
        chkPictureCompress = findViewById<View>(R.id.checkBoxPictureCompress) as CheckBox
        chkAutoPrint = findViewById<View>(R.id.checkBoxAutoPrint) as CheckBox

//		findViewById(R.id.btnTestBT).setOnClickListener(this);
//		findViewById(R.id.btnTestBLE).setOnClickListener(this);
        findViewById<View>(R.id.btnTestUSB).setOnClickListener(this)
        //		findViewById(R.id.btnTestNET).setOnClickListener(this);
//		findViewById(R.id.btnTestCOM).setOnClickListener(this);
    }

    override fun onClick(v: View) {
        // TODO Auto-generated method stub
        if (radio58!!.isChecked) nPrintWidth = 384 else if (radio80!!.isChecked) nPrintWidth = 576
        if (radioPrintCount1!!.isChecked) nPrintCount =
            1 else if (radioPrintCount10!!.isChecked) nPrintCount =
            10 else if (radioPrintCount100!!.isChecked) nPrintCount =
            100 else if (radioPrintCount1000!!.isChecked) nPrintCount = 1000
        if (radioPrintContentS!!.isChecked) nPrintContent =
            1 else if (radioPrintContentM!!.isChecked) nPrintContent =
            2 else if (radioPrintContentL!!.isChecked) nPrintContent = 3
        bCutter = chkCutter!!.isChecked
        bDrawer = chkDrawer!!.isChecked
        bBeeper = chkBeeper!!.isChecked
        nCompressMethod = if (chkPictureCompress!!.isChecked) 1 else 0
        bAutoPrint = chkAutoPrint!!.isChecked
        when (v.id) {
            R.id.btnTestUSB -> {
                val intent = Intent(this@AppStart, ConnectUSBActivity::class.java)
                startActivity(intent)
            }
        }
    }



    companion object {
        var nPrintWidth = 384
        var bCutter = false
        var bDrawer = false
        var bBeeper = true
        var nPrintCount = 1
        var nCompressMethod = 0
        var bAutoPrint = false
        var nPrintContent = 0
    }


    private fun alertBox() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_alert)
        dialog.setCancelable(false)
        dialog.show()

        val stay_diag: MaterialButton? = dialog.findViewById(R.id.stay_diag)
        stay_diag?.setOnClickListener {
            dialog.dismiss()
        }
        val logOut: MaterialButton? = dialog.findViewById(R.id.logout_diag)
        logOut?.setOnClickListener {
            dialog.dismiss()
            resetForm()
        }
    }


    private fun resetForm(){
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.rest_form)
        dialog.setCancelable(false)
        dialog.show()

        val cancel:MaterialButton = dialog.findViewById(R.id.btn_cancel)
        cancel.setOnClickListener {
            dialog.dismiss()
        }

        val reset: MaterialButton? = dialog.findViewById(R.id.btn_reset)
        val etRestId = dialog.findViewById<TextInputEditText>(R.id.et_reset_id)
        reset?.setOnClickListener {
            if (etRestId.text?.isNotEmpty() == true) {
                if (etRestId.text.toString()=="@#Hisar23") {
                    dialog.dismiss()
                    deleteAllRfid()
                } else{
                    etRestId.error = "ResetId is wrong"
                }
            } else{
                etRestId.error = "Please Provide reset id"
            }
        }
    }
//
//    fun deleteAllRfid(){
//        dialog = Dialog(this)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.setContentView(R.layout.rest_alert)
//        dialog.setCancelable(false)
//        dialog.show()
//
//        val cancel:MaterialButton = dialog.findViewById(R.id.btn_cancel)
//        cancel.setOnClickListener {
//            dialog.dismiss()
//        }
//
//        val deleteEverything: MaterialButton = dialog.findViewById(R.id.btn_ok)
//        deleteEverything.setOnClickListener {
//            es.submit(TaskPrint(mPos))
//            roomDbViewModel.deleteAllRfid()
//            roomDbViewModel.deleteAllBookDetails()
//            Toast.makeText(this, "Successfully removed everything", Toast.LENGTH_SHORT).show()
//
//        }
//
//
//    }

    private fun deleteAllRfid() {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton("Yes") { _, _ ->
            es.submit(TaskPrint(mPos))
            roomDbViewModel.deleteAllRfid()
            roomDbViewModel.deleteAllBookDetails()
            Toast.makeText(this, "Successfully removed everything", Toast.LENGTH_SHORT).show()

        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete everything?")
        builder.setMessage("Are you sure you want to delete everything?")
        builder.setCancelable(false)
        builder.create().show()
    }

    inner class TaskPrint(pos: CSNPOS?) : Runnable {
        var pos: CSNPOS? = null

        init {
            this.pos = pos
        }

        override fun run() {
            val bPrintResult: Int? = pos?.let {
                PrintTicket(
                    applicationContext,
                    it,
                    AppStart.nPrintWidth,
                    AppStart.bCutter,
                    AppStart.bDrawer,
                    AppStart.bBeeper,
                    AppStart.nPrintCount,
                    AppStart.nPrintContent,
                    AppStart.nCompressMethod
                )
            }
            //val bIsOpened = pos!!.GetIO().IsOpened()
          pos!!.GetIO().IsOpened()
            mActivity!!.runOnUiThread {
                if (bPrintResult != null) {
                    Toast.makeText(applicationContext, if (bPrintResult >= 0) resources.getString(R.string.printsuccess) + " " + ResultCodeToString(bPrintResult)
                    else
                        resources.getString(R.string.printfailed) + " " + ResultCodeToString(bPrintResult), Toast.LENGTH_SHORT).show()

//                    roomDbViewModel.deleteAllRfid()
//                    roomDbViewModel.deleteAllBookDetails()


                }
               // mActivity!!.btnPrint!!.isEnabled = bIsOpened
            }
        }
    }



    fun ResultCodeToString(code: Int): String {
        return when (code) {
            3 -> "There is an uncollected receipt at the paper exit, please take the receipt in time"
            2 -> "The paper is almost exhausted and there are uncollected receipts at the paper outlet, please pay attention to replace the paper roll and take away the receipts in time"
            1 -> "The paper is almost exhausted, please pay attention to replace the paper roll"
            0 -> " "
            -1 -> "The receipt is not printed, please check for paper jams"
            -2 -> "The cutter is abnormal, please troubleshoot manually"
            -3 -> "The print head is overheated, please wait for the printer to cool down"
            -4 -> "printer offline"
            -5 -> "The printer is out of paper"
            -6 -> "cover open"
            -7 -> "Real-time status query failed"
            -8 -> "Query status failed, please check whether the communication port is connected normally"
            -9 -> "Out of paper during printing, please check the integrity of the document"
            -10 -> "The upper cover is opened during printing, please print again"
            -11 -> "The connection is interrupted, please confirm whether the printer is connected"
            -12 -> "Please take away the printed receipt before printing it!"
            -13 -> "unknown mistake"
            else -> "unknown mistake"
        }
    }




    fun PrintTicket(
        ctx: Context,
        pos: CSNPOS,
        nPrintWidth: Int,
        bCutter: Boolean,
        bDrawer: Boolean,
        bBeeper: Boolean,
        nCount: Int,
        nPrintContent: Int,
        nCompressMethod: Int
    ): Int {
        var bPrintResult = 0
        val status = ByteArray(1)
        if (pos.POS_RTQueryStatus(status, 3, 1000, 2)) {
            if ((status[0] and 0x08) == 0x08.toByte()) //Judging whether the cutter is abnormal
                return -2.also { bPrintResult = it }
            if ((status[0] and 0x40) == 0x40.toByte()) //Determine whether the print head is within the normal range
                return -3.also { bPrintResult = it }
            if (pos.POS_RTQueryStatus(status, 2, 1000, 2)) {
                if ((status[0] and 0x04) == 0x04.toByte()) //Judging whether the lid is closed normally
                    return -6.also { bPrintResult = it }
                if ((status[0] and 0x20) == 0x20.toByte()) //Judging whether there is no paper
                    return -5.also { bPrintResult = it } else {


                    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                    val currentDate = sdf.format(Date())
                    for (i in 0 until nCount) {
                        if (!pos.GetIO().IsOpened()) break
                        if (nPrintContent >= 1) {
                            if (nPrintWidth == Cons.nPrintWidth) {
                                pos.POS_Reset()
                                //pos.POS_FeedLine()
                                pos.POS_TextOut("   Dayanand College\r\n", 3, 24, 1, 1, 0, 0)
                                //pos.POS_TextOut("\r\n", 3, 24, 0, 0, 0, 0)
                                pos.POS_TextOut("     Hisar,Haryana\r\n", 0, 1, 1, 0, 0, 0)
                                pos.POS_TextOut("\r\n", 3, 18, 0, 0, 0, 0)
                                pos.POS_TextOut("     Automated Library Dropbox\r\n", 3, 80, 0, 0, 0, 0)
                                pos.POS_TextOut("       $currentDate\r\n", 3, 80, 0, 0, 0, 0)
                                pos.POS_TextOut("\r\n", 3, 17, 0, 0, 0, 0)
//                               pos.POS_TextOut("${title.toString().replace("[", "").replace("]", "").split(",").toTypedArray()}\r\n", 3, 0, 0, 0, 0, 0)


                                roomDbViewModel.readAllBookDetails.observe(this , Observer {
                                    it.forEach {
                                    pos.POS_TextOut("   ${it.Title}\r\n", 3, 0, 0, 0, 0, 0)
                                        pos.POS_TextOut("   ${it.AccessNo}\r\n", 3, 0, 0, 0, 0, 0)
                                    }
                                    pos.POS_TextOut("   Total Rfid No: ${it.size}\r\n", 3, 0, 0, 0, 0, 0)
                                pos.POS_TextOut("             By Poxo Rfid Automation", 0, 20, 0, 0, 0, 0)
                                pos.POS_FeedLine()
                                pos.POS_FeedLine()
                                })

                                pos.POS_SetCharSetAndCodePage(0, 0)

                                pos.POS_TextOut("          \r\n", 3, 0, 0, 0, 0, 0)
                                pos.POS_TextOut("     \r\n", 3, 0, 0, 0, 0, 0)



                                pos.POS_FeedLine()
                                pos.POS_FeedLine()

                            }

                            if (nPrintContent == 1 && nCount > 1) {
                                pos.POS_HalfCutPaper()
                                try {
                                    Thread.currentThread()
                                    Thread.sleep(4000)
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }
                            }
                        }

                    }

                }
                if (bBeeper) pos.POS_Beep(1, 5)
                if (bCutter && nCount == 1) pos.POS_FullCutPaper()
                if (bDrawer) pos.POS_KickDrawer(0, 100)
                if (nCount == 1) {
                    try {
                        Thread.currentThread()
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            return (-8).also {
                bPrintResult = it //查询失败
            }
        }
        return 0.also { bPrintResult = it }
    }


    @SuppressLint("RtlHardcoded")
    private fun probe() {
        linearlayoutdevices!!.removeAllViews()
        val mUsbManager = getSystemService(USB_SERVICE) as UsbManager
        val deviceList = mUsbManager.deviceList
        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
        if (deviceList.size > 0) {
            // Initialize the layout of the selection dialog, and add buttons and events
            while (deviceIterator.hasNext()) { // Here is if not while, indicating that I only want to support one device
                val device = deviceIterator.next()
                //Toast.makeText( this, device.toString(), Toast.LENGTH_SHORT).show();
                val btDevice = Button(linearlayoutdevices!!.context)
                btDevice.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                btDevice.gravity = (Gravity.CENTER_VERTICAL or Gravity.LEFT)
                btDevice.text = String.format(" VID:%04X PID:%04X", device.vendorId, device.productId)
                //btDevice.setOnClickListener {

                val mPermissionIntent = PendingIntent
                    .getBroadcast(this@AppStart, 0, Intent(this@AppStart.applicationInfo.packageName), 0)
                if (!mUsbManager.hasPermission(device)){
                    mUsbManager.requestPermission(device, mPermissionIntent)
                    Toast.makeText(applicationContext, "permission denied", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "Connecting...", Toast.LENGTH_SHORT).show()
                    linearlayoutdevices!!.isEnabled = false
                    for (i in 0 until linearlayoutdevices!!.childCount) {
                        val btn = linearlayoutdevices!!.getChildAt(i) as Button
                        btn.isEnabled = false
                    }
                    // btnDisconnect!!.isEnabled = false
                   // btnPrint!!.isEnabled = false
                    es.submit(TaskOpen(mUsb, mUsbManager, device, this))

                }
                //}
                linearlayoutdevices!!.addView(btDevice)
            }
       }
    }

     override fun OnOpen() {
         runOnUiThread {
             Toast.makeText(mActivity, "Connected", Toast.LENGTH_SHORT).show()
         }
     }

     override fun OnOpenFailed() {
         this.runOnUiThread {
             Toast.makeText(mActivity, "Failed", Toast.LENGTH_SHORT).show()
         }
     }

     override fun OnClose() {
         this.runOnUiThread {
             probe()
         }
     }

    override fun onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()

        } else {
            Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        pressedTime = System.currentTimeMillis()
    }




    private fun hideSystemUI() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        window.decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }






}