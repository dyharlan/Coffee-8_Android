package com.dyharlan.coffee_8

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.view.WindowMetrics
import android.widget.Toast
import com.dyharlan.coffee_8.Backend.Chip8SOC
import com.dyharlan.coffee_8.Backend.MachineType

/*
* A class representing the last frame of the display where:
* prevFrame: contains the pixels that are on from the previous frame. Atm, it is a shallow copy, but works fine with a deep copy as well.
* hires: if the previous frame is hi-res or not
* prevColors: the colors in the previous frame
* Original implementation from: https://github.com/JohnEarnest/Octo/
*/
internal class LastFrame(arr2D: Array<IntArray>, hires: Boolean, colorArr: Array<Color>) {
    var prevFrame: Array<IntArray>
    var hires: Boolean
    var prevColors: Array<Color>

    //constructor
    init {
        prevFrame = arrayOf(
            arr2D[0].clone(),
            arr2D[1].clone(),
            arr2D[2].clone(),
            arr2D[3].clone()
        )
        this.hires = hires
        prevColors = Array(16) { Color.valueOf(0xFFFFFF) }
        System.arraycopy(colorArr, 0, prevColors, 0, prevColors.size)
    }


}

//Extends Chip8SOC and adds additional functions that help in running the emulator
class Chip8Cycle(

    applicationContext: Context,
    planeColors: Array<Color>,
    chip8Surface: SurfaceView,
    machineType: MachineType
) : Chip8SOC(true, machineType), Runnable {
    //height of the chip 8 screen
    private val BITMAP_WIDTH = 128
    private val BITMAP_HEIGHT = 64
    private var isRunning: Boolean = false      //is the machine running?

    private var romStatus: Boolean = false      //is there a rom loaded?

    private var nullifyLastFrame: Boolean = false
    private var planeColors: Array<Color>       //color palette
    private var cpuCycleThread: Thread? = null  //separate thread for the cpu cycle
    private var last: LastFrame? = null         //last frame of th display
    private var bitmap: Bitmap                  //bitmap object where the framebuffer contents will be applied
    private var chip8Surface: SurfaceView       //SurfaceView that will display the output
    private var chip8SurfaceHolder: SurfaceHolder
    private var lowResRect: Rect
    private var hiResRect: Rect
    private var applicationContext: Context
    private var dbHandler: DatabaseHandler      //database handler for the DB that will store the contents of the RPL Flags from apps that will use it
    private var checksum: Long = 0
    private var romArray: ArrayList<Int> = ArrayList<Int>()

    //primary constructor
    init {
        this.applicationContext = applicationContext
        super.enableSound()
        this.planeColors = planeColors
        this.chip8Surface = chip8Surface
        this.chip8SurfaceHolder = chip8Surface.holder
        //val callback: Chip8SurfaceCallBack = Chip8SurfaceCallBack()
        //chip8SurfaceHolder.addCallback(callback)
        var scalingFactor: Int = 2
        var screenWidth = 0
        var screenHeight = 0

        //automatically resize the SurfaceView with integer scaling
        if (Build.VERSION.SDK_INT < 30) {
            screenHeight = applicationContext.resources.displayMetrics.heightPixels
            screenWidth = applicationContext.resources.displayMetrics.widthPixels
        } else {
            val deviceWindowMetrics: WindowMetrics =
                applicationContext.getSystemService<WindowManager>(
                    WindowManager::class.java
                ).maximumWindowMetrics
            screenWidth = deviceWindowMetrics.bounds.width()
            screenHeight = deviceWindowMetrics.bounds.height()
        }
        println(screenWidth)
        while (((64 * (scalingFactor + 2)) <= screenWidth)) {
            scalingFactor += 2
        }
        println("width: $screenWidth")
        println("height: $screenHeight")
        println("new scaling factor $scalingFactor")
        val LOWRES_SCALE_FACTOR = scalingFactor
        val HIRES_SCALE_FACTOR = LOWRES_SCALE_FACTOR / 2
        val hiResViewWidth = BITMAP_WIDTH * HIRES_SCALE_FACTOR
        val hiResViewHeight = BITMAP_HEIGHT * HIRES_SCALE_FACTOR
        val lowResViewWidth = BITMAP_WIDTH * LOWRES_SCALE_FACTOR
        val lowResViewHeight = BITMAP_HEIGHT * LOWRES_SCALE_FACTOR
        lowResRect = Rect(0, 0, lowResViewWidth, lowResViewHeight)
        hiResRect = Rect(0, 0, hiResViewWidth, hiResViewHeight)
        val layoutParams = chip8Surface.layoutParams
        layoutParams.width = hiResViewWidth
        layoutParams.height = hiResViewHeight


        bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.RGB_565) //Instantiate bitmap

        dbHandler = DatabaseHandler(applicationContext) //instantiate db handler
    }

    //reset the emulator
    fun resetROM() {
        if (romStatus) {
            synchronized(this) {
                if (romArray.isEmpty()) {
                    return
                }
                var offset = 0x0
                chip8Init()
                for (i in romArray.indices) {
                    //crc32.update(romArray.get(i) & 0xFF);
                    mem[0x200 + offset] = romArray[i] and 0xFF
                    offset += 0x1
                }
                nullifyLastFrame = true
                startEmulation()
            }

        } else {
            Toast.makeText(applicationContext, "Machine is not running!", Toast.LENGTH_SHORT).show()
        }
    }

    fun getRomSize():Int{
        return romArray.size
    }

//    fun checkROMSize(fileDescriptor: AssetFileDescriptor?): Boolean {
//        var rightSize = true
//        if (fileDescriptor == null) {
//            return false
//        }
//        if ((currentMachine === MachineType.COSMAC_VIP) && (fileDescriptor.length > 3232L)) {
//            rightSize = false
//        } else if ((currentMachine === MachineType.SUPERCHIP_1_1) && (fileDescriptor.length > 3583L)) {
//            rightSize = false
//        } else if ((currentMachine === MachineType.XO_CHIP) && (fileDescriptor.length > 65024L)) {
//            rightSize = false
//        }
//        return rightSize
//    }
    //check if the rom size is greater than the specified values. Return false, otherwise true.
    fun checkROMSize(size: Int, newMachine: MachineType): Boolean {
        var rightSize = true

        if (newMachine === MachineType.COSMAC_VIP && size > 3232L) {
            rightSize = false
        } else if (newMachine === MachineType.SUPERCHIP_1_1 && size > 3583L) {
            rightSize = false
        } else if (newMachine === MachineType.XO_CHIP && size > 65024L) {
            rightSize = false
        }
        return rightSize
    }
    fun closeROM(): Boolean {
        var status = true
        try {
            romArray.clear()
            checksum = 0
            romStatus = false
            chip8Init()
        } catch (ex: Exception) {
            status = false
        }
        return status
    }
    fun openROM(rom: ArrayList<Int>, checksum: Long) {
        romArray = rom.clone() as ArrayList<Int>
        romStatus = true
        println("rom status: $romStatus")
        this.checksum = checksum
    }

    fun startEmulation() {
        if (cpuCycleThread == null && !isRunning) {
            isRunning = true
            cpuCycleThread = Thread(this)
            cpuCycleThread?.start()
        }
    }

    private fun arrayEqual(a: IntArray?, b: IntArray): Boolean {
        val length = a?.size
        if (length != b.size) {
            return false
        }
        for (i in 0 until length) {
            if (a[i] != b[i]) {
                return false
            }
        }
        return true
    }

    private fun arrayEqual(a: Array<Color>?, b: Array<Color>): Boolean {
        val length = a?.size
        if (length != b.size) {
            return false
        }
        for (i in 0 until length) {
            if (a[i] !== b[i]) {
                return false
            }
        }
        return true
    }

    fun stopEmulation() {
        isRunning = false
        cpuCycleThread = null

    }

    fun getBitmap(): Bitmap {
        return bitmap
    }

    override fun run() {
        cpuCycleThread?.priority = Thread.MAX_PRIORITY
        val frameTime = (1000 / 60).toDouble()
        var elapsedTimeFromEpoch = System.currentTimeMillis()
        var origin = elapsedTimeFromEpoch + frameTime / 2
        while (isRunning) {
            //println("running...")
            //println( cpuCycleThread!!.isAlive)
            synchronized(this) {
                val diff = System.currentTimeMillis() - elapsedTimeFromEpoch
                elapsedTimeFromEpoch += diff
                var i: Long = 0
                while (origin < elapsedTimeFromEpoch - frameTime && i < 2) {
                    var j = 0
                    while (j < this.cycles && !this.waitState) {
                        //try {
                        if(!isCpuHalted)
                            super.cpuExec()
                        else{
                            stopEmulation()
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(applicationContext,"An error occurred during execution and the machine has been halted: $causeOfHalt",Toast.LENGTH_SHORT).show()
                            }
                            break
                        }
                        //} catch (ex: Exception) {
                        //    stopEmulation()
                        //    ex.printStackTrace()
                        //    break
                        //}
                        j++
                    }
                    super.updateTimers()
                    origin += frameTime
                    i++
                }
                try {
                    Thread.sleep(frameTime.toLong())
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }
                if (this.vbLankInterrupt == 1) {
                    this.vbLankInterrupt = 2
                }
            }
            //if there is a last frame
            if (last != null) {
                //check if the previous frame and the previous palette is the same as the current frame in both planes.
                if (arrayEqual(
                        last?.prevFrame?.get(0),
                        super.graphics[0]
                    ) && arrayEqual(last?.prevFrame?.get(1), super.graphics[1]) && arrayEqual(
                        last?.prevFrame?.get(2), super.graphics[2]
                    ) && arrayEqual(last?.prevFrame?.get(3), super.graphics[3]) && arrayEqual(
                        last?.prevColors, planeColors
                    )
                ) {
                    //exit early if it is the same.
                    continue;
                }
                //clear last frame if we've switched from hi res to lowres or vice versa. Also clear it if the color palette has changed
                if (last?.hires != super.getHiRes() || nullifyLastFrame || !arrayEqual(last?.prevColors, planeColors)){
                    last = null;
                    if(nullifyLastFrame){
                        nullifyLastFrame = false
                    }
                }

            }
            val lastPixels: Array<IntArray> =
                if (last != null) last!!.prevFrame else Array<IntArray>(4) {
                    IntArray(
                        this.machineWidth * this.machineHeight
                    )
                }

            if (this.graphics != null) {
                for (y in 0 until this.machineHeight) {
                    for (x in 0 until this.machineWidth) {
                        val newPlane: Int =
                            super.graphics[3][x + y * this.machineWidth] shl 3 or (super.graphics[2][x + y * this.machineWidth] shl 2) or (super.graphics[1][x + y * this.machineWidth] shl 1) or super.graphics[0][x + y * this.machineWidth] and 0xF
                        //selectively update each pixel if the last frame exists
                        if (last != null) {
                            val oldPlane =
                                (lastPixels[3][x + y * this.machineWidth] shl 3) or (lastPixels[2][x + y * this.machineWidth] shl 2) or (lastPixels[1][x + y * this.machineWidth] shl 1) or lastPixels[0][x + y * this.machineWidth] and 0xF
                            if (oldPlane != newPlane) {
                                bitmap.setPixel(x, y, planeColors[newPlane].toArgb())

                            }
                        } else {
                            //full rewrite of the screen
                            bitmap.setPixel(x, y, planeColors[newPlane].toArgb())
                        }
                    }
                }
            }
            //chip8Canvas.postInvalidate()
            updateSurface(chip8SurfaceHolder, bitmap)
            last = LastFrame(super.graphics, this.hiRes, planeColors)
        }
    }

    private fun updateSurface(holder: SurfaceHolder, bitmap: Bitmap) {
        if (holder.surface.isValid) {
            val canvas: Canvas = holder.lockHardwareCanvas()
            if (this.hiRes) {
                canvas.drawBitmap(bitmap, null, hiResRect, null)
            } else if (!this.hiRes) {
                canvas.drawBitmap(bitmap, null, lowResRect, null)
            }

            holder.unlockCanvasAndPost(canvas)
        }

    }

    fun getRomStatus(): Boolean {
        return romStatus
    }


    fun getIsRunning(): Boolean {
        return isRunning
    }

    //android implementation of FX75 and FX85
    override fun C8INST_FX75() {
        val flags = ArrayList<Int>()
        for (n in 0..(if (super.X > 0x7) 0x7 else super.X)) {
            flags.add(super.v[n] and 0xFF)
        }
        dbHandler.saveFlags(checksum, flags.toTypedArray())
    }

    override fun C8INST_FX85() {
        val flags = dbHandler.loadFlags(checksum)
        for (n in 0..(if (super.X > 0x7) 0x7 else super.X)) {
            super.v[n] = flags[n] and 0xFF
        }
    }
}