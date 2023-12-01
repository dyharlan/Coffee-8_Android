package com.dyharlan.coffee_8

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dyharlan.coffee_8.Backend.Chip8SOC
import com.dyharlan.coffee_8.Backend.MachineType
import com.google.android.material.appbar.MaterialToolbar





class MainActivity : AppCompatActivity() {
    //global variables representing the color palette, backend cpu, shared preferences
    private lateinit var planeColors: Array<Color>
    private lateinit var chip8Cycle: Chip8Cycle
    private val sharedPrefFile = "prefFile"
    private lateinit var sharedPreferences: SharedPreferences
    @SuppressLint("ClickableViewAccessibility")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        planeColors = arrayOf(
            Color.valueOf(0x000000),
            Color.valueOf(0xAA0000),
            Color.valueOf(0x00AA00),
            Color.valueOf(0x0000AA),
            Color.valueOf(0x00AAAA),
            Color.valueOf(0xAA00AA),
            Color.valueOf(0xAA5500),
            Color.valueOf(0xAAAAAA),
            Color.valueOf(0x555555),
            Color.valueOf(0xFF5555), //LR
            Color.valueOf(0x55FF55), //LG
            Color.valueOf(0x5555FF), //LB
            Color.valueOf(0x55FFFF), //LC
            Color.valueOf(0xFF55FF),
            Color.valueOf(0xFFFF55),
            Color.valueOf(0xFFFFFF)
        )
        setContentView(R.layout.activity_main)


        val chip8Surface = findViewById<SurfaceView>(R.id.chip8Surface)
        //setup toolbar?
        val toolbar = findViewById<MaterialToolbar>(R.id.materialToolbar)
        setSupportActionBar(toolbar)

        //set cycle count to 200 as a default
        val cycleCount: Int = sharedPreferences.getInt("cycles", 200)

        //set machine type to XO-CHIP as a default
        val machineType: String? = sharedPreferences.getString("machineType", MachineType.XO_CHIP.machineName)

        //Otherwise, set to cosmac vip or super-chip if the setting exists in the shared preferences.
        val currentMachine = if(machineType?.equals(MachineType.COSMAC_VIP.machineName) == true){
            MachineType.COSMAC_VIP
        }else if(machineType?.equals(MachineType.SUPERCHIP_1_1.machineName) == true){
            MachineType.SUPERCHIP_1_1
        }else{
            MachineType.XO_CHIP
        }

        //instantiate cpu
        chip8Cycle = Chip8Cycle(applicationContext, planeColors, chip8Surface, currentMachine)

        //apply cycle count
        if(cycleCount == 200){
            chip8Cycle.cycles = 200
        }else{
            chip8Cycle.cycles = cycleCount
        }
        /*
        * Programmatically setup the keypad dimensions and the event handling
         */
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
        //setup event handler by looping over all buttons
        for((currentKey, key) in keyPad.withIndex()){
            key.text = keyLabels[currentKey]
            key.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f)
            key.setOnTouchListener { _, event ->
//                if (event.action == MotionEvent.ACTION_DOWN) {
//                    chip8Cycle.keyPress(currentKey)
//                    true
//                } else if (event.action == MotionEvent.ACTION_CANCEL) {
//                    chip8Cycle.keyRelease(currentKey)
//
//                    true
//                } else if (event.action == MotionEvent.ACTION_UP) {
//                    chip8Cycle.keyRelease(currentKey)
//                    true
//                }
//                else false

                when(event.action){
                    MotionEvent.ACTION_DOWN -> {
                        chip8Cycle.keyPress(currentKey)
                        true}
                    MotionEvent.ACTION_CANCEL -> {
                        chip8Cycle.keyRelease(currentKey)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        chip8Cycle.keyRelease(currentKey)
                        true
                    }
                    else -> false
                }

            }


        }
        //Log.i("onCreate","density: "+applicationContext.getResources().getDisplayMetrics().density)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activitybar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuReset -> resetButton()
            R.id.menuPause -> pauseEmulation()
            R.id.menuLoad -> openLoadROMIntent()
            R.id.menuSet -> showCyclesButton()
            R.id.menuChange -> showMachineTypeButton()
        }
        return super.onOptionsItemSelected(item)
    }

    //helper functions for various settings related to the emulated machine
    fun showCyclesButton(){
        showCyclesDialog(chip8Cycle)
    }
    fun showMachineTypeButton(){
        showMachineTypeSelectorDialog(chip8Cycle)
    }
    fun resetButton(){
        chip8Cycle.resetROM()
    }
    /*
     * show a dialog that allows the user to change machine cycle count
     */
    private fun showCyclesDialog(chip8SOC: Chip8SOC) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.cycles_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = "Set the number of Cycles done by the emulator. Higher values might slow down performance."
        val editText = dialog.findViewById<EditText>(R.id.newCycles)
        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            //retrieve value from the text box
            val newCycles = editText.text.toString().toInt()
            //re-set the cycle count if entered value is positive
            if(newCycles >= 0){
                chip8SOC.cycles = newCycles
                Toast.makeText(this, "Cycles: $newCycles", Toast.LENGTH_SHORT).show()
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.putInt("cycles", newCycles)
                editor.apply()
                editor.commit()
            }
            else{
                Toast.makeText(this, "Please enter a positive value and try again!", Toast.LENGTH_LONG).show()
            }

            dialog.dismiss()
        }
        //exit from dialog
        btnNo.setOnClickListener {
            Toast.makeText(this, "Cycles: ${chip8SOC.cycles}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }
    //Sets up an activity contract for loading the rom using SAF
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val fileDescriptor = contentResolver.openAssetFileDescriptor(uri, "r")
                if(chip8Cycle.checkROMSize(fileDescriptor)){
                    chip8Cycle.openROM(inputStream)
                }else{
                    Toast.makeText(this,"Rom is too large for ${chip8Cycle.currentMachine.machineName}!",Toast.LENGTH_LONG).show()
                }
                inputStream.close()
            }
        }

    }
    fun openLoadROMIntent(){
        getContent.launch("*/*")
    }


    private fun showMachineTypeSelectorDialog(chip8Cycle: Chip8Cycle){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.machinetype_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = "Select the Chip-8 Variant to emulate."


        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)
        val machineRgp = dialog.findViewById<RadioGroup>(R.id.machineGroup)
        btnYes.setOnClickListener {
            var newMachine: MachineType? = null
            val selectedId: Int = machineRgp.checkedRadioButtonId
            Log.i("showMachineTypeSelectorDialog","selected: $selectedId")

            // find the radiobutton by returned id
            if(selectedId == R.id.COSMACradioButton){
                if(chip8Cycle.getRomStatus() && !chip8Cycle.checkROMSize(chip8Cycle.romSize, MachineType.COSMAC_VIP)){
                    Toast.makeText(applicationContext,"Rom is too large for ${MachineType.COSMAC_VIP.machineName}!",Toast.LENGTH_LONG).show()
                }else{
                    newMachine = MachineType.COSMAC_VIP
                }
            }else if(selectedId == R.id.SCHIPradioButton){
                if(chip8Cycle.getRomStatus() && !chip8Cycle.checkROMSize(chip8Cycle.romSize, MachineType.SUPERCHIP_1_1)){
                    Toast.makeText(applicationContext,"Rom is too large for ${MachineType.SUPERCHIP_1_1.machineName}!",Toast.LENGTH_LONG).show()
                }else{
                    newMachine = MachineType.SUPERCHIP_1_1
                }
            }else if(selectedId == R.id.XOCHIPradioButton){
                if(chip8Cycle.getRomStatus() && !chip8Cycle.checkROMSize(chip8Cycle.romSize, MachineType.XO_CHIP)){
                    Toast.makeText(applicationContext,"Rom is too large for ${MachineType.XO_CHIP.machineName}!",Toast.LENGTH_LONG).show()
                }else{
                    newMachine = MachineType.XO_CHIP
                }
            }
            Log.i("showMachineTypeSelectorDialog","is new machine null? ${newMachine == null}")
            if(newMachine != null && (chip8Cycle.currentMachine == newMachine)){
                Toast.makeText(this, "Machine: ${chip8Cycle.currentMachine.machineName}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }else if(chip8Cycle.getRomStatus() && newMachine != null){
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                chip8Cycle.currentMachine = newMachine
                editor.putString("machineType", newMachine.machineName)
                editor.apply()
                editor.commit()
                chip8Cycle.resetROM()
                Toast.makeText(this, "Machine: ${chip8Cycle.currentMachine.machineName}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }else if(!chip8Cycle.getRomStatus() && newMachine != null){
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                chip8Cycle.currentMachine = newMachine
                editor.putString("machineType", newMachine.machineName)
                editor.apply()
                editor.commit()
                Toast.makeText(this, "Machine: ${chip8Cycle.currentMachine.machineName}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

        }

        btnNo.setOnClickListener {
            Toast.makeText(this, "Machine: ${chip8Cycle.currentMachine.machineName}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
        if(chip8Cycle.currentMachine == MachineType.COSMAC_VIP){
            val btn = dialog.findViewById<RadioButton>(R.id.COSMACradioButton)
            btn.isChecked = true
        }else if(chip8Cycle.currentMachine == MachineType.SUPERCHIP_1_1){
            val btn = dialog.findViewById<RadioButton>(R.id.SCHIPradioButton)
            btn.isChecked = true
        }else if(chip8Cycle.currentMachine == MachineType.XO_CHIP){
            val btn = dialog.findViewById<RadioButton>(R.id.XOCHIPradioButton)
            btn.isChecked = true
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

    fun pauseEmulation(){
        if(chip8Cycle.getRomStatus() && chip8Cycle.getIsRunning()){
            chip8Cycle.stopEmulation()
        }else if(chip8Cycle.getRomStatus() && !chip8Cycle.getIsRunning()){
            chip8Cycle.startEmulation()
        }else if(!chip8Cycle.getRomStatus() && !chip8Cycle.getIsRunning()){
            Toast.makeText(this, "Machine is not running!", Toast.LENGTH_SHORT).show()
        }
    }


}
