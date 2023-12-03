package com.dyharlan.coffee_8

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteException
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dyharlan.coffee_8.Backend.Chip8SOC
import com.dyharlan.coffee_8.Backend.MachineType
import com.google.android.material.appbar.MaterialToolbar
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.CRC32


class MainActivity : AppCompatActivity() {
    //global variables representing the color palette, backend cpu, shared preferences
    private var menu: Menu? = null
    private lateinit var planeColors: Array<Color>
    private lateinit var chip8Cycle: Chip8Cycle
    private val sharedPrefFile = "prefFile"
    private lateinit var sharedPreferences: SharedPreferences
    val crc32: CRC32 = CRC32()
    init {
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
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        setContentView(R.layout.activity_main)


        val chip8Surface = findViewById<SurfaceView>(R.id.chip8Surface)
        //setup toolbar?
        val toolbar = findViewById<MaterialToolbar>(R.id.materialToolbar)
        setSupportActionBar(toolbar)

        //set cycle count to 200 as a default
        //val cycleCount: Int = sharedPreferences.getInt("cycles", -1)

        //set machine type to XO-CHIP as a default
        //val machineType: String? = sharedPreferences.getString("machineType", MachineType.XO_CHIP.machineName)

        //Otherwise, set to cosmac vip or super-chip if the setting exists in the shared preferences.
//        val currentMachine = if(machineType?.equals(MachineType.COSMAC_VIP.machineName) == true){
//            MachineType.COSMAC_VIP
//        }else if(machineType?.equals(MachineType.SUPERCHIP_1_1.machineName) == true){
//            MachineType.SUPERCHIP_1_1
//        }else{
//            MachineType.XO_CHIP
//        }

        //instantiate cpu
        chip8Cycle = Chip8Cycle(applicationContext, planeColors, chip8Surface)

        //apply cycle count
//        if(cycleCount == 200){
//            chip8Cycle.cycles = 200
//        }else{
//            chip8Cycle.cycles = cycleCount
//        }
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
        this.menu = menu;
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuReset -> resetButton(chip8Cycle)
            R.id.menuPause -> pauseEmulation(chip8Cycle)
            R.id.menuLoad -> openLoadROMIntent()
            R.id.menuSet -> showCyclesButton(chip8Cycle)
            R.id.menuChange -> showMachineTypeButton(chip8Cycle)
        }
        this.menu = menu;
        return super.onOptionsItemSelected(item)
    }

    //helper functions for various settings related to the emulated machine
    fun showCyclesButton(chip8Cycle: Chip8Cycle){
        if(chip8Cycle.getRomStatus())
            showCyclesDialog(chip8Cycle)
        else
            Toast.makeText(this, "Machine is not running!", Toast.LENGTH_SHORT).show()
    }
    fun showMachineTypeButton(chip8Cycle: Chip8Cycle){
        if(chip8Cycle.getRomStatus())
            showMachineTypeSelectorDialog(chip8Cycle)
        else
            Toast.makeText(this, "Machine is not running!", Toast.LENGTH_SHORT).show()
    }
    fun resetButton(chip8Cycle: Chip8Cycle){
        chip8Cycle.resetROM()
    }
    /*
     * show a dialog that allows the user to change machine cycle count
     */
    private fun showCyclesDialog(chip8SOC: Chip8SOC)  {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.cycles_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = resources.getString(R.string.cycles_dialog_title_text_en)

        val editText = dialog.findViewById<EditText>(R.id.newCycles)
        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            //retrieve value from the text box
            var newCycles = 0
            try{
                newCycles = editText.text.toString().toInt()
            }catch(nfe: NumberFormatException){
                Toast.makeText(this, "Please enter a numerical value and try again!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            //re-set the cycle count if entered value is positive
            if(newCycles >= 0){
                chip8SOC.cycles = newCycles
                Toast.makeText(this, "Cycles: $newCycles", Toast.LENGTH_SHORT).show()
//                val editor: SharedPreferences.Editor = sharedPreferences.edit()
//                editor.putInt("cycles", newCycles)
//                editor.apply()
//                editor.commit()
                val dbHandler: DatabaseHandler = DatabaseHandler(this)
                dbHandler.saveConfigs(RomConfigClass(crc32.value, chip8Cycle.currentMachine, newCycles))
                dbHandler.close()
                dialog.dismiss()
            }
            else{
                Toast.makeText(this, "Please enter a positive value and try again!", Toast.LENGTH_LONG).show()
            }

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
                if (fileDescriptor != null) {
                    Log.i("","size: ${fileDescriptor.length}")
                    if(fileDescriptor.length <= 65024L){

                        var currByte = 0
                        crc32.reset()
                        val romArray = ArrayList<Int>()
                        try{
                            val input = DataInputStream(BufferedInputStream(inputStream))
                            while(currByte != -1){
                                currByte = input.read()
                                if(currByte!= -1){
                                    crc32.update(currByte and 0xFF)
                                    romArray.add(currByte)
                                }
                            }
                            input.close()
                            inputStream.close()
                        }catch(ioe: IOException){
                            Toast.makeText(this, "An error occurred while loading the ROM: ${ioe.toString()}", Toast.LENGTH_SHORT).show()
                            Log.e("initialSetupDialog", "An error occurred while loading the ROM: ${ioe.toString()}")
                            return@registerForActivityResult
                        }
                        val dbHandler = DatabaseHandler(this)
                        val romConfig = dbHandler.loadConfigs(crc32.value)
                        dbHandler.close()
                        if(romConfig.machineType == MachineType.NONE && romConfig.cycles < 0){
                            showInitialSetupDialog(romArray)
                        }else{
                            chip8Cycle.cycles = romConfig.cycles
                            chip8Cycle.currentMachine = romConfig.machineType
//                            if(chip8Cycle.getRomStatus())
//                                chip8Cycle.closeROM()
                            chip8Cycle.openROM(romArray,crc32.value)
                            chip8Cycle.resetROM()

                        }
                    }else{
                        Toast.makeText(this,"Rom is too large for the emulator! Roms must be 65024 bytes or less.",Toast.LENGTH_LONG).show()
                    }
                }
                inputStream.close()
            }
        }

    }
    fun openLoadROMIntent(){
        getContent.launch("*/*")
    }

    fun showInitialSetupDialog(romArray: ArrayList<Int>){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.initial_setup_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = resources.getString(R.string.machine_type_dialog_title_text_en)
        val dialogSubTitle = dialog.findViewById<TextView>(R.id.dialogSubTitle)
        dialogSubTitle.text = resources.getString(R.string.cycles_dialog_title_text_en)



        val btnStart = dialog.findViewById<Button>(R.id.btnStart)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val editText = dialog.findViewById<EditText>(R.id.newCycles)
        val machineRgp = dialog.findViewById<RadioGroup>(R.id.machineGroup)

        btnStart.setOnClickListener {
            //retrieve value from the text box
            var newCycles = 0
            try{
                newCycles = editText.text.toString().toInt()
            }catch(nfe: NumberFormatException){
                Toast.makeText(this, "Please enter a numerical value and try again!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            //re-set the cycle count if entered value is positive
            var newMachine: MachineType? = null
            val selectedId: Int = machineRgp.checkedRadioButtonId
            if(selectedId == R.id.COSMACradioButton){
                if(!chip8Cycle.checkROMSize(romArray.size, MachineType.COSMAC_VIP)){
                    Toast.makeText(applicationContext,"Rom is too large for ${MachineType.COSMAC_VIP.machineName}!",Toast.LENGTH_LONG).show()
                }else{
                    newMachine = MachineType.COSMAC_VIP
                }
            }else if(selectedId == R.id.SCHIPradioButton){
                if(!chip8Cycle.checkROMSize(romArray.size, MachineType.SUPERCHIP_1_1)){
                    Toast.makeText(applicationContext,"Rom is too large for ${MachineType.SUPERCHIP_1_1.machineName}!",Toast.LENGTH_LONG).show()
                }else{
                    newMachine = MachineType.SUPERCHIP_1_1
                }
            }else if(selectedId == R.id.XOCHIPradioButton){
                if(!chip8Cycle.checkROMSize(romArray.size, MachineType.XO_CHIP)){
                    Toast.makeText(applicationContext,"Rom is too large for ${MachineType.XO_CHIP.machineName}!",Toast.LENGTH_LONG).show()
                }else{
                    newMachine = MachineType.XO_CHIP
                }
            }else if(selectedId == -1){
                newMachine = null
            }
            if(newCycles >= 0 && newMachine != null){
                chip8Cycle.cycles = newCycles
                chip8Cycle.currentMachine = newMachine
                Toast.makeText(this, "Cycles: $newCycles", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Machine Type: $newMachine", Toast.LENGTH_SHORT).show()
                val dbHandler: DatabaseHandler = DatabaseHandler(this)
                var status: Boolean = try{
                    dbHandler.saveConfigs(RomConfigClass(crc32.value, newMachine, newCycles))
                }catch (sqle: SQLiteException){
                    Toast.makeText(this, "An error occurred while loading the ROM: $sqle", Toast.LENGTH_SHORT).show()
                    Log.e("showInitialSetupDialog", sqle.toString())
                    false
                }finally {
                    dbHandler.close()
                }
                println("status: $status")
                if(status){
//                    if(chip8Cycle.getRomStatus())
//                        chip8Cycle.closeROM()
                    chip8Cycle.openROM(romArray,crc32.value)
                    chip8Cycle.resetROM()
                }
                dialog.dismiss()
            }
            if(newCycles < 0){
                Toast.makeText(this, "Please enter a positive value and try again!", Toast.LENGTH_SHORT).show()
            }
            if(newMachine == null){
                Toast.makeText(this, "Please enter a machine to emulate!", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
        if(romArray.size > 3232L){
            val COSMACradioButton = dialog.findViewById<RadioButton>(R.id.COSMACradioButton)
            COSMACradioButton.isEnabled = false
        }
        if(romArray.size > 3583L){
            val SCHIPradioButton = dialog.findViewById<RadioButton>(R.id.SCHIPradioButton)
            SCHIPradioButton.isEnabled = false
        }
        if(romArray.size > 65024L){
            val XOCHIPradioButton = dialog.findViewById<RadioButton>(R.id.XOCHIPradioButton)
            XOCHIPradioButton.isEnabled = false
        }
    }
    private fun showMachineTypeSelectorDialog(chip8Cycle: Chip8Cycle){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.machinetype_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = resources.getString(R.string.machine_type_dialog_title_text_en)

        val romStatus = chip8Cycle.getRomStatus()


        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)
        val machineRgp = dialog.findViewById<RadioGroup>(R.id.machineGroup)
        btnYes.setOnClickListener {
            var newMachine: MachineType? = null
            val selectedId: Int = machineRgp.checkedRadioButtonId
            Log.i("showMachineTypeSelectorDialog","selected: $selectedId")

            // find the radiobutton by returned id
            if(selectedId == R.id.COSMACradioButton){
                if(romStatus && !chip8Cycle.checkROMSize(chip8Cycle.getRomSize(), MachineType.COSMAC_VIP)){
                    Toast.makeText(applicationContext,"Rom is too large for ${MachineType.COSMAC_VIP.machineName}!",Toast.LENGTH_LONG).show()
                }else{
                    newMachine = MachineType.COSMAC_VIP
                }
            }else if(selectedId == R.id.SCHIPradioButton){
                if(romStatus && !chip8Cycle.checkROMSize(chip8Cycle.getRomSize(), MachineType.SUPERCHIP_1_1)){
                    Toast.makeText(applicationContext,"Rom is too large for ${MachineType.SUPERCHIP_1_1.machineName}!",Toast.LENGTH_LONG).show()
                }else{
                    newMachine = MachineType.SUPERCHIP_1_1
                }
            }else if(selectedId == R.id.XOCHIPradioButton){
                if(romStatus && !chip8Cycle.checkROMSize(chip8Cycle.getRomSize(), MachineType.XO_CHIP)){
                    Toast.makeText(applicationContext,"Rom is too large for ${MachineType.XO_CHIP.machineName}!",Toast.LENGTH_LONG).show()
                }else{
                    newMachine = MachineType.XO_CHIP
                }
            }
            Log.i("showMachineTypeSelectorDialog","is new machine null? ${newMachine == null}")
            if(newMachine != null && (chip8Cycle.currentMachine == newMachine)){
                Toast.makeText(this, "Machine: ${chip8Cycle.currentMachine.machineName}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }else if(romStatus && newMachine != null){
                //val editor: SharedPreferences.Editor = sharedPreferences.edit()
                chip8Cycle.currentMachine = newMachine
//                editor.putString("machineType", newMachine.machineName)
//                editor.apply()
//                editor.commit()
                val dbHandler: DatabaseHandler = DatabaseHandler(this)
                dbHandler.saveConfigs(RomConfigClass(crc32.value, newMachine, chip8Cycle.cycles))
                dbHandler.close()
                chip8Cycle.resetROM()
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
        val romSize = chip8Cycle.getRomSize()
        if(romSize > 3232L){
            val COSMACradioButton = dialog.findViewById<RadioButton>(R.id.COSMACradioButton)
            COSMACradioButton.isEnabled = false
        }
        if(romSize > 3583L){
            val SCHIPradioButton = dialog.findViewById<RadioButton>(R.id.SCHIPradioButton)
            SCHIPradioButton.isEnabled = false
        }
        if(romSize > 65024L){
            val XOCHIPradioButton = dialog.findViewById<RadioButton>(R.id.XOCHIPradioButton)
            XOCHIPradioButton.isEnabled = false
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

    fun pauseEmulation(chip8Cycle: Chip8Cycle){
        var status = menu?.findItem(R.id.menuPause)
        if(chip8Cycle.getRomStatus() && chip8Cycle.getIsRunning()){
            chip8Cycle.stopEmulation()
            status?.setTitle("Resume")
        }else if(chip8Cycle.getRomStatus() && !chip8Cycle.getIsRunning()){
            chip8Cycle.startEmulation()
            status?.setTitle("Pause")
        }else if(!chip8Cycle.getRomStatus() && !chip8Cycle.getIsRunning()){
            Toast.makeText(this, "Machine is not running!", Toast.LENGTH_SHORT).show()
            status?.setTitle("Pause")
        }
    }


}
