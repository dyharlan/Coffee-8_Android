package com.dyharlan.coffee_8

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.view.WindowMetrics
import android.widget.Toast
import com.dyharlan.coffee_8.Backend.Chip8SOC
import com.dyharlan.coffee_8.Backend.MachineType
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream

    class Chip8Cycle(applicationContext: Context, planeColors: Array<Color>, chip8Surface: SurfaceView, machineType: MachineType): Chip8SOC(true, machineType), Runnable {
        private val BITMAP_WIDTH = 128
        private val BITMAP_HEIGHT = 64
        private var isRunning: Boolean = false
        private var romStatus: Boolean = false
        //private var chip8SOC: Chip8SOC
        private var planeColors: Array<Color>
        private var cpuCycleThread: Thread? = null
        private var last: LastFrame? = null
        private var bitmap: Bitmap
        private var chip8Surface: SurfaceView
        private var chip8SurfaceHolder: SurfaceHolder
        private var lowResRect: Rect
        private var hiResRect: Rect
        private var applicationContext: Context
        private var dbHandler: DatabaseHandler

        //private lateinit var rom: InputStream
        init{
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
            while(((64 * (scalingFactor+2)) <= screenWidth )){
                scalingFactor+=2
            }
            println("width: $screenWidth")
            println("height: $screenHeight")
            println("new scaling factor $scalingFactor")
            var LOWRES_SCALE_FACTOR = scalingFactor
            var HIRES_SCALE_FACTOR = LOWRES_SCALE_FACTOR/2
            var hiResViewWidth = BITMAP_WIDTH * HIRES_SCALE_FACTOR
            var hiResViewHeight = BITMAP_HEIGHT * HIRES_SCALE_FACTOR
            var lowResViewWidth = BITMAP_WIDTH * LOWRES_SCALE_FACTOR
            var lowResViewHeight = BITMAP_HEIGHT * LOWRES_SCALE_FACTOR
            lowResRect = Rect(0,0,lowResViewWidth,lowResViewHeight)
            hiResRect = Rect(0,0,hiResViewWidth,hiResViewHeight)
            val layoutParams = chip8Surface.layoutParams
            layoutParams.width = hiResViewWidth
            layoutParams.height = hiResViewHeight
            bitmap = Bitmap.createBitmap(BITMAP_WIDTH,BITMAP_HEIGHT, Bitmap.Config.RGB_565)
            dbHandler = DatabaseHandler(applicationContext)
        }
        fun resetROM(){
           if(romStatus){

               synchronized(this){
                   super.reset()
               }
               last = null
               startEmulation()
           }else{
               Toast.makeText(applicationContext, "Machine is not running!", Toast.LENGTH_SHORT).show()
           }
        }
        fun checkROMSize(fileDescriptor: AssetFileDescriptor?): Boolean {
            var rightSize = true
            if(fileDescriptor == null){
                return false
            }
            if (currentMachine === MachineType.COSMAC_VIP && fileDescriptor.length > 3232L) {
                rightSize = false
            } else if (currentMachine === MachineType.SUPERCHIP_1_1 && fileDescriptor.length > 3583L) {
                rightSize = false
            } else if (currentMachine === MachineType.XO_CHIP && fileDescriptor.length > 65024L) {
                rightSize = false
            }
            return rightSize
        }

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
        fun openROM(rom: InputStream) {
            try {
                synchronized(this) {
                    romStatus = super.loadROM(rom)
                    println("rom status: $romStatus")
                }
                if (romStatus) {
                    //clear the last frame each time a new rom is loaded.
                    last = null
                    startEmulation()

                } else {
                    romStatus = false
                    Toast.makeText(applicationContext,"No ROM has been loaded into the emulator! Please load a ROM and try again.", Toast.LENGTH_LONG).show()
                }
            } catch (ioe: IOException) {
                romStatus = false
                Toast.makeText(applicationContext,"There was a problem loading the ROM file: $ioe",
                    Toast.LENGTH_LONG).show()
            }
        }
        fun startEmulation() {
            if (cpuCycleThread == null) {
                isRunning = true
                cpuCycleThread = Thread(this)
                cpuCycleThread!!.start()
            }
        }
        private fun arrayEqual(a: IntArray, b: IntArray): Boolean {
            val length = a.size
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
        private fun arrayEqual(a: ByteArray?, b: ByteArray): Boolean {
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

        private fun arrayEqual(a: Array<Color>, b: Array<Color>): Boolean {
            val length = a.size
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

            cpuCycleThread!!.priority = Thread.MAX_PRIORITY
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
                                super.cpuExec()
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
                if(last != null){
                    //check if the previous frame and the previous palette is the same as the current frame in both planes.
                    if(arrayEqual(last!!.prevFrame[0], this.graphics[0]) && arrayEqual(last!!.prevFrame[1], this.graphics[1])  && arrayEqual(
                            last!!.prevFrame[2], this.graphics[2])  && arrayEqual(last!!.prevFrame[3], this.graphics[3]) && arrayEqual(
                            last!!.prevColors,planeColors)){
                        //exit early if it is the same.
                        continue;
                    }
                    //clear last frame if we've switched from hi res to lowres or vice versa. Also clear it if the color palette has changed
                    if (last!!.hires != this.getHiRes() || !arrayEqual(last!!.prevColors,planeColors))
                        last = null;
                }
                var lastPixels: Array<IntArray> =
                    if (last != null) last!!.prevFrame else Array<IntArray>(4) {
                        IntArray(
                            this.machineWidth * this.machineHeight
                        )
                    }

                if (this.graphics != null) {
                    for (y in 0 until this.machineHeight) {
                        for (x in 0 until this.machineWidth) {
                            //int newPlane = (chip8SOC.graphics[1][(x) + ((y) * chip8SOC.getMachineWidth())] << 1 | chip8SOC.graphics[0][(x) + ((y) * chip8SOC.getMachineWidth())]) & 0x3;
                            val newPlane: Int = this.graphics[3][x + y * this.machineWidth] shl 3 or (this.graphics[2][x + y * this.machineWidth] shl 2) or (this.graphics[1][x + y * this.machineWidth] shl 1) or this.graphics[0][x + y * this.machineWidth] and 0xF
                            //System.out.println(newPlane);
                            //selectively update each pixel if the last frame exists
                            if (last != null) {
                                //int oldPlane = (lastPixels[1][(x) + ((y) * chip8SOC.getMachineWidth())] << 1 | lastPixels[0][(x) + ((y) * chip8SOC.getMachineWidth())]) & 0x3;
                                val oldPlane =
                                    (lastPixels[3][x + y * this.machineWidth] shl 3) or (lastPixels[2][x + y * this.machineWidth] shl 2) or (lastPixels[1][x + y * this.machineWidth] shl 1) or lastPixels[0][x + y * this.machineWidth] and 0xF
                                if (oldPlane != newPlane) {
                                    bitmap.setPixel(x,y,planeColors[newPlane].toArgb())

                                }
                            } else {
                                //full rewrite of the screen
                                bitmap.setPixel(x,y,planeColors[newPlane].toArgb())
                            }
                        }
                    }
                }
                //chip8Canvas.postInvalidate()
                updateSurface(chip8SurfaceHolder, bitmap)
                last = LastFrame(this.graphics, this.getHiRes(), planeColors)
            }
        }

        private fun updateSurface(holder: SurfaceHolder, bitmap: Bitmap){
            if(holder.surface.isValid){
                var canvas: Canvas = holder.lockHardwareCanvas()
                if(this.hiRes){
                    canvas.drawBitmap(bitmap, null, hiResRect, null)
                }else if(!this.hiRes){
                    canvas.drawBitmap(bitmap, null, lowResRect, null)
                }

                holder.unlockCanvasAndPost(canvas)
            }

        }

        public fun getRomStatus() : Boolean{
            return romStatus
        }



        public fun getIsRunning(): Boolean{
            return isRunning
        }

        override fun C8INST_FX75() {
            var flags = ArrayList<Int>()
            for(n in 0..(if (super.X > 0x7) 0x7 else super.X)){
                flags.add(super.v[n] and 0xFF)
            }
            dbHandler.saveFlags(super.crc32Checksum, flags.toTypedArray())
        }

        override fun C8INST_FX85() {
            val flags = dbHandler.loadFlags(super.crc32Checksum)
            for(n in 0..(if (super.X > 0x7) 0x7 else super.X)){
                super.v[n] = flags[n] and 0xFF
            }
        }
    }