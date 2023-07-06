//package com.example.lms.BookDetails
//
//import android.annotation.SuppressLint
//import android.app.AlertDialog
//import android.app.Dialog
//import android.app.PendingIntent
//import android.app.ProgressDialog
//import android.content.Context
//import android.content.Intent
//import android.graphics.*
//import android.graphics.drawable.ColorDrawable
//import android.hardware.usb.UsbDevice
//import android.hardware.usb.UsbManager
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.text.Editable
//import android.text.TextWatcher
//import android.text.method.ScrollingMovementMethod
//import android.util.Log
//import android.view.Gravity
//import android.view.ViewGroup
//import android.view.Window
//import android.view.WindowManager
//import android.widget.Button
//import android.widget.LinearLayout
//import android.widget.Toast
//import androidx.activity.viewModels
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.core.view.isVisible
//import androidx.lifecycle.Observer
//import com.csnprintersdk.csnio.CSNPOS
//import com.csnprintersdk.csnio.CSNUSBPrinting
//import com.csnprintersdk.csnio.csnbase.CSNIOCallBack
//import com.example.lms.BookDetails.model.GetRfidTagNoFromClientModel
//import com.example.lms.BookDetails.model.RfidNo
//import com.example.lms.BookDetails.repository.viewmodel.BookDetailsViewModel
//import com.example.lms.BookDetails.room_view_model.RoomDbViewModel
//import com.example.lms.R
//
//import com.example.lms.data.Rfid
//import com.example.lms.databinding.ActivityBookDetailsBinding
//import com.example.lms.getrfid.PassData
//import com.example.lms.prints.Prints
//import com.example.lms.prints.Prints.PrintTicket
//import com.example.lms.utils.Cons
//import com.example.lms.utils.NetworkResult
//import com.example.lms.utils.SharePreference
//import com.example.lms.utils.TaskOpen
//import com.google.android.material.button.MaterialButton
//import com.google.android.material.textfield.TextInputEditText
//import dagger.hilt.android.AndroidEntryPoint
//import java.io.IOException
//import java.util.*
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//import kotlin.experimental.and
//
//
//@AndroidEntryPoint
//class BookDetailsActivity : AppCompatActivity(),PassData, CSNIOCallBack {
//    lateinit var binding: ActivityBookDetailsBinding
//    //lateinit var serverClass: ServerClass
//    lateinit var cache:MutableSet<String>
//    lateinit var sharePref:SharePreference
//    lateinit var progressDialog: ProgressDialog
//    lateinit var mUsb:CSNUSBPrinting
//    lateinit var mPos:CSNPOS
//    lateinit var dialog:Dialog
//    lateinit var rfidTags:String
//    var es: ExecutorService = Executors.newScheduledThreadPool(30)
//    private val bookDetailsViewModel:BookDetailsViewModel by viewModels()
//
//    private val roomDbViewModel:RoomDbViewModel by viewModels()
//    lateinit var rfid:String
//
//
//
//    private lateinit  var linearlayoutdevices: LinearLayout
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
//        window.requestFeature(Window.FEATURE_NO_TITLE)
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        setContentView(binding.root)
//
//        linearlayoutdevices = findViewById(R.id.linearlayoutdevices)
//
//        rfid = String()
//        rfidTags = String()
//        rfidTags =  binding.tvRfid.text.toString()
//
//
//
//        progressDialog = ProgressDialog(this)
//        mUsb = CSNUSBPrinting()
//        mPos = CSNPOS()
//
//        mPos.Set(mUsb)
//        mUsb.SetCallBack(this)
//
//       // serverClass = ServerClass(this)
//        cache = mutableSetOf()
//        sharePref = SharePreference(this)
//
//        binding.tvDropBox.text = "Please drop book one by one....."
//        binding.tvDropBox.movementMethod = ScrollingMovementMethod()
//
//
//
//
//        roomDbViewModel.readAllData.observe(this, Observer {
//            if (it.size==50){
//                alertBox()
//            } else{
//                //for (i in 0 until it[0].id) {
//                if (it.size<=50) {
//                    Log.d("RSize",it.size.toString())
//                    roomDbViewModel.addRfid(Rfid(0, binding.tvRfid.text.toString()))
//                }
//                //}
//            }
//        })
//
//
//
//
//        binding.btnPrint.setOnClickListener {
//           binding.btnPrint.isEnabled = false
//            es.submit(TaskPrint(mPos))
//
//
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
//            probe()
//        } else {
//            finish()
//        }
//
////        binding.btnConfirm.setOnClickListener {
////            if (binding.tvRfid.text.isNotEmpty()) {
////                bookDetailsViewModel.confirm(binding.tvRfid.text.toString())
////                bindObserverForConfirm()
////            } else{
////                Toast.makeText(this,"Please! drop book first",Toast.LENGTH_SHORT).show()
////            }
////        }
//
//
//
//
//        binding.tvRfid.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                bookDetailsViewModel.getBookInfo(GetRfidTagNoFromClientModel(binding.tvRfid.text.toString()))
//                bindObserverToGetResponse()
//            }
//
//            override fun afterTextChanged(s: Editable) {}
//        })
//
//    }
//
//
//
//    private fun bindObserverToGetResponse(){
//        bookDetailsViewModel.getBookDetailsResponseLiveData.observe(this, Observer {
//            hideProgressbar()
//            when(it){
//                is NetworkResult.Success->{
//                  // binding.mCardView.isVisible = true
//                   // binding.tvTitle.text = it.data?.title
//                    it.data?.title?.let { it1 -> Log.d("title", it1) }
//
//                }
//                is NetworkResult.Error->{
//                    Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
//                    Log.d("message",it.message.toString())
//
//                }
//                is NetworkResult.Loading->{
//                       showProgressbar()
//                }
//            }
//        })
//    }
//
//    private fun showProgressbar(){
//        progressDialog.setMessage(Cons.loaderMessage)
//        progressDialog.setCancelable(false)
//        progressDialog.show()
//    }
//
//    private fun hideProgressbar(){
//        progressDialog.hide()
//    }
//
//
//    override fun passRfidTag(item: String) {
//        getRealtimeDataIntegrated(item)
//        rfid = item
//
//    }
//
//    override fun onResume() {
//        getRealtimeDataIntegrated(rfid)
//        super.onResume()
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun getRealtimeDataIntegrated(temp:String) {
//
//        var str1 = temp
//        val strArray =  arrayOf("50")
//        var index2 = 0
//
//        try {
//
//            do {
//                val startIndex = str1.indexOf("1100EE00", 0)
//                if (startIndex < 0) {
//                    break
//                }
//
//                    strArray[index2] = str1.substring(startIndex + 8, 32)
//                    Toast.makeText(this,str1.substring(startIndex + 8, 32),Toast.LENGTH_SHORT).show()
//                    binding.tvRfid.text = strArray[index2]
//                      //rfidTags = str1.substring(startIndex + 8, 32)
//                   // binding.tvRfid.text = str1.substring(startIndex + 8, 32)
//                    Log.d("strVivek", strArray[index2])
//                    str1 = str1.removeRange(startIndex, 28)
//                    ++index2
//
//
//
//            }
//            while (str1.length > 28)
//
//            for (index3 in 0 until index2-1) {
//                val str2 = strArray[index3]
//                if (!cache.contains(str2)) {
//                    cache.add(str2)
//                    Toast.makeText(this,"str2",Toast.LENGTH_SHORT).show()
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
//
//        }catch (e:Exception){
//
//        }
//
//    }
//
//     fun POS_TextOut( pszString:String,  nLan:Int,  nOrgx:Int,  nWidthTimes:Int,nHeightTimes:Int,  nFontType:Int, nFontStyle:Int){
//
//     }
//
//
//    override fun OnOpen() {
//
//        // TODO Auto-generated method stub
//        runOnUiThread {
////            btnDisconnect.setEnabled(true)
//            binding.btnPrint.isEnabled = true
//            linearlayoutdevices.isEnabled = false
//            for (i in 0 until linearlayoutdevices.getChildCount()) {
//                val btn = linearlayoutdevices.getChildAt(i) as Button
//                btn.isEnabled = false
//            }
//            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
//            binding.linearlayoutdevices.isVisible=false
//
//        }
//        Log.d("Usb","Connected")
//    }
//
//    override fun OnOpenFailed() {
//
////        // TODO Auto-generated method stub
//        runOnUiThread {
////            btnDisconnect.setEnabled(false)
//            binding.btnPrint.setEnabled(false)
//            linearlayoutdevices.setEnabled(true)
//            for (i in 0 until linearlayoutdevices.getChildCount()) {
//                val btn = linearlayoutdevices.getChildAt(i) as Button
//                btn.isEnabled = true
//           }
//            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
//            binding.linearlayoutdevices.isVisible=true
//
//        }
//        Log.d("Usb","Disconnected")
//    }
//
//    override fun OnClose() {
//        binding.linearlayoutdevices.isVisible=false
//    }
//
//    private fun getAllRfid(){
//        bookDetailsViewModel.getBookDetailsResponseLiveData.observe(this, Observer {
//
//        })
//    }
//
//    var dwWriteIndex = 1
//
//
//
//   inner class TaskPrint(pos: CSNPOS) : Runnable {
//        var pos: CSNPOS? = null
//
//
//
//        init {
//            this.pos = pos
//        }
//
//        override fun run() {
//
//
//            val bPrintResult = pos?.let {
//                PrintTicket(applicationContext,
//                    it,Cons.nPrintWidth,Cons.bCutter,Cons.bDrawer,Cons.bBeeper,Cons.nPrintCount,Cons.nPrintContent,Cons.nCompressMethod)
//            }
//
//
//            val bIsOpened = pos?.GetIO()?.IsOpened()
//            runOnUiThread(Runnable {
//                if (bPrintResult != null) {
//                    Toast.makeText(
//                        applicationContext,
//                        if (bPrintResult >= 0) resources?.getString(R.string.printsuccess) + " " + ResultCodeToString(
//                            bPrintResult
//                        ) else resources?.getString(R.string.printfailed) + " " + ResultCodeToString(
//                            bPrintResult
//                        ),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                if (bIsOpened != null) {
//                    binding.btnPrint.isEnabled = bIsOpened
//                }
//            })
//        }
//    }
//
//
//
//
//
//    private fun probe() {
//        val mUsbManager = getSystemService(USB_SERVICE) as UsbManager
//        val deviceList = mUsbManager.deviceList
//        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
//        if (deviceList.size > 0) {
//            // Initialize the layout of the selection dialog, and add buttons and events
//            while (deviceIterator.hasNext()) { // Here is if not while, indicating that I only want to support one device
//                val device = deviceIterator.next()
//                //Toast.makeText( this, device.toString(), Toast.LENGTH_SHORT).show();
//                val btDevice = Button(linearlayoutdevices.context)
//                btDevice.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//                btDevice.gravity = (Gravity.CENTER_VERTICAL or Gravity.LEFT)
//                btDevice.text = String.format(" VID:%04X PID:%04X", device.vendorId, device.productId)
//                btDevice.setOnClickListener {
//
//                    val mPermissionIntent = PendingIntent.getBroadcast(this@BookDetailsActivity, 0, Intent(
//                                this@BookDetailsActivity.applicationInfo.packageName), 0)
//                    if (!mUsbManager.hasPermission(device)) {
//                        mUsbManager.requestPermission(device, mPermissionIntent)
//                        Toast.makeText(applicationContext, "permission denied", Toast.LENGTH_LONG).show()
//                    } else {
//                        Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show()
//                        linearlayoutdevices.isEnabled = false
//                        for (i in 0 until linearlayoutdevices.childCount) {
//                            val btn = linearlayoutdevices.getChildAt(i) as Button
//                            btn.isEnabled = false
//                        }
//
//                        binding.btnPrint.isEnabled = false
//                        es.submit(TaskOpen(mUsb, mUsbManager, device, this@BookDetailsActivity))
//
//                    }
//                }
//                linearlayoutdevices.addView(btDevice)
//            }
//        }
//    }
//
//
//
//
//
//
////    private fun GetRealtiemeDataIntegrated(string temp, string Port, string IP) {
////
////        byte[] ScanModeData = new byte[40960];
////        int nLen, NumLen;
////        string temp1 = "";
////        string RSSI = "";
////        string AntStr = "";
////        string lenstr = "";
////        string lEPCStr = "";
////        int ValidDatalength;
////        ValidDatalength = 0;
////        int xtime = System.Environment.TickCount;
////        string str1 = temp;
////        string[] strArray = new string[50];
////        int index2 = 0;
////        string fInventory_EPC_List = "";
////        try
////        {
////
////
////            do
////            {
////                int startIndex = str1.IndexOf("1100EE00", 0);
////                if (startIndex < 0)
////                {
////                    break;
////                }
////                strArray[index2] = str1.Substring(startIndex + 8, 24);
////                //Console.WriteLine(strArray[index2].ToString());
////                str1 = str1.Remove(startIndex, 28);
////                ++index2;
////            }
////            while (str1.Length > 28);
////
////            for (int index3 = 0; index3 <= index2 - 1; ++index3)
////            {
////                string str2 = strArray[index3].ToString();
////
////
////
////                if (!this.cache.Contains(str2, null))
////                {
////                    this.cache.Add(str2, DateTime.Now.ToString(), new CacheItemPolicy()
////                    {
////                        AbsoluteExpiration = DateTime.Now.AddSeconds(200)
////                    }, null);
////
////
////
////
////
////
////
////
////
////                }
////            }
////
////
////        } catch (e:Exception){
////
////        }
//
//  //  }
//
////    override fun onBackPressed() {
////        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
////        builder.setMessage("Are you sure you want to Exit ?")
////            .setCancelable(false)
////            .setPositiveButton("Yes",
////                DialogInterface.OnClickListener { dialogInterface, i -> moveTaskToBack(true) })
////            .setNegativeButton("No",
////                DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.cancel() })
////        val alertDialog: AlertDialog = builder.create()
////        alertDialog.show()
////        binding.tvRfid.text = ""
////    }
//
//    private fun bindObserverForConfirm(){
//        bookDetailsViewModel.confirmBookResponseLiveData.observe(this, Observer {
//            hideProgressbar()
//            when(it){
//                is NetworkResult.Success->{
//                    Toast.makeText(this,it.data,Toast.LENGTH_SHORT).show()
//                }
//                is NetworkResult.Error->{
//                    Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
//
//                }
//                is NetworkResult.Loading->{
//                    showProgressbar()
//                }
//            }
//        })
//    }
//
//
//    private fun alertBox() {
//        dialog = Dialog(this)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.setContentView(R.layout.dialog_alert)
//        dialog.setCancelable(false)
//        dialog.show()
//
//        val stay_diag: MaterialButton? = dialog.findViewById(R.id.stay_diag)
//        stay_diag?.setOnClickListener {
//            dialog.dismiss()
//        }
//        val logOut: MaterialButton? = dialog.findViewById(R.id.logout_diag)
//        logOut?.setOnClickListener {
//            dialog.dismiss()
//            resetForm()
//        }
//    }
//
//
//    private fun resetForm(){
//        dialog = Dialog(this)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.setContentView(R.layout.rest_form)
//        dialog.setCancelable(true)
//        dialog.show()
//
//        val reset: MaterialButton? = dialog.findViewById(R.id.btn_reset)
//        val etRestId = dialog.findViewById<TextInputEditText>(R.id.et_reset_id)
//        reset?.setOnClickListener {
//            if (etRestId.text?.isNotEmpty() == true) {
//                dialog.dismiss()
//                deleteAllRfid()
//            } else{
//                etRestId.error = "Please Provide reset id"
//            }
//        }
//    }
//
//
//
//    private fun deleteAllRfid() {
//        val builder = AlertDialog.Builder(this)
//        builder.setPositiveButton("Yes") { _, _ ->
//            roomDbViewModel.deleteAllRfid()
//            Toast.makeText(
//                this,
//                "Successfully removed everything",
//                Toast.LENGTH_SHORT).show()
//        }
//        builder.setNegativeButton("No") { _, _ -> }
//        builder.setTitle("Delete everything?")
//        builder.setMessage("Are you sure you want to delete everything?")
//        builder.create().show()
//    }
//
//
//
//    fun PrintTicket(
//        ctx: Context,
//        pos: CSNPOS,
//        nPrintWidth: Int,
//        bCutter: Boolean,
//        bDrawer: Boolean,
//        bBeeper: Boolean,
//        nCount: Int,
//        nPrintContent: Int,
//        nCompressMethod: Int
//    ): Int {
//        var bPrintResult = 0
//        val status = ByteArray(1)
//        if (pos.POS_RTQueryStatus(status, 3, 1000, 2)) {
//            if ((status[0] and 0x08) == 0x08.toByte()) //Judging whether the cutter is abnormal
//                return -2.also { bPrintResult = it }
//            if ((status[0] and 0x40) == 0x40.toByte()) //Determine whether the print head is within the normal range
//                return -3.also { bPrintResult = it }
//            if (pos.POS_RTQueryStatus(status, 2, 1000, 2)) {
//                if ((status[0] and 0x04) == 0x04.toByte()) //Judging whether the lid is closed normally
//                    return -6.also { bPrintResult = it }
//                if ((status[0] and 0x20) == 0x20.toByte()) //Judging whether there is no paper
//                    return -5.also { bPrintResult = it } else {
//                    val bm1 = getTestImage1(nPrintWidth, nPrintWidth)
//                    val bm2 = getTestImage2(nPrintWidth, nPrintWidth)
//                    val bmBlackWhite = getImageFromAssetsFile(ctx, "blackwhite.png")
//                    val bmIu = getImageFromAssetsFile(ctx, "iu.jpeg")
//                    val bmYellowmen = getImageFromAssetsFile(ctx, "yellowmen.png")
//
//                    for (i in 0 until nCount) {
//                        if (!pos.GetIO().IsOpened()) break
//                        if (nPrintContent >= 1) {
//                            if (nPrintWidth == Cons.nPrintWidth) {
//                                pos.POS_Reset()
//                                pos.POS_FeedLine()
//                                pos.POS_TextOut("Title ==> Vivek \r\n", 3, 0, 0, 0, 0, 0)
//                                pos.POS_FeedLine()
//                                pos.POS_FeedLine()
//                            } else {
//                                pos.POS_Reset()
//                                pos.POS_FeedLine()
//                                pos.POS_TextOut("Poxo Rfid Automation\r\n", 0, 96, 1, 1, 0, 0)
//                                pos.POS_FeedLine()
//
//
//                            }
//                            if (nPrintContent == 1 && nCount > 1) {
//                                pos.POS_HalfCutPaper()
//                                try {
//                                    Thread.currentThread()
//                                    Thread.sleep(4000)
//                                } catch (e: InterruptedException) {
//                                    e.printStackTrace()
//                                }
//                            }
//                        }
////                        if (nPrintContent >= 2) {
////                            pos.POS_PrintPicture(bm1, nPrintWidth, 1, nCompressMethod)
////                            pos.POS_PrintPicture(bm2, nPrintWidth, 1, nCompressMethod)
////                            if (nPrintContent == 2 && nCount > 1) {
////                                pos.POS_HalfCutPaper()
////                                try {
////                                    Thread.currentThread()
////                                    Thread.sleep(4500)
////                                } catch (e: InterruptedException) {
////                                    e.printStackTrace()
////                                }
////                            }
////                            if (nPrintContent == 2 && nCount == 1) {
////                                if (bBeeper) pos.POS_Beep(1, 5)
////                                if (bCutter) pos.POS_FullCutPaper()
////                                if (bDrawer) pos.POS_KickDrawer(0, 100)
////                            }
////                        }
//                    }
////                    if (nPrintContent >= 3) {
////                        if (bmBlackWhite != null) {
////                            pos.POS_PrintPicture(bmBlackWhite, nPrintWidth, 1, nCompressMethod)
////                        }
////                        if (bmIu != null) {
////                            pos.POS_PrintPicture(bmIu, nPrintWidth, 0, nCompressMethod)
////                        }
////                        if (bmYellowmen != null) {
////                            pos.POS_PrintPicture(bmYellowmen, nPrintWidth, 0, nCompressMethod)
////                        }
////                        if (nPrintContent == 3 && nCount > 1) {
////                            pos.POS_HalfCutPaper()
////                            try {
////                                Thread.currentThread()
////                                Thread.sleep(6000)
////                            } catch (e: InterruptedException) {
////                                e.printStackTrace()
////                            }
////                        }
////                        if (nPrintContent == 3 && nCount == 1) {
////                            if (bBeeper) pos.POS_Beep(1, 5)
////                            if (bCutter) pos.POS_FullCutPaper()
////                            if (bDrawer) pos.POS_KickDrawer(0, 100)
////                        }
////                    }
//                }
//                if (bBeeper) pos.POS_Beep(1, 5)
//                if (bCutter && nCount == 1) pos.POS_FullCutPaper()
//                if (bDrawer) pos.POS_KickDrawer(0, 100)
//                if (nCount == 1) {
//                    try {
//                        Thread.currentThread()
//                        Thread.sleep(500)
//                    } catch (e: InterruptedException) {
//                        e.printStackTrace()
//                    }
//                }
//            }
//        } else {
//            return (-8).also {
//                bPrintResult = it //查询失败
//            }
//        }
//        return 0.also { bPrintResult = it }
//    }
//
//    fun ResultCodeToString(code: Int): String {
//        return when (code) {
//            3 -> "There is an uncollected receipt at the paper exit, please take the receipt in time"
//            2 -> "The paper is almost exhausted and there are uncollected receipts at the paper outlet, please pay attention to replace the paper roll and take away the receipts in time"
//            1 -> "The paper is almost exhausted, please pay attention to replace the paper roll"
//            0 -> " "
//            -1 -> "The receipt is not printed, please check for paper jams"
//            -2 -> "The cutter is abnormal, please troubleshoot manually"
//            -3 -> "The print head is overheated, please wait for the printer to cool down"
//            -4 -> "printer offline"
//            -5 -> "The printer is out of paper"
//            -6 -> "cover open"
//            -7 -> "Real-time status query failed"
//            -8 -> "Query status failed, please check whether the communication port is connected normally"
//            -9 -> "Out of paper during printing, please check the integrity of the document"
//            -10 -> "The upper cover is opened during printing, please print again"
//            -11 -> "The connection is interrupted, please confirm whether the printer is connected"
//            -12 -> "Please take away the printed receipt before printing it!"
//            -13 -> "unknown mistake"
//            else -> "unknown mistake"
//        }
//    }
//
//    /**
//     * 从Assets中读取图片
//     */
//    fun getImageFromAssetsFile(ctx: Context, fileName: String?): Bitmap? {
//        var image: Bitmap? = null
//        val am = ctx.resources.assets
//        try {
//            val `is` = am.open(fileName!!)
//            image = BitmapFactory.decodeStream(`is`)
//            `is`.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return image
//    }
//
//    fun resizeImage(bitmap: Bitmap, w: Int, h: Int): Bitmap {
//        // load the origial Bitmap
//        val width = bitmap.width
//        val height = bitmap.height
//
//        // calculate the scale
//        val scaleWidth = w.toFloat() / width
//        val scaleHeight = h.toFloat() / height
//
//        // create a matrix for the manipulation
//        val matrix = Matrix()
//        // resize the Bitmap
//        matrix.postScale(scaleWidth, scaleHeight)
//        // if you want to rotate the Bitmap
//        // matrix.postRotate(45);
//
//        // recreate the new Bitmap
//
//        // make a Drawable from Bitmap to allow to set the Bitmap
//        // to the ImageView, ImageButton or what ever
//        return Bitmap.createBitmap(
//            bitmap, 0, 0, width,
//            height, matrix, true
//        )
//    }
//
//    fun getTestImage1(width: Int, height: Int): Bitmap {
//        val bitmap = Bitmap.createBitmap(width, 4, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        val paint = Paint()
//        paint.color = Color.BLACK
//        canvas.drawRect(0f, 0f, width.toFloat(), 4f, paint)
//
////		paint.setColor(Color.BLACK);
////		for(int i = 0; i < 8; ++i)
////		{
////			for(int x = i; x < width; x += 8)
////			{
////				for(int y = i; y < height; y += 8)
////				{
////					canvas.drawPoint(x, y, paint);
////				}
////			}
////		}
//        return bitmap
//    }
//
//    fun getTestImage2(width: Int, height: Int): Bitmap {
//        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        val paint = Paint()
//        paint.color = Color.WHITE
//        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
//        paint.color = Color.BLACK
//        var y = 0
//        while (y < height) {
//            var x = y % 32
//            while (x < width) {
//                canvas.drawRect(
//                    x.toFloat(),
//                    y.toFloat(),
//                    (x + 4).toFloat(),
//                    (y + 4).toFloat(),
//                    paint
//                )
//                x += 32
//            }
//            y += 4
//        }
//        return bitmap
//    }
//
//
//
//
//
//
//
//
//
//
//}