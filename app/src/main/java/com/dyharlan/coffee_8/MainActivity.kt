package com.dyharlan.coffee_8

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.view.WindowMetrics
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dyharlan.coffee_8.Backend.Chip8SOC
import com.dyharlan.coffee_8.Backend.MachineType
import java.io.IOException
import java.io.InputStream


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
        prevColors = Array(16){ Color.valueOf(0xFFFFFF)}
        System.arraycopy(colorArr, 0, prevColors, 0, prevColors.size)
    }
}
class MainActivity : AppCompatActivity() {
    private lateinit var planeColors: Array<Color>
    private lateinit var chip8Cycle: Chip8Cycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setContentView(R.layout.activity_main)


        val chip8Surface = findViewById<SurfaceView>(R.id.chip8Surface)

        chip8Cycle = Chip8Cycle(applicationContext, planeColors, chip8Surface)
        chip8Cycle.getChip8SOC().cycles = 200
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
                if (chip8Cycle.getChip8SOC().keyPad == null) {
                    false
                }
                if (event.getAction() === MotionEvent.ACTION_DOWN) {
                    chip8Cycle.getChip8SOC().keyPad[currentKey] = true
                } else if (event.getAction() === MotionEvent.ACTION_CANCEL) {
                    if (chip8Cycle.getChip8SOC().waitState) {
                        chip8Cycle.getChip8SOC().waitState = false
                        chip8Cycle.getChip8SOC().sendKeyStroke(currentKey)
                    }
                    chip8Cycle.getChip8SOC().keyPad[currentKey] = false
                } else if (event.getAction() === MotionEvent.ACTION_UP) {
                    if (chip8Cycle.getChip8SOC().waitState) {
                        chip8Cycle.getChip8SOC().waitState = false
                        chip8Cycle.getChip8SOC().sendKeyStroke(currentKey)
                    }
                    chip8Cycle.getChip8SOC().keyPad[currentKey] = false
                }

                false
            }
        }
        println("density: "+applicationContext.getResources().getDisplayMetrics().density)
    }

//    var REQUEST_CODE: Int = 1
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
//            if(data == null){
//                return
//            }
//            //var context: Context = applicationContext
//            val uri: Uri? = data.data
//            if (uri != null) {
//                Toast.makeText(applicationContext, uri.path, Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//
//    fun openFileChooser(){
//        val intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "*/*"
//        startActivityFor
//    }
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            //Toast.makeText(applicationContext, uri.path, Toast.LENGTH_LONG).show()
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                chip8Cycle.loadROM(inputStream)
            }
        }

    }


    override fun onResume() {
        super.onResume()
        if(chip8Cycle.getRomStatus()){
            chip8Cycle.startEmulation()
        }
    }
    override fun onPause() {
        super.onPause()
        chip8Cycle.stopEmulation()
    }

    override fun onDestroy() {
        super.onDestroy()
        chip8Cycle.stopEmulation()
        chip8Cycle.getChip8SOC().closeSound()
    }

    fun openLoadROMIntent(view: View){
//        if (checkPermission()){
//            Log.d(TAG, "onCreate: Permission already granted, create folder")
//            val file = File(Environment.getExternalStorageDirectory(), "nyancat (1).ch8")
//            chip8Cycle.loadROM(file)
//        }
//        else{
//            Log.d(TAG, "onCreate: Permission was not granted, request")
//            requestPermission()
//        }
        getContent.launch("*/*")
    }

//    private fun checkPermission(): Boolean{
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
//            //Android is 11(R) or above
//            Environment.isExternalStorageManager()
//        }
//        else{
//            //Android is below 11(R)
//            val write = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            val read = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
//            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
//        }
//    }

//    private fun requestPermission(){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
//            //Android is 11(R) or above
//            try {
//                Log.d(TAG, "requestPermission: try")
//                val intent = Intent()
//                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
//                val uri = Uri.fromParts("package", this.packageName, null)
//                intent.data = uri
//                storageActivityResultLauncher.launch(intent)
//            }
//            catch (e: Exception){
//                Log.e(TAG, "requestPermission: ", e)
//                val intent = Intent()
//                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
//                storageActivityResultLauncher.launch(intent)
//            }
//        }
//        else{
//            //Android is below 11(R)
//            ActivityCompat.requestPermissions(this,
//                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
//                STORAGE_PERMISSION_CODE
//            )
//        }
//    }

//    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
//        Log.d(TAG, "storageActivityResultLauncher: ")
//        //here we will handle the result of our intent
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
//            //Android is 11(R) or above
//            if (Environment.isExternalStorageManager()){
//                //Manage External Storage Permission is granted
//                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is granted")
//                //createFolder()
//            }
//            else{
//                //Manage External Storage Permission is denied....
//                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is denied....")
//                toast("Manage External Storage Permission is denied....")
//            }
//        }
//        else{
//            //Android is below 11(R)
//        }
//    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == STORAGE_PERMISSION_CODE){
//            if (grantResults.isNotEmpty()){
//                //check each permission if granted or not
//                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
//                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
//                if (write && read){
//                    //External Storage Permission granted
//                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission granted")
//                    //createFolder()
//                }
//                else{
//                    //External Storage Permission denied...
//                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission denied...")
//                    toast("External Storage Permission denied...")
//                }
//            }
//        }
//    }


//    private fun toast(message: String){
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }

    fun pauseEmulation(view: View){
       if(chip8Cycle.getRomStatus() && chip8Cycle.getIsRunning()){
           chip8Cycle.stopEmulation()
       }else if(chip8Cycle.getRomStatus() && !chip8Cycle.getIsRunning()){
           chip8Cycle.startEmulation()
       }
    }

    class Chip8Cycle: Runnable {
        private val BITMAP_WIDTH = 128
        private val BITMAP_HEIGHT = 64
        private var isRunning: Boolean = false
        private var romStatus: Boolean = false
        private var chip8SOC: Chip8SOC
        private var planeColors: Array<Color>
        private var cpuCycleThread: Thread? = null
        private var last: LastFrame? = null
        private var bitmap: Bitmap
        private var chip8Surface: SurfaceView
        private var chip8SurfaceHolder: SurfaceHolder
        private var lowResRect: Rect
        private var hiResRect: Rect
        private var applicationContext: Context


        private lateinit var rom: InputStream
        constructor(applicationContext: Context, planeColors: Array<Color>, chip8Surface: SurfaceView){
            this.applicationContext = applicationContext
            this.chip8SOC = Chip8SOC(true, MachineType.XO_CHIP)
            chip8SOC.enableSound()
            this.planeColors = planeColors
            this.chip8Surface = chip8Surface
            this.chip8SurfaceHolder = chip8Surface.holder
            val callback: Chip8SurfaceCallBack = Chip8SurfaceCallBack()
            chip8SurfaceHolder.addCallback(callback)
            var scalingFactor: Int = 2
            var screenWidth = 0
            var screenHeight = 0

            if (Build.VERSION.SDK_INT < 30) {
                screenHeight = applicationContext.getResources().getDisplayMetrics().heightPixels
                screenWidth = applicationContext.getResources().getDisplayMetrics().widthPixels
            } else {
                val deviceWindowMetrics: WindowMetrics =
                    applicationContext.getSystemService<WindowManager>(
                        WindowManager::class.java
                    ).getMaximumWindowMetrics()
                screenWidth = deviceWindowMetrics.bounds.width()
                screenHeight = deviceWindowMetrics.bounds.height()
            }
            println(screenWidth)
            while(((64 * (scalingFactor+2)) <= screenWidth )){
                scalingFactor+=2
            }
            println("width: " + screenWidth)
            println("height: " + screenHeight)
            println("new scaling factor " + scalingFactor)
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
        }

        fun loadROM(rom: InputStream) {
            try {
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

                    Toast.makeText(applicationContext,"No ROM has been loaded into the emulator! Please load a ROM and try again.", Toast.LENGTH_LONG).show()
                }
            } catch (ioe: IOException) {
                romStatus = false
                Toast.makeText(applicationContext,"There was a problem loading the ROM file:$ioe",Toast.LENGTH_LONG).show()
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
                            try {
                                chip8SOC.cpuExec()
                            } catch (ex: Exception) {
                                stopEmulation()
                                ex.printStackTrace()
                                break
                            }
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
                if(chip8SOC.hiRes){
                    canvas.drawBitmap(bitmap, null, hiResRect, null)
                }else if(!chip8SOC.hiRes){
                    canvas.drawBitmap(bitmap, null, lowResRect, null)
                }

                holder.unlockCanvasAndPost(canvas)
            }

        }

        public fun getRomStatus() : Boolean{
            return romStatus
        }

        public fun getChip8SOC(): Chip8SOC{
            return chip8SOC
        }

        public fun getIsRunning(): Boolean{
            return isRunning
        }
    }

    class Chip8SurfaceCallBack: SurfaceHolder.Callback{

        override fun surfaceCreated(holder: SurfaceHolder) {
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

        }


        override fun surfaceDestroyed(holder: SurfaceHolder) {
        }

    }
}
