package com.example.lms.utils

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.csnprintersdk.csnio.CSNUSBPrinting


class TaskOpen(usb: CSNUSBPrinting?, usbManager: UsbManager?, usbDevice: UsbDevice?, context: Context?) : Runnable {
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