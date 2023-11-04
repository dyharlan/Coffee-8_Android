package com.dyharlan.coffee_8

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.Window

import android.widget.Button
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dyharlan.coffee_8.Backend.Chip8SOC


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
        val keyRow1 = findViewById<TableRow>(R.id.keyRow1)
            keyRow1.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0, 1f)
        val keyRow2 = findViewById<TableRow>(R.id.keyRow2)
            keyRow2.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0, 1f)
        val keyRow3 = findViewById<TableRow>(R.id.keyRow3)
            keyRow3.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0, 1f)
        val keyRow4 = findViewById<TableRow>(R.id.keyRow4)
            keyRow4.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0, 1f)
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
            key.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f)
            key.setOnTouchListener { v, event ->
                if (chip8Cycle.getChip8SOC().keyPad == null) {
                    false
                }else if (event.getAction() === MotionEvent.ACTION_DOWN) {
                    chip8Cycle.getChip8SOC().keyPad[currentKey] = true
                    true
                } else if (event.getAction() === MotionEvent.ACTION_CANCEL) {
                    if (chip8Cycle.getChip8SOC().waitState) {
                        chip8Cycle.getChip8SOC().waitState = false
                        chip8Cycle.getChip8SOC().sendKeyStroke(currentKey)
                    }
                    chip8Cycle.getChip8SOC().keyPad[currentKey] = false
                    true
                } else if (event.getAction() === MotionEvent.ACTION_UP) {
                    if (chip8Cycle.getChip8SOC().waitState) {
                        chip8Cycle.getChip8SOC().waitState = false
                        chip8Cycle.getChip8SOC().sendKeyStroke(currentKey)
                    }
                    chip8Cycle.getChip8SOC().keyPad[currentKey] = false
                    true
                }

                else false
            }
        }
        println("density: "+applicationContext.getResources().getDisplayMetrics().density)
    }
    fun showCyclesButton(view: View){
        if(chip8Cycle != null && chip8Cycle.getChip8SOC() != null){
            showCyclesDialog(chip8Cycle.getChip8SOC())
        }
    }
    private fun showCyclesDialog(chip8SOC: Chip8SOC) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = "Set the number of Cycles done by the emulator"
        val editText = dialog.findViewById<EditText>(R.id.newCycles)
        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            if(chip8SOC != null){
                var newCycles = editText.text.toString().toInt()
                if(newCycles >= 0){
                    chip8SOC.cycles = newCycles
                    Toast.makeText(this, "Cycles: $newCycles", Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(this, "Please enter a positive value and try again!", Toast.LENGTH_LONG).show()
                }

            }
            dialog.dismiss()
        }
        btnNo.setOnClickListener {
            Toast.makeText(this, "Cycles: ${chip8SOC.cycles}", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }
        dialog.show()
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
    fun pauseEmulation(view: View){
        if(chip8Cycle.getRomStatus() && chip8Cycle.getIsRunning()){
            chip8Cycle.stopEmulation()
        }else if(chip8Cycle.getRomStatus() && !chip8Cycle.getIsRunning()){
            chip8Cycle.startEmulation()
        }
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





//    class Chip8SurfaceCallBack: SurfaceHolder.Callback{
//
//        override fun surfaceCreated(holder: SurfaceHolder) {
//        }
//
//        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//
//        }
//
//
//        override fun surfaceDestroyed(holder: SurfaceHolder) {
//        }
//
//    }
}
