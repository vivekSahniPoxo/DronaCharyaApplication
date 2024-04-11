package com.example.lms.BookDetails


import android.Manifest
import android.animation.*
import android.annotation.SuppressLint
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.*
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.csnprintersdk.csnio.CSNPOS
import com.csnprintersdk.csnio.CSNUSBPrinting
import com.csnprintersdk.csnio.csnbase.CSNIOCallBack
import com.example.lms.AppStart
import com.example.lms.BookDetails.model.BookDetailsModel
import com.example.lms.BookDetails.model.GetRfidTagNoFromClientModel
import com.example.lms.BookDetails.model.RfidNo
import com.example.lms.BookDetails.repository.viewmodel.BookDetailsViewModel
import com.example.lms.BookDetails.room_view_model.RoomDbViewModel

import com.example.lms.R
import com.example.lms.ServerClass
import com.example.lms.data.Rfid
import com.example.lms.data.RfidDatabase
import com.example.lms.databinding.ActivityBookDetailsBinding
import com.example.lms.getrfid.PassData
import com.example.lms.helpers.*
import com.example.lms.tcp_clinet.TcpClientCallback
import com.example.lms.tcp_clinet.TcpClientService
import com.example.lms.utils.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.renderer.XAxisRendererHorizontalBarChart
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.rheyansh.helpers.RPermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.io.*
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import kotlin.experimental.and


@AndroidEntryPoint
class ConnectUSBActivity : AppCompatActivity(),PassData, View.OnClickListener, CSNIOCallBack, TcpClientCallback {


    private lateinit var tcpClientService: TcpClientService
    private var isServiceBound = false
    var RFIDNO = ""

    private val uiHandler = Handler(Looper.getMainLooper())

    private lateinit var rfidDatabase: RfidDatabase
    private var isGenerating = false
    val pdfModel = RPdfGeneratorModel(listOf(), "Your Header")

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TcpClientService.LocalBinder
            tcpClientService = binder.getService()
            tcpClientService.setCallback(this@ConnectUSBActivity)
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

        private var linearlayoutdevices: LinearLayout? = null
    lateinit var binding: ActivityBookDetailsBinding
    //var btnDisconnect: Button? = null
    var btnPrint: Button? = null
    var mActivity: ConnectUSBActivity? = null
    lateinit var serverClass: ServerClass
    private val bookDetailsViewModel:BookDetailsViewModel by viewModels()

   // private lateinit var bookDetailsViewModel:BookDetailsViewModel
    var es: ExecutorService = Executors.newScheduledThreadPool(30)
    var mPos = CSNPOS()
    var mUsb = CSNUSBPrinting()
    lateinit var cache:MutableSet<String>

    lateinit var progressDialog: ProgressDialog
    lateinit var bookTitle:ArrayList<String>

    lateinit var bookDetailsAdapter: BookDetailsAdapter
    lateinit var bookDetailsList:ArrayList<BookDetailsModel>
    lateinit var temp:ArrayList<BookDetailsModel>
    lateinit var passData: PassData

   lateinit  var list:ArrayList<String>
    lateinit var bookDetailsString:String

    lateinit var rfidTagNo:ArrayList<RfidNo>

    lateinit var dialog:Dialog
    private val roomDbViewModel: RoomDbViewModel by viewModels()

     var context: Context?=null



    //lateinit var rfidTemp:ArrayList<String>

    var rfidTemp:MutableList<Any> = mutableListOf()
    var avoidDublicateRfid:MutableList<Any> = mutableListOf()


    lateinit var getRfidFromStrArray:String

    var backButtonCount:Int = 0

    lateinit var roomDBRfidList:ArrayList<String>
    lateinit var tempTcpList:ArrayList<String>

    var rfidFromClient = String()



    lateinit var socket:Socket

    private val reload = MutableLiveData<Unit>()
    lateinit var roomDBList:ArrayList<String>

     var mRfidTemp:MutableList<Any> = mutableListOf()

    var roomRfidList:MutableList<Any> = mutableListOf()

    private lateinit var animatorSet: AnimatorSet
  //  private lateinit var dummyInfo: RPdfGeneratorModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(binding.root)
        //dummyInfo = dummyModel()


        lifecycleScope.launch {
            val rowCount = rfidDatabase.rfidDao().getCountOfBooks()
            binding.coloredView.setTotalRange(50)
            binding.coloredView.setDataListSize(rowCount)

        }

        tcpClientService = TcpClientService()
        createPdf(false)





        // Bind to the service
        val intent = Intent(this, TcpClientService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        startService(intent)  // Ensure that the service is started

        val isMyServiceRunning = isServiceRunning(this, TcpClientService::class.java)
        if (isMyServiceRunning) {
            // The service is running
            Log.d("Runnign","yes")

        } else {
            Log.d("Runnign","No")
        }
        getRfidFromStrArray = String()
        serverClass = ServerClass(this,this)
        progressDialog = ProgressDialog(this)

        hideSystemUI()






        binding.coloredView.setTotalRange(50)
        binding.coloredView.setDataListSize(20)

//        val handler = Handler()
//        val delay = 5000 // 5 seconds in milliseconds
//        var iteration = 1
//
//        handler.postDelayed(object : Runnable {
//            override fun run() {
//                // Update data list size based on the iteration
//                when (iteration) {
//                    1 -> {
//                        binding.coloredView.setDataListSize(20)
//                        Toast.makeText(this@ConnectUSBActivity,"BinSize is 20",Toast.LENGTH_SHORT).show()
//                    }
//                    2 -> {
//                        binding.coloredView.setDataListSize(35)
//                        Toast.makeText(this@ConnectUSBActivity,"BinSize is 35",Toast.LENGTH_SHORT).show()
//                    }
//                    3 -> {
//                        binding.coloredView.setDataListSize(40)
//                        Toast.makeText(this@ConnectUSBActivity,"BinSize is 40",Toast.LENGTH_SHORT).show()
//                    }
//                    4 -> {
//                        binding.coloredView.setDataListSize(46)
//                        Toast.makeText(this@ConnectUSBActivity,"BinSize is 46",Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                // Increment the iteration or reset if it reaches 4
//                iteration = if (iteration < 4) iteration + 1 else 1
//
//                // Call the same method after the delay
//                handler.postDelayed(this, delay.toLong())
//            }
//        }, delay.toLong())



        val animator = ObjectAnimator.ofInt(binding.coloredView, "progress", 0, 100)
        animator.duration = 5000 // 5000 milliseconds = 5 seconds
        animator.start()


        cache = mutableSetOf()


        bookDetailsString = String()
        roomDBList  = arrayListOf()
        roomDBRfidList =  arrayListOf()
        tempTcpList= arrayListOf()


        list = arrayListOf()

        socket = Socket()

//        val progressBar = findViewById<ProgressBar>(R.id.hori_progressBar)
//        progressBar.max = 50
//        var listSise =  progressBar.progress
//          listSise = 2
//
//        // Example: Animate progress from 0 to 100 over 5 seconds
//        val animator = ObjectAnimator.ofInt(progressBar, "progress", listSise, listSise)
//        animator.duration = 5000 // 5000 milliseconds = 5 seconds
//        animator.start()


        bookDetailsList = arrayListOf()
        bookDetailsAdapter = BookDetailsAdapter(bookDetailsList)
        bookDetailsAdapter.clear()
        binding.tvBookStatue.text = ""


        bookTitle = arrayListOf()

        rfidTagNo = arrayListOf()



        mActivity = this
        linearlayoutdevices = findViewById<View>(R.id.linearlayoutdevices) as LinearLayout
        // btnDisconnect = findViewById<View>(R.id.buttonDisconnect) as Button
        btnPrint = findViewById<View>(R.id.btn_print) as Button
        // btnDisconnect!!.setOnClickListener(this)
        btnPrint!!.setOnClickListener(this)
        // btnDisconnect!!.isEnabled = false
        btnPrint!!.isEnabled = false
        mPos.Set(mUsb)
        mUsb.SetCallBack(this)

        probe()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            probe()
        } else {
            finish()
        }


        rfidTagNo.clear()
        list.clear()
        //rfidTemp.clear()
        binding.tvTotalCount.text=""
        bookDetailsList.clear()
        binding.tvBookStatue.text=""
        bookDetailsAdapter.notifyItemRangeRemoved(0,bookDetailsList.size)
        bookDetailsAdapter.notifyDataSetChanged()


        binding.btnTestUSB.setOnClickListener {
//            showBookInfoUi()
//            hideHomeUi()
            val i = Intent(applicationContext, AppStart::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)

        }


        binding.btnBack.setOnClickListener {
            rfidTagNo.clear()
            list.clear()
            //rfidTemp.clear()
            binding.tvTotalCount.text=""
            binding.tvBookStatue.text = ""
            bookDetailsList.clear()

            bookDetailsAdapter.notifyDataSetChanged()

            val i = Intent(applicationContext, AppStart::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)

        }

        binding.btnTest.setOnClickListener {
//            hideBookInfoUi()
//            showHomeUi()

            rfidTagNo.clear()
            list.clear()
            //rfidTemp.clear()
            binding.tvTotalCount.text=""
            bookDetailsList.clear()

            bookDetailsAdapter.notifyDataSetChanged()

            val i = Intent(applicationContext, AppStart::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)

        }


    }










    fun generateColors(count: Int): List<String> {
        val colors = mutableListOf<String>()

        for (i in 0 until count) {
            when {
                i < 20 -> colors.add("3bcd8b")
                i in 20 until 30 -> colors.add("#FFFF00")
                i in 30 until 40 -> colors.add("FFA500")
                else -> colors.add("FF0000")
            }
        }

        return colors
    }

    private fun bindObserverToGetResponse(){
        bookDetailsViewModel.getBookDetailsResponseLiveData.observe(this) {
              binding.progressBar.isVisible = false
           //hideProgressbar()
            when (it) {
                is NetworkResult.Success -> {

//                   rfidTagNo.clear()
                    roomDbViewModel.addRfid(Rfid(0, binding.tvRfid.text.toString()))
                    rfidTemp.add(binding.tvRfid.text.toString())

                    try {
                        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        if(it.data?.body()?.title?.isNotEmpty() == true && it.data.body()?.accessNo?.isNotEmpty() == true){
                            writeToFileExternal("returnBooksLog.txt",binding.tvRfid.text.toString(),it.data?.body()?.title.toString(),it.data?.body()?.accessNo.toString())
                            binding.tvTotalCount.isVisible = true
                            if (!bookDetailsList.contains(BookDetailsModel(it.data.body()?.accessNo.toString(), it.data?.body()?.title.toString()))) {
                            bookDetailsList.add(BookDetailsModel(it.data.body()?.accessNo.toString(), it.data.body()?.title.toString()))
                            bookTitle.add(it.data.body()?.title.toString())
                            bookTitle.add(it.data.body()?.accessNo.toString())
                                roomDbViewModel.addBookDetails(com.example.lms.data.BookDetailsModel(0,it.data.body()?.title.toString(),it.data.body()?.accessNo.toString(),currentDate,RFIDNO))

                                lifecycleScope.launch {
                                    val rowCount = rfidDatabase.rfidDao().getCountOfBooks()
                                    binding.coloredView.setTotalRange(50)
                                    binding.coloredView.setDataListSize(rowCount)

                                }

                                val newTransaction = RTransaction()
                                newTransaction.Date = currentDate
                                newTransaction.AccessNo = it.data.body()?.accessNo.toString()
                                newTransaction.BookName = it.data.body()?.title.toString()
                                newTransaction.RFIDNI = RFIDNO
                                pdfModel.list.add(newTransaction)
                        } }




                        val list = bookDetailsList.distinct()

                        binding.tvTotalCount.text = "Total books count:${list.size}"
                        bookDetailsAdapter = BookDetailsAdapter(bookDetailsList)
                        //binding.bookList.adapter?.setHasStableIds(true)
                        binding.bookList.adapter = bookDetailsAdapter
                        bookDetailsAdapter.notifyDataSetChanged()
                        bookDetailsViewModel.confirm(binding.tvRfid.text.toString())
                        bindObserverForConfirm()

                    } catch (e:Exception){

                    }


                }
                is NetworkResult.Error -> {
                    Toast.makeText(this,"Book does not exist", Toast.LENGTH_SHORT).show()
                    //context?.toast("Book does not exist")
                   // context?.toast(it.message.toString())
                    Log.d("message", it.message.toString())

                }
                is NetworkResult.Loading -> {
                    //showProgressbar()
                    binding.progressBar.isVisible = true
                }
            }
        }
    }





    private fun showProgressbar(){
        progressDialog.setMessage(Cons.loaderMessage)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    private fun hideProgressbar(){
        progressDialog.hide()
    }








    override fun onClick(arg0: View) {
        when (arg0.id) {

            R.id.btn_print -> {
                btnPrint!!.isEnabled = false
                es.submit(TaskPrint(mPos))

                createPdf(true)
            }
            else -> {}
        }
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
                        .getBroadcast(this@ConnectUSBActivity, 0, Intent(this@ConnectUSBActivity.applicationInfo.packageName), 0)
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
                        btnPrint!!.isEnabled = false
                        es.submit(TaskOpen(mUsb, mUsbManager, device, this))

                    }
                //}
                linearlayoutdevices!!.addView(btDevice)
            }
        }
    }

    inner class TaskTest(
        pos: CSNPOS,
        usb: CSNUSBPrinting?,
        usbManager: UsbManager?,
        usbDevice: UsbDevice?,
        context: Context?
    ) :
        Runnable {
        var pos: CSNPOS? = null
        var usb: CSNUSBPrinting? = null
        var usbManager: UsbManager? = null
        var usbDevice: UsbDevice? = null
        var context: Context? = null

        init {
            this.pos = pos
            this.usb = usb
            this.usbManager = usbManager
            this.usbDevice = usbDevice
            this.context = context
            pos.Set(usb)
        }

        override fun run() {
            for (i in 0..999) {
                var beginTime = System.currentTimeMillis()
                if (usb!!.Open(usbManager, usbDevice, context)) {
                    var endTime = System.currentTimeMillis()
                    pos!!.POS_S_Align(0)
                    pos!!.POS_S_TextOut("""$i	Open	UsedTime:${endTime - beginTime}""", 0, 0, 0, 0, 0)
                    beginTime = System.currentTimeMillis()
                    val ticketResult = pos!!.POS_TicketSucceed(i, 30000)
                    endTime = System.currentTimeMillis()
                    pos!!.POS_S_TextOut("""$i	Ticket	UsedTime:${endTime - beginTime}	${if (ticketResult == 0) "Succeed" else "Failed"}""", 0, 0, 0, 0, 0)
                    pos!!.POS_FullCutPaper()
                    usb!!.Close()
                }
            }
        }
    }

    inner class TaskOpen(
        usb: CSNUSBPrinting?,
        usbManager: UsbManager?,
        usbDevice: UsbDevice?,
        context: Context?
    ) :
        Runnable {
        var usb: CSNUSBPrinting? = null
        var usbManager: UsbManager? = null
        var usbDevice: UsbDevice? = null
        var context: Context? = null

        init {
            this.usb = usb
            this.usbManager = usbManager
            this.usbDevice = usbDevice
            this.context = context
        }

        override fun run() {
            usb!!.Open(usbManager, usbDevice, context)
        }
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
            val bIsOpened = pos!!.GetIO().IsOpened()
            mActivity!!.runOnUiThread {
                if (bPrintResult != null) {

//                        hideBookInfoUi()
//                        showHomeUi()
                    //binding.imCropBox.isVisible=false
                    Toast.makeText(applicationContext, if (bPrintResult >= 0) resources.getString(R.string.printsuccess) + " " + ResultCodeToString(bPrintResult)
                    else
                        resources.getString(R.string.printfailed) + " " + ResultCodeToString(bPrintResult), Toast.LENGTH_SHORT).show()

                    val i = Intent(applicationContext, AppStart::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)

                }
                mActivity!!.btnPrint!!.isEnabled = bIsOpened
            }
        }
    }

    inner class TaskClose(usb: CSNUSBPrinting?) : Runnable {
        var usb: CSNUSBPrinting? = null

        init {
            this.usb = usb
        }

        override fun run() {
            // TODO Auto-generated method stub
            usb!!.Close()
        }
    }

    override fun OnOpen() {
        runOnUiThread {
           // btnDisconnect!!.isEnabled = true
            btnPrint!!.isEnabled = true
            linearlayoutdevices!!.isEnabled = false
            for (i in 0 until linearlayoutdevices!!.childCount) {
                val btn = linearlayoutdevices!!.getChildAt(i) as Button
                btn.isEnabled = false
            }
            binding.linearlayoutdevices.isVisible = false
            Toast.makeText(mActivity, "Connected", Toast.LENGTH_SHORT).show()
            binding.imCropBox.setBackgroundResource(R.drawable.drop_box)
        }
    }

    override fun OnOpenFailed() {

        this.runOnUiThread {
            //btnDisconnect!!.isEnabled = false
            btnPrint!!.isEnabled = false
            linearlayoutdevices!!.isEnabled = true
            for (i in 0 until linearlayoutdevices!!.childCount) {
                val btn = linearlayoutdevices!!.getChildAt(i) as Button
                btn.isEnabled = true
            }
            Toast.makeText(mActivity, "Failed", Toast.LENGTH_SHORT).show()
            binding.imCropBox.setBackgroundResource(R.drawable.printer_not_connected)
        }
    }

    override fun OnClose() {

        this.runOnUiThread {
           // btnDisconnect!!.isEnabled = false
            btnPrint!!.isEnabled = false
            linearlayoutdevices!!.isEnabled = true
            for (i in 0 until linearlayoutdevices!!.childCount) {
                val btn = linearlayoutdevices!!.getChildAt(i) as Button
                btn.isEnabled = true
            }
            probe()
        }
    }

    companion object {
        var dwWriteIndex = 1
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
                    val bm1 = getTestImage1(nPrintWidth, nPrintWidth)
                    val bm2 = getTestImage2(nPrintWidth, nPrintWidth)
                    val bmBlackWhite = getImageFromAssetsFile(ctx, "blackwhite.png")
                    val img = resources.getDrawable(R.drawable.logo,theme)



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

                                val title = bookTitle.distinct()
                                //val title = bookTitle.distinct()
                                for (i in title) {
                                    pos.POS_TextOut("   ${i}\r\n", 3, 0, 0, 0, 0, 0)

                                }

                                pos.POS_TextOut("   ${binding.tvTotalCount.text}\r\n", 3, 0, 0, 0, 0, 0)
                                pos.POS_TextOut("             By Poxo Rfid Automation", 0, 20, 0, 0, 0, 0)
                                pos.POS_FeedLine()
                                pos.POS_FeedLine()


                                pos.POS_SetCharSetAndCodePage(0, 0)

                                pos.POS_TextOut("         \r\n", 3, 0, 0, 0, 0, 0)
                                pos.POS_TextOut("      \r\n", 3, 0, 0, 0, 0, 0)


//                                pos.POS_TextOut("$$$$$$$$ \r\n", 3, 0, 0, 0, 0, 0)
//                                pos.POS_TextOut("$$$$$$\r\n", 3, 0, 0, 0, 0, 0)



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

    /**
     * 从Assets中读取图片
     */
    fun getImageFromAssetsFile(ctx: Context, fileName: String?): Bitmap? {
        var image: Bitmap? = null
        val am = ctx.resources.assets
        try {
            val `is` = am.open(fileName!!)
            image = BitmapFactory.decodeStream(`is`)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }



    fun getTestImage1(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, 4, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        canvas.drawRect(0f, 0f, width.toFloat(), 4f, paint)

        return bitmap
    }

    fun getTestImage2(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.color = Color.BLACK
        var y = 0
        while (y < height) {
            var x = y % 32
            while (x < width) {
                canvas.drawRect(
                    x.toFloat(),
                    y.toFloat(),
                    (x + 4).toFloat(),
                    (y + 4).toFloat(),
                    paint
                )
                x += 32
            }
            y += 4
        }
        return bitmap
    }

    override fun passRfidTag(item: String) {
        //getRealtimeDataIntegrated(item)
        if(item.length<24){

        } else if (!rfidTemp.contains(item)) {
            binding.tvRfid.text = item
            binding.searchView.text = item

            CoroutineScope(Dispatchers.Default).launch {
                val rfid = roomDbViewModel.findRfid(item)
                try {
                     if (rfid.rfidTagNo==item) {
                         if (rfidTemp.size==rfidTemp.size) {
                         }

                        } else {
                         bookDetailsViewModel.getBookInfo(GetRfidTagNoFromClientModel(item))
                         bindObserverToGetResponse()

                        }

                } catch (e:Exception){
                    Log.d("Exception",e.message.toString())
                    bookDetailsViewModel.getBookInfo(GetRfidTagNoFromClientModel(item))
                    bindObserverToGetResponse()
                    Log.d("rfrmlvfm",item)
                }
            }
        }


    }

    override fun tcpConnection(connection: Boolean) {

        runOnUiThread {
            if (connection){
                binding.imConnectivity.setBackgroundResource(R.drawable.connectivity)
                Log.d("tcpConnectionFromService", connection.toString())
                }
            else {
                binding.imConnectivity.setBackgroundResource(R.drawable.not_connected)
                Log.d("tcpConnectionFromService", connection.toString())
            }
        }
    }

    override fun onBackPressed() {
        Toast.makeText(applicationContext, "Back Button Disabled", Toast.LENGTH_SHORT).show()

    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    private fun getRealtimeDataIntegrated(temp:String) {
//        bookDetailsList.clear()
//        bookDetailsAdapter.notifyDataSetChanged()

        var str1 = temp
        val strArray =  ArrayList<GetRfidTagNoFromClientModel>()
        var index2 = 0

        try{
            while (str1.length>=32) {
                val startIndex = str1.indexOf("1100EE00", 0)
                if(startIndex>=0) {
                    //Handler(Looper.getMainLooper()).postDelayed(2000) {
                        strArray.add(GetRfidTagNoFromClientModel(str1.substring(startIndex + 8, 32)))
                        str1 = str1.removeRange(startIndex, 36)
                        Log.d("str1", str1)
                      Log.d("rfidFrom",str1)
//                    val strArrayList = strArray.distinct()
//                    for(i in strArrayList) {
//                        binding.searchView.text = i.RFIDNo
//                        roomDbViewModel.getAllRfid(i.RFIDNo)
//                        bindObserverToGetDetails()
//                    }
                    //}


                    } else{
                        break

                }

            }
        } catch (e:Exception){
            println(e)
        }


        val strArrayList = strArray.distinct()
        for (i in strArrayList) {
            rfidTagNo.add(RfidNo(i.RFIDNo))
                binding.tvRfid.text = i.RFIDNo
                binding.searchView.text = i.RFIDNo


            if (socket.isConnected) {
                Log.d("connected", socket.isConnected.toString())
                binding.imConnectivity.setBackgroundResource(R.drawable.not_connected)
            } else {
                binding.imConnectivity.setBackgroundResource(R.drawable.connectivity)
                Log.d("not_connection", socket.isConnected.toString())
            }
        }

        val rfidFromTcp= rfidTagNo.distinct()
            for (i in rfidFromTcp)
                rfidFromClient = i.rfidno

                if (rfidFromClient.length<24){

                } else {
                    Log.d("RfidFromReader",rfidFromClient)
                       // mRfidTemp.add(rfidFromClient)
                    if (!rfidTemp.contains(rfidFromTcp)) {
                        binding.tvRfid.text = rfidFromClient
                        binding.searchView.text = rfidFromClient
                        //rfidFromClient = rfidFromClient
                        // getting RFid From Local Database
//                        roomDbViewModel.getAllRfid(rfidFromClient)
//                        bindObserverToGetDetails()
                    }


                  //Handler(Looper.getMainLooper()).postDelayed(2000) {



                //}

                 }

          // }

//            if (socket.isConnected) {
//                Log.d("connected", socket.isConnected.toString())
//                binding.imConnectivity.setBackgroundResource(R.drawable.not_connected)
//            } else {
//                binding.imConnectivity.setBackgroundResource(R.drawable.connectivity)
//                Log.d("not_connection", socket.isConnected.toString())
//            }
//                rfidTagNo.clear()
//                mRfidTemp.clear()

            //}


   }




    private fun bindObserverForConfirm(){
        bookDetailsViewModel.confirmBookResponseLiveData.observe(this, androidx.lifecycle.Observer {
            //hideProgressbar()
           binding.progressBar.isVisible = false
            when(it){
                is NetworkResult.Success->{
                    binding.tvBookStatue.text = "Book Status: ${it.data}"
                    Log.d("bookStatus",it.data.toString())
//                    it.data?.let { it1 -> context?.toast(it1) }
//                    Log.d("manageBook",it.data.toString())
                }
                is NetworkResult.Error->{
                    Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()

                }
                is NetworkResult.Loading->{
                    //showProgressbar()
                    binding.progressBar.isVisible = true
                }
            }
        })
    }


    override fun onRestart() {
        super.onRestart()
        rfidTagNo.clear()
        list.clear()
        //rfidTemp.clear()
        binding.tvTotalCount.text=""
        binding.tvBookStatue.text = ""
        bookDetailsList.clear()
        bookDetailsAdapter.notifyItemRangeRemoved(0,bookDetailsList.size)
        bookDetailsAdapter.notifyDataSetChanged()
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

        val reset: MaterialButton? = dialog.findViewById(R.id.btn_reset)
        val etRestId = dialog.findViewById<TextInputEditText>(R.id.et_reset_id)
        reset?.setOnClickListener {
            if (etRestId.text?.isNotEmpty() == true) {
                if (etRestId.text.toString()=="@#Hisar23") {
                    dialog.dismiss()
                    deleteAllRfid()
                } else{
                    etRestId.error = "Ohh! ResetId is wrong"
                }
            } else{
                etRestId.error = "Please Provide reset id"
            }
        }
    }

    private fun deleteAllRfid() {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton("Yes") { _, _ ->
            roomDbViewModel.deleteAllRfid()
            roomDbViewModel.deleteAllBookDetails()
            Toast.makeText(this, "Successfully removed everything", Toast.LENGTH_SHORT).show() }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete everything?")
        builder.setMessage("Are you sure you want to delete everything?")
        builder.create().show()
    }



//    override fun onBackPressed() {
//        val i = Intent(applicationContext, AppStart::class.java)
//        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(i)
        //finish()
//        if (backButtonCount >= 1) {
//            val intent = Intent(Intent.ACTION_MAIN)
//            intent.addCategory(Intent.CATEGORY_HOME)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
//        } else {
//            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show()
//            backButtonCount++
//        }
   // }







    private fun showHomeUi(){
        binding.tvAutomatedLibrary.isVisible = true
        binding.imBookLogo.isVisible = true
        binding.btnTestUSB.isVisible = true
        binding.tvBookStatue.isVisible=true


        rfidTagNo.clear()
        list.clear()
        //rfidTemp.clear()
        binding.tvTotalCount.text=""
        bookDetailsList.clear()
        bookDetailsAdapter.notifyItemRangeRemoved(0,bookDetailsList.size)
        bookDetailsAdapter.notifyDataSetChanged()

    }

    private fun hideHomeUi(){
        binding.tvAutomatedLibrary.isVisible = false
        binding.imBookLogo.isVisible = false
        binding.btnTestUSB.isVisible = false
//        binding.tvBookStatue.isVisible=false
//        binding.imCropBox.isVisible=false
    }

    private fun showBookInfoUi(){
        binding.bookList.isVisible = true
        binding.tvBookInfo.isVisible = true
        binding.tvDropBox.isVisible = true
        binding.tvTotalCount.isVisible = true
        binding.btnPrint.isVisible = true
        binding.imClgLogo.isVisible = true
        binding.imConnectivity.isVisible = true
        binding.tvBookStatue.isVisible=true
        binding.imCropBox.isVisible=true
    }

    private fun hideBookInfoUi(){
        binding.bookList.isVisible = false
        binding.tvBookInfo.isVisible = false
        binding.tvDropBox.isVisible = false
        binding.tvTotalCount.isVisible = false
        binding.btnPrint.isVisible = false
        //binding.imClgLogo.isVisible = false
        binding.imConnectivity.isVisible = false
        binding.tvBookStatue.isVisible=false
        binding.imCropBox.isVisible=false
        binding.tvBookStatue.text=""
    }




    private fun bindObserverToGetDetails() {
        roomDbViewModel.fetchItemFromRoomDb.observe(this@ConnectUSBActivity, Observer {
            //progressDialog.hide()
            when (it) {
                is NetworkResult.Success -> {

                        val RfidFromScanner = binding.searchView.text.toString()
                         Log.d("rfidFromLocalDataBase",it.data?.rfidTagNo.toString())
                       // if (it.data?.rfidTagNo.toString() == RfidFromScanner) {
                            //mRfidTemp.clear()
//                        } else {
//                            Log.d("tvRfid", binding.tvRfid.text.toString())
//                            Log.d("roomRfidList", mRfidTemp.toString())
//                            val notMatchedRfid = binding.searchView.text.toString()
//                            bookDetailsViewModel.getBookInfo(GetRfidTagNoFromClientModel(RfidFromScanner))
//                            bindObserverToGetResponse()
//
//                        }


                    }


                is NetworkResult.Error -> {
                    Toast.makeText(this@ConnectUSBActivity, it.message, Toast.LENGTH_LONG).show()
                    Log.d("Error",it.message.toString())
                }
                is NetworkResult.Loading -> {
                    //showProgressbar()
                    Log.d("Error","Loading")
                }


            }
        })

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


    private fun countDownTimer(){

        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.timer_dialog)
        dialog.setCancelable(false)
        dialog.show()

        val btnNo: MaterialButton? = dialog.findViewById(R.id.btn_no)
        btnNo?.setOnClickListener {
            dialog.dismiss()
            val i = Intent(applicationContext, AppStart::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }

        val btnYes: MaterialButton? = dialog.findViewById(R.id.btn_yes)
        btnYes?.setOnClickListener {
            dialog.dismiss()



        }
        // time count down for 5 seconds,
        // with 1 second as countDown interval
//        object : CountDownTimer(timer.toLong(), 1000) {
//
//
//            // Callback function, fired on regular interval
//            override fun onTick(millisUntilFinished: Long) {
//                val tvTimer: MaterialTextView? = dialog.findViewById(R.id.tv_timer)
//                tvTimer?.text = " " + millisUntilFinished / 1000
//            }
//
//            // Callback function, fired
//            // when the time is up
//            override fun onFinish() {
//                val i = Intent(applicationContext, AppStart::class.java)
//                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(i)
//                dialog.dismiss()
//
//            //textView.setText("done!")
//            }
//
//        }.start()
    }

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


//    override fun onDestroy() {
//        super.onDestroy()
//
//        if (isServiceBound) {
//            tcpClientService.closeConnection() // Close the connection
//            unbindService(serviceConnection)
//            isServiceBound = false
//        }
//    }

    override fun onPause() {
        super.onPause()
        tcpClientService.closeConnection()
        tcpClientService.disconnectFromServer()
        if (isServiceBound) {
           // tcpClientService.closeConnection() // Close the connection
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    override fun onStop() {
        super.onStop()
        tcpClientService.closeConnection() // Close the connection
        tcpClientService.disconnectFromServer()
    }




//    override fun onResume() {
//        super.onResume()
//        tcpClientService.reconnect()
//    }

    // TcpClientCallback methods
    override fun onConnected(isConnected: Boolean) {
        // Handle connection established

        runOnUiThread {
                if (isConnected==true){
                    binding.imConnectivity.setBackgroundResource(R.drawable.connectivity)
                    Log.d("tcpConnectionFromService", isConnected.toString())
                }
                else if (!isConnected) {
                    binding.imConnectivity.setBackgroundResource(R.drawable.not_connected)
                    Log.d("tcpConnectionFromService", isConnected.toString())
                }
            }
    }

    override fun onDisconnected(isConnected:Boolean) {
        // Handle disconnection
        runOnUiThread {
            binding.imConnectivity.setBackgroundResource(R.drawable.not_connected)
            Log.d("tcpConnectionFromService1", isConnected.toString())
        }
    }







    override fun onDataReceived(data: String) {
        // Handle received data

       // runOnUiThread {
           // binding.txtString.text = data

            if(data.length<24){

            } else if (!rfidTemp.contains(data)) {
                binding.tvRfid.text = data
                binding.searchView.text = data
                RFIDNO = data

                CoroutineScope(Dispatchers.Default).launch {
                    val rfid = roomDbViewModel.findRfid(data)
                    try {
                        if (rfid.rfidTagNo==data) {
                            if (rfidTemp.size==rfidTemp.size) {
                                Snackbar.make(binding.root,"Book Already Return",Snackbar.LENGTH_SHORT).show()
                            }

                        } else if(rfid.rfidTagNo!=data || rfid.rfidTagNo.isNullOrEmpty()) {
                            runOnUiThread {
                                bookDetailsViewModel.getBookInfo(GetRfidTagNoFromClientModel(data))
                                bindObserverToGetResponse()
                            }

                        }

                    } catch (e:Exception){
                        Log.d("Exception",e.message.toString())
                        runOnUiThread {
                            bookDetailsViewModel.getBookInfo(GetRfidTagNoFromClientModel(data))
                            bindObserverToGetResponse()
                        }
                       // Log.d("rfrmlvfm",item)
                    }
                }
            }


            ///Toast.makeText(this,data,Toast.LENGTH_SHORT).show()
       // }
    }


    fun writeToFileExternal(fileName: String, rfid: String,bookName:String, accessCode: String) {
        try {
            val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val file = File(externalDir, fileName)

            // Use BufferedWriter for better performance
            val bufferedWriter = BufferedWriter(FileWriter(file, true))

            // No need to check file length for duplicates

            // Write the data
            bufferedWriter.write(rfid)
            bufferedWriter.write(" ")
            bufferedWriter.write(bookName)
            bufferedWriter.write(" ")
            bufferedWriter.write(accessCode)
            bufferedWriter.newLine() // Use newLine() for appending a newline
            bufferedWriter.flush() // Flush to ensure the data is written immediately

            // Close the writer
            bufferedWriter.close()
        } catch (e: Exception) {
            Log.d("exception", e.toString())
            e.printStackTrace()
        }
    }



    private fun startColorAnimation(textView: TextView) {
        val colorFrom = textView.currentTextColor
        val colorTo = 0xFF00FF00.toInt() // Green color

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 1000 // 1 second

        colorAnimation.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            textView.setTextColor(color)
        }

        colorAnimation.start()
    }




    fun createPdf(download: Boolean) {

        val permissionHelper = RPermissionHelper(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
        permissionHelper.denied {
            if (it) {
                Log.d("Permission check", "Permission denied by system")
                permissionHelper.openAppDetailsActivity()
            } else {
                Log.d("Permission check", "Permission denied")
            }
        }

//Request all permission
        permissionHelper.requestAll {
            Log.d("Permission check", "All permission granted")

            if (!isGenerating && download) {
                isGenerating = true


//                val pdfModeltwo = RPdfGeneratorModeltwo(listOf(), "Your Header")
//
//
//                val randomDataList = generateRandomData()
//
//                for ((index, data) in randomDataList.withIndex()) {
//                    println("Data #$index: $data")
//                    pdfModeltwo.list.add(data)
//                }

                RPdfGenerator.generatePdf(this, pdfModel)

                //RPdfGenerator.generatePdf(this, dummyInfo)

                val handler = Handler()
                val runnable = Runnable {
                    //to avoid multiple generation at the same time. Set isGenerating = false on some delay
                    isGenerating = false
                }
                handler.postDelayed(runnable, 2000)
            }
        }

//Request individual permission
        permissionHelper.requestIndividual {
            Log.d("Permission check", "Individual Permission Granted")
        }
    }





    fun generateRandomData(): List<DateClass> {
        val randomDataList = mutableListOf<DateClass>()

        for (i in 1..30) {
            val randomData = DateClass(
                Date = generateRandomString(),
                AccessNo = generateRandomString(),
                BookName = generateRandomString(),
                RFIDNI = generateRandomString()
            )

            randomDataList.add(randomData)
        }

        return randomDataList
    }

    fun generateRandomString(length: Int = 5): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

}


