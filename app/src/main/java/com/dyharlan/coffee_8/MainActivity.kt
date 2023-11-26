package com.dyharlan.coffee_8

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
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
    private val sharedPrefFile = "prefFile"
    var sharedPreferences: SharedPreferences? = null
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
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

        if (sharedPreferences != null) {
            val cycleCount: Int = sharedPreferences!!.getInt("cycles", 200)
            if(cycleCount.equals(200)){
                chip8Cycle.cycles = 200
            }else{
                chip8Cycle.cycles = cycleCount
            }
        }
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
                if (chip8Cycle.keyPad == null) {
                    false
                }else if (event.getAction() === MotionEvent.ACTION_DOWN) {
                    chip8Cycle.keyPad[currentKey] = true
                    true
                } else if (event.getAction() === MotionEvent.ACTION_CANCEL) {
                    if (chip8Cycle.waitState) {
                        chip8Cycle.waitState = false
                        chip8Cycle.sendKeyStroke(currentKey)
                    }
                    chip8Cycle.keyPad[currentKey] = false
                    true
                } else if (event.getAction() === MotionEvent.ACTION_UP) {
                    if (chip8Cycle.waitState) {
                        chip8Cycle.waitState = false
                        chip8Cycle.sendKeyStroke(currentKey)
                    }
                    chip8Cycle.keyPad[currentKey] = false
                    true
                }

                else false
            }
        }
        println("density: "+applicationContext.getResources().getDisplayMetrics().density)
    }
    fun showCyclesButton(view: View){
        if(chip8Cycle != null){
            showCyclesDialog(chip8Cycle)
        }
    }
    fun resetButton(view: View){
        if(chip8Cycle != null){
            chip8Cycle.resetROM()
        }
    }
    private fun showCyclesDialog(chip8SOC: Chip8SOC) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = "Set the number of Cycles done by the emulator. Higher values might slow down performance."
        val editText = dialog.findViewById<EditText>(R.id.newCycles)
        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            if(chip8SOC != null && sharedPreferences != null){
                var newCycles = editText.text.toString().toInt()
                if(newCycles >= 0){
                    chip8SOC.cycles = newCycles
                    Toast.makeText(this, "Cycles: $newCycles", Toast.LENGTH_LONG).show()
                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                    editor.putInt("cycles", newCycles)
                    editor.apply()
                    editor.commit()
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

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            //Toast.makeText(applicationContext, uri.path, Toast.LENGTH_LONG).show()
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {

                chip8Cycle.openROM(inputStream)
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
        chip8Cycle.closeSound()
    }

    fun openLoadROMIntent(view: View){
        getContent.launch("*/*")
    }
    fun pauseEmulation(view: View){
        if(chip8Cycle.getRomStatus() && chip8Cycle.getIsRunning()){
            chip8Cycle.stopEmulation()
        }else if(chip8Cycle.getRomStatus() && !chip8Cycle.getIsRunning()){
            chip8Cycle.startEmulation()
        }
    }


}
