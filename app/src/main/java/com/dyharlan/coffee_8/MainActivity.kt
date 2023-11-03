package com.dyharlan.coffee_8

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dyharlan.coffee_8.Backend.Chip8SOC
import com.dyharlan.coffee_8.Backend.MachineType
import java.io.File


internal class LastFrame(arr2D: Array<IntArray>, hires: Boolean, colorArr: Array<Color>) {
    var prevFrame: Array<IntArray>
    var hires: Boolean
    lateinit var prevColors: Array<Color>

    //constructor
    init {
        prevFrame = arrayOf(
            arr2D[0].clone(),
            arr2D[1].clone(),
            arr2D[2].clone(),
            arr2D[3].clone()
        )
        this.hires = hires
        prevColors = Array(16){ Color.valueOf(0xFFFFFF)}
        System.arraycopy(colorArr, 0, prevColors, 0, prevColors.size)
    }
}
class MainActivity : AppCompatActivity() {
    private lateinit var planeColors: Array<Color>
    private lateinit var chip8SOC: Chip8SOC
    private lateinit var chip8Cycle: Chip8Cycle
    private companion object{
        //PERMISSION request constant, assign any value
        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        planeColors = arrayOf(
        Color.valueOf(Color.parseColor("#FF996600")),
        Color.valueOf(Color.parseColor("#FFFFCC00")),
        Color.valueOf(Color.parseColor("#FFFF6600")),
        Color.valueOf(Color.parseColor("#FF662200")),
        Color.valueOf(0xBF2AED),
        Color.valueOf(Color.MAGENTA),
        Color.valueOf(Color.YELLOW),
        Color.valueOf(Color.GREEN),
        Color.valueOf(Color.GRAY),
        Color.valueOf(0x4B0082), //INDIGO

        Color.valueOf(0xEE82EE), //VIOLET

        Color.valueOf(0xAA5500),
        Color.valueOf(Color.BLACK),
        Color.valueOf(Color.WHITE),
        Color.valueOf(Color.BLUE),
        Color.valueOf(Color.RED)
        )




        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chip8SOC = Chip8SOC(true, MachineType.XO_CHIP)
        chip8SOC.enableSound()
        val chip8Surface = findViewById<SurfaceView>(R.id.chip8Surface)
        chip8Cycle = Chip8Cycle(chip8SOC, planeColors, chip8Surface)
        val keyPad: Array<Button> = arrayOf(
            findViewById(R.id.keyPad0),
            findViewById(R.id.keyPad1),
            findViewById(R.id.keyPad2),
            findViewById(R.id.keyPad3),
            findViewById(R.id.keyPad4),
            findViewById(R.id.keyPad5),
            findViewById(R.id.keyPad6),
            findViewById(R.id.keyPad7),
            findViewById(R.id.keyPad8),
            findViewById(R.id.keyPad9),
            findViewById(R.id.keyPadA),
            findViewById(R.id.keyPadB),
            findViewById(R.id.keyPadC),
            findViewById(R.id.keyPadD),
            findViewById(R.id.keyPadE),
            findViewById(R.id.keyPadF),
        )
        val keyLabels: Array<String> = arrayOf(
            "0","1","2","3",
            "4","5","6","7",
            "8","9","A","B",
            "C","D","E","F"
        )
        for((currentKey, key) in keyPad.withIndex()){
            key.text = keyLabels[currentKey]
            key.setOnTouchListener { v, event ->
                if (chip8SOC.keyPad == null) {
                    false
                }
                if (event.getAction() === MotionEvent.ACTION_DOWN) {
                    chip8SOC.keyPad[currentKey] = true
                } else if (event.getAction() === MotionEvent.ACTION_CANCEL) {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(currentKey)
                    }
                    chip8SOC.keyPad[currentKey] = false
                } else if (event.getAction() === MotionEvent.ACTION_UP) {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(currentKey)
                    }
                    chip8SOC.keyPad[currentKey] = false
                }

                false
            }
        }



    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {

        if(event.action == KeyEvent.ACTION_DOWN){
            println("pressed a key")
            return when (event.keyCode) {
                KeyEvent.KEYCODE_X -> {
                    chip8SOC.keyPad[0] = true
                    true
                }

                KeyEvent.KEYCODE_1 -> {
                    chip8SOC.keyPad[1] = true
                    true
                }

                KeyEvent.KEYCODE_2 -> {
                    chip8SOC.keyPad[2] = true
                    true
                }

                KeyEvent.KEYCODE_3 -> {
                    chip8SOC.keyPad[3] = true
                    true
                }

                KeyEvent.KEYCODE_Q -> {
                    chip8SOC.keyPad[4] = true
                    true
                }

                KeyEvent.KEYCODE_W -> {
                    chip8SOC.keyPad[5] = true
                    true
                }

                KeyEvent.KEYCODE_E -> {
                    chip8SOC.keyPad[6] = true
                    true
                }

                KeyEvent.KEYCODE_A -> {
                    chip8SOC.keyPad[7] = true
                    true
                }

                KeyEvent.KEYCODE_S -> {
                    chip8SOC.keyPad[8] = true
                    true
                }

                KeyEvent.KEYCODE_D -> {
                    chip8SOC.keyPad[9] = true
                    true
                }

                KeyEvent.KEYCODE_Z -> {
                    chip8SOC.keyPad[10] = true
                    true
                }

                KeyEvent.KEYCODE_C -> {
                    chip8SOC.keyPad[11] = true
                    true
                }

                KeyEvent.KEYCODE_4 -> {
                    chip8SOC.keyPad[12] = true
                    true
                }

                KeyEvent.KEYCODE_R -> {
                    chip8SOC.keyPad[13] = true
                    true
                }

                KeyEvent.KEYCODE_F -> {
                    chip8SOC.keyPad[14] = true
                    true
                }

                KeyEvent.KEYCODE_V -> {
                    chip8SOC.keyPad[15] = true
                    true
                }

                else -> super.dispatchKeyEvent(event)
            }
        }else if(event.action == KeyEvent.ACTION_DOWN){
            println("released a key")
            return when (event.keyCode) {
                KeyEvent.KEYCODE_X -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(0)
                    }
                    chip8SOC.keyPad[0] = false
                    true
                }

                KeyEvent.KEYCODE_1 -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(1)
                    }
                    chip8SOC.keyPad[1] = false
                    true
                }

                KeyEvent.KEYCODE_2 -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(2)
                    }
                    chip8SOC.keyPad[2] = false
                    true
                }

                KeyEvent.KEYCODE_3 -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(3)
                    }
                    chip8SOC.keyPad[3] = false
                    true
                }

                KeyEvent.KEYCODE_Q -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(4)
                    }
                    chip8SOC.keyPad[4] = false
                    true
                }

                KeyEvent.KEYCODE_W -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(5)
                    }
                    chip8SOC.keyPad[5] = false
                    true
                }

                KeyEvent.KEYCODE_E -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(6)
                    }
                    chip8SOC.keyPad[6] = false
                    true
                }

                KeyEvent.KEYCODE_A -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(7)
                    }
                    chip8SOC.keyPad[7] = false
                    true
                }

                KeyEvent.KEYCODE_S -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(8)
                    }
                    chip8SOC.keyPad[8] = false
                    true
                }

                KeyEvent.KEYCODE_D -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(9)
                    }
                    chip8SOC.keyPad[9] = false
                    true
                }

                KeyEvent.KEYCODE_Z -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(10)
                    }
                    chip8SOC.keyPad[10] = false
                    true
                }

                KeyEvent.KEYCODE_C -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(11)
                    }
                    chip8SOC.keyPad[11] = false
                    true
                }

                KeyEvent.KEYCODE_4 -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(12)
                    }
                    chip8SOC.keyPad[12] = false
                    true
                }

                KeyEvent.KEYCODE_R -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(13)
                    }
                    chip8SOC.keyPad[13] = false
                    true
                }

                KeyEvent.KEYCODE_F -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(14)
                    }
                    chip8SOC.keyPad[14] = false
                    true
                }

                KeyEvent.KEYCODE_V -> {
                    if (chip8SOC.waitState) {
                        chip8SOC.waitState = false
                        chip8SOC.sendKeyStroke(15)
                    }
                    chip8SOC.keyPad[15] = false
                    true
                }

                else -> super.dispatchKeyEvent(event)
            }
        }
        return super.dispatchKeyEvent(event)
    }






    fun openLoadROMIntent(view: View){
        if (checkPermission()){
            Log.d(TAG, "onCreate: Permission already granted, create folder")
            val file = File(Environment.getExternalStorageDirectory(), "superneatboy.xo8")
            chip8Cycle.loadROM(file)
        }
        else{
            Log.d(TAG, "onCreate: Permission was not granted, request")
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            Environment.isExternalStorageManager()
        }
        else{
            //Android is below 11(R)
            val write = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            }
            catch (e: Exception){
                Log.e(TAG, "requestPermission: ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        }
        else{
            //Android is below 11(R)
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.d(TAG, "storageActivityResultLauncher: ")
        //here we will handle the result of our intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            if (Environment.isExternalStorageManager()){
                //Manage External Storage Permission is granted
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is granted")
                //createFolder()
            }
            else{
                //Manage External Storage Permission is denied....
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is denied....")
                toast("Manage External Storage Permission is denied....")
            }
        }
        else{
            //Android is below 11(R)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty()){
                //check each permission if granted or not
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read){
                    //External Storage Permission granted
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission granted")
                    //createFolder()
                }
                else{
                    //External Storage Permission denied...
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission denied...")
                    toast("External Storage Permission denied...")
                }
            }
        }
    }


    private fun toast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun pauseEmulation(view: View){
        chip8Cycle.stopEmulation()
    }

    class Chip8Cycle: Runnable {

        private var isRunning: Boolean = false
        private var romStatus: Boolean = false;
        private var chip8SOC: Chip8SOC
        private var planeColors: Array<Color>
        //private var chip8Canvas: Chip8Canvas
        private var cpuCycleThread: Thread? = null
        private var last: LastFrame? = null
        private var bitmap: Bitmap
        private var chip8Surface: SurfaceView
        private var chip8SurfaceHolder: SurfaceHolder
        private var rect: Rect
        //private var

        private lateinit var rom: File
        constructor(chip8SOC: Chip8SOC, planeColors: Array<Color>, chip8Surface: SurfaceView){
            this.chip8SOC = chip8SOC
            this.planeColors = planeColors
            //this.chip8Canvas = chip8Canvas
            this.chip8Surface = chip8Surface
            this.chip8SurfaceHolder = chip8Surface.holder
            rect = Rect(0,0,128*5,64*5)
            bitmap = Bitmap.createBitmap(128,64, Bitmap.Config.RGB_565)
        }

        fun loadROM(rom: File) {
            //try {
                //stopEmulation();
                synchronized(chip8SOC) {
                    romStatus = chip8SOC.loadROM(rom)
                    println("romstatus: $romStatus")

                }
                if (romStatus) {
                    this.rom = rom
                        //clear the last frame each time a new rom is loaded.
                        last = null
                        startEmulation()

                } else {
                    romStatus = false

                    //Toast.makeText(this,"No ROM has been loaded into the emulator! Please load a ROM and try again.",Toast.LENGTH_LONG).show()
                }
            //} catch (ioe: IOException) {
                //romStatus = false
                //Toast.makeText(context,"There was a problem loading the ROM file:$ioe",Toast.LENGTH_LONG).show()
            //}
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
        fun getBitmap(): Bitmap{
            return bitmap
        }
        override fun run() {

            cpuCycleThread!!.priority = Thread.NORM_PRIORITY
            val frameTime = (1000 / 60).toDouble()
            var elapsedTimeFromEpoch = System.currentTimeMillis()
            var origin = elapsedTimeFromEpoch + frameTime / 2
            while (isRunning) {
                //println("running...")
                //println( cpuCycleThread!!.isAlive)
                synchronized(chip8SOC) {
                    val diff = System.currentTimeMillis() - elapsedTimeFromEpoch
                    elapsedTimeFromEpoch += diff
                    var i: Long = 0
                    while (origin < elapsedTimeFromEpoch - frameTime && i < 2) {
                        var j = 0
                        while (j < chip8SOC.getCycles() && !chip8SOC.getWaitState()) {
                            //try {
                                chip8SOC.cpuExec()
                            //} catch (ex: Exception) {
                                //stopEmulation()
                                //println(ex)
                                //break
                            //}
                            j++
                        }
                        chip8SOC.updateTimers()
                        origin += frameTime
                        i++
                    }
                    try {
                        Thread.sleep(frameTime.toInt().toLong())
                    } catch (ex: InterruptedException) {
                        ex.printStackTrace()
                    }
                    if (chip8SOC.getVBLankInterrupt() == 1) {
                        chip8SOC.setVBLankInterrupt(2)
                    }
                }
                //if there is a last frame
                if(last != null){
                    //check if the previous frame and the previous palette is the same as the current frame in both planes.
                    if(arrayEqual(last!!.prevFrame[0], chip8SOC.graphics[0]) && arrayEqual(last!!.prevFrame[1], chip8SOC.graphics[1])  && arrayEqual(
                            last!!.prevFrame[2], chip8SOC.graphics[2])  && arrayEqual(last!!.prevFrame[3], chip8SOC.graphics[3]) && arrayEqual(
                            last!!.prevColors,planeColors)){
                        //exit early if it is the same.
                        continue;
                    }
                    //clear last frame if we've switched from hi res to lowres or vice versa. Also clear it if the color palette has changed
                    if (last!!.hires != chip8SOC.getHiRes() || !arrayEqual(last!!.prevColors,planeColors))
                        last = null;
                }
                var lastPixels: Array<IntArray> =
                    if (last != null) last!!.prevFrame else Array<IntArray>(4) {
                        IntArray(
                            chip8SOC.getMachineWidth() * chip8SOC.getMachineHeight()
                        )
                    }

                if (chip8SOC.graphics != null) {
                    for (y in 0 until chip8SOC.getMachineHeight()) {
                        for (x in 0 until chip8SOC.getMachineWidth()) {
                            //int newPlane = (chip8SOC.graphics[1][(x) + ((y) * chip8SOC.getMachineWidth())] << 1 | chip8SOC.graphics[0][(x) + ((y) * chip8SOC.getMachineWidth())]) & 0x3;
                            val newPlane: Int = chip8SOC.graphics.get(3)
                                .get(x + y * chip8SOC.getMachineWidth()) shl 3 or (chip8SOC.graphics.get(
                                2
                            ).get(
                                x + y * chip8SOC.getMachineWidth()
                            ) shl 2) or (chip8SOC.graphics.get(1)
                                .get(x + y * chip8SOC.getMachineWidth()) shl 1) or chip8SOC.graphics.get(
                                0
                            ).get(
                                x + y * chip8SOC.getMachineWidth()
                            ) and 0xF
                            //System.out.println(newPlane);
                            //selectively update each pixel if the last frame exists
                            if (last != null) {
                                //int oldPlane = (lastPixels[1][(x) + ((y) * chip8SOC.getMachineWidth())] << 1 | lastPixels[0][(x) + ((y) * chip8SOC.getMachineWidth())]) & 0x3;
                                val oldPlane =
                                    (lastPixels[3][x + y * chip8SOC.getMachineWidth()] shl 3) or (lastPixels[2][x + y * chip8SOC.getMachineWidth()] shl 2) or (lastPixels[1][x + y * chip8SOC.getMachineWidth()] shl 1) or lastPixels[0][x + y * chip8SOC.getMachineWidth()] and 0xF
                                if (oldPlane != newPlane) {
                                    //frameBuffer.setColor(planeColors[newPlane])
                                    //frameBuffer.fillRect(x, y, 1, 1)
                                    bitmap.setPixel(x,y,planeColors[newPlane].toArgb())

                                }
                            } else {
                                //full rewrite of the screen
                                //frameBuffer.setColor(planeColors[newPlane])
                                //frameBuffer.fillRect(x, y, 1, 1)
                                bitmap.setPixel(x,y,planeColors[newPlane].toArgb())
                            }
                        }
                    }
                }
                //chip8Canvas.postInvalidate()
                updateSurface(chip8SurfaceHolder, bitmap)
                last = LastFrame(chip8SOC.graphics, chip8SOC.getHiRes(), planeColors)
            }
        }

        private fun updateSurface(holder: SurfaceHolder,bitmap: Bitmap){
            if(holder.surface.isValid){
                var canvas: Canvas = holder.lockHardwareCanvas()
                canvas.drawBitmap(bitmap, null, rect, null)
                holder.unlockCanvasAndPost(canvas)
            }

        }
    }

//    class Chip8Canvas  @JvmOverloads constructor(context: Context,
//                                  attrs: AttributeSet? = null, defStyleAttr: Int = 0)
//        : View(context, attrs, defStyleAttr) {
//
//        //private var partialUpdate: Boolean = false
//
////        private var xCoord: Float = 0.0f
////        private var yCoord: Float = 0.0f
//        private lateinit var bitmap: Bitmap
//
//        private var rect: Rect = Rect(0,0,128*5,64*5)
//        init {
//            setWillNotDraw(false) // Enable onDraw in this view group
//        }
//        fun loadBitmap(bitmap: Bitmap){
//            this.bitmap = bitmap
//        }
//
//        // Called when the view should render its content.
//        override fun onDraw(canvas: Canvas) {
//            super.onDraw(canvas)
//
//
//        }
//    }


}