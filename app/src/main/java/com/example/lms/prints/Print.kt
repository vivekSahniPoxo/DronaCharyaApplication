package com.example.lms.prints

import android.content.Context
import android.graphics.*
import com.csnprintersdk.csnio.CSNPOS
import com.example.lms.utils.Cons
import java.io.IOException
import kotlin.experimental.and

object Prints {
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
                    val bmIu = getImageFromAssetsFile(ctx, "iu.jpeg")
                    val bmYellowmen = getImageFromAssetsFile(ctx, "yellowmen.png")
                    val bm01 = getImageFromAssetsFile(ctx, "01.jpg")
                    val bm02 = getImageFromAssetsFile(ctx, "02.jpg")
                    val bm03 = getImageFromAssetsFile(ctx, "03.jpg")
                    val bm04 = getImageFromAssetsFile(ctx, "04.jpg")
                    val bm05 = getImageFromAssetsFile(ctx, "05.jpg")
                    for (i in 0 until nCount) {
                        if (!pos.GetIO().IsOpened()) break
                        if (nPrintContent >= 1) {
                            if (nPrintWidth == Cons.nPrintWidth) {
                                pos.POS_Reset()
                                pos.POS_FeedLine()
                                pos.POS_TextOut("UTF-8 ==> vivek \r\n", 3, 0, 0, 0, 0, 0)
                                pos.POS_FeedLine()
                                pos.POS_FeedLine()
                            } else {
                                pos.POS_Reset()
                                pos.POS_FeedLine()
                                pos.POS_TextOut("电子发票证明联\r\n", 0, 96, 1, 1, 0, 0)
                                pos.POS_FeedLine()
                                pos.POS_TextOut(
                                    "小票：270500027719 收银员：010121212122121\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "------------------------------------------\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "   商品编码        单价  数量       小计\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0)
                                pos.POS_TextOut(
                                    "01.9940228004700    3.98   1.181  20080616\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "   番石榴     小计：4.70   小计： 4.70小计\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "02.996100800220     6.00   0.376  20080617\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "   白面条     小计：2.20          4.70小计\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "03.6921644701204    3.50   1(包)  20080617\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "   恒源德调味 小计：3.50          3.50小计\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "04.9940316000602    5.16   0.116  20080617\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "   生葱       小计：0.60          0.60小计   \r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "------------------------------------------\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "购货总额：                         11.00   \r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "付款：   现金       人民币         101.00\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "找零：   现金       人民币         90.00  \r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "            售出商品数量：4件         \r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut(
                                    "           2005-09-13  16:50:19\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_TextOut("            欢迎光临   多谢惠顾\r\n", 0, 0, 0, 0, 0, 0)
                                pos.POS_TextOut("             （开发票当月有效）\r\n", 0, 0, 0, 0, 0, 0)
                                pos.POS_TextOut("              满家福百货南邮店\r\n", 0, 0, 0, 0, 0, 0)
                                pos.POS_TextOut(
                                    "小票：270500027721           收银员：01012\r\n",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                                pos.POS_FeedLine()
                                pos.POS_FeedLine()
                                pos.POS_TextOut(
                                    """
                                        REC${String.format("%03d", i + 1)}
                                        Printer
                                        简体中文测试


                                        """.trimIndent(), 0, 1, 1, 0, 0, 0
                                )
                                pos.POS_FeedLine()
                                pos.POS_FeedLine()
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
                        if (nPrintContent >= 2) {
                            if (bm1 != null) {
                                pos.POS_PrintPicture(bm1, nPrintWidth, 1, nCompressMethod)
                            }
                            if (bm2 != null) {
                                pos.POS_PrintPicture(bm2, nPrintWidth, 1, nCompressMethod)
                            }
                            if (nPrintContent == 2 && nCount > 1) {
                                pos.POS_HalfCutPaper()
                                try {
                                    Thread.currentThread()
                                    Thread.sleep(4500)
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }
                            }
                            if (nPrintContent == 2 && nCount == 1) {
                                if (bBeeper) pos.POS_Beep(1, 5)
                                if (bCutter) pos.POS_FullCutPaper()
                                if (bDrawer) pos.POS_KickDrawer(0, 100)
                            }
                        }
                    }
                    if (nPrintContent >= 3) {
                        if (bmBlackWhite != null) {
                            pos.POS_PrintPicture(bmBlackWhite, nPrintWidth, 1, nCompressMethod)
                        }
                        if (bmIu != null) {
                            pos.POS_PrintPicture(bmIu, nPrintWidth, 0, nCompressMethod)
                        }
                        if (bmYellowmen != null) {
                            pos.POS_PrintPicture(bmYellowmen, nPrintWidth, 0, nCompressMethod)
                        }
                        if (nPrintContent == 3 && nCount > 1) {
                            pos.POS_HalfCutPaper()
                            try {
                                Thread.currentThread()
                                Thread.sleep(6000)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                        if (nPrintContent == 3 && nCount == 1) {
                            if (bBeeper) pos.POS_Beep(1, 5)
                            if (bCutter) pos.POS_FullCutPaper()
                            if (bDrawer) pos.POS_KickDrawer(0, 100)
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
            return -8.also {
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

    fun resizeImage(bitmap: Bitmap, w: Int, h: Int): Bitmap {
        // load the origial Bitmap
        val width = bitmap.width
        val height = bitmap.height

        // calculate the scale
        val scaleWidth = w.toFloat() / width
        val scaleHeight = h.toFloat() / height

        // create a matrix for the manipulation
        val matrix = Matrix()
        // resize the Bitmap
        matrix.postScale(scaleWidth, scaleHeight)
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);

        // recreate the new Bitmap

        // make a Drawable from Bitmap to allow to set the Bitmap
        // to the ImageView, ImageButton or what ever
        return Bitmap.createBitmap(
            bitmap, 0, 0, width,
            height, matrix, true
        )
    }

    fun getTestImage1(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, 4, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        canvas.drawRect(0f, 0f, width.toFloat(), 4f, paint)

//		paint.setColor(Color.BLACK);
//		for(int i = 0; i < 8; ++i)
//		{
//			for(int x = i; x < width; x += 8)
//			{
//				for(int y = i; y < height; y += 8)
//				{
//					canvas.drawPoint(x, y, paint);
//				}
//			}
//		}
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







}