package com.dyharlan.coffee_8
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteException
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dyharlan.coffee_8.Backend.Chip8SOC
import com.dyharlan.coffee_8.Backend.MachineType
import com.google.android.material.appbar.MaterialToolbar
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.util.zip.CRC32


class MainActivity : AppCompatActivity() {
    //global variables representing the color palette, backend cpu, shared preferences
    private var menu: Menu? = null
    private var planeColors: IntArray = intArrayOf(
        0x000000,
        0xAA0000,
        0x00AA00,
        0x0000AA,
        0x00AAAA,
        0xAA00AA,
        0xAA5500,
        0xAAAAAA,
        0x555555,
        0xFF5555, //LR
        0x55FF55, //LG
        0x5555FF, //LB
        0x55FFFF, //LC
        0xFF55FF,
        0xFFFF55,
        0xFFFFFF
    )
//    private var physicalKeys: IntArray = intArrayOf(
//        KeyEvent.KEYCODE_X,
//        KeyEvent.KEYCODE_1,
//        KeyEvent.KEYCODE_2,
//        KeyEvent.KEYCODE_3,
//        KeyEvent.KEYCODE_Q,
//        KeyEvent.KEYCODE_W,
//        KeyEvent.KEYCODE_E,
//        KeyEvent.KEYCODE_A,
//        KeyEvent.KEYCODE_S,
//        KeyEvent.KEYCODE_D,
//        KeyEvent.KEYCODE_Z,
//        KeyEvent.KEYCODE_C,
//        KeyEvent.KEYCODE_4,
//        KeyEvent.KEYCODE_R,
//        KeyEvent.KEYCODE_F,
//        KeyEvent.KEYCODE_V
//    )
    private var physicalKeys: IntArray = IntArray(16){0}
    private lateinit var chip8Cycle: Chip8Cycle
    private lateinit var keyPad: Array<Button>
    private val sharedPrefFile = "prefFile"
    private lateinit var sharedPreferences: SharedPreferences
    private val crc32: CRC32 = CRC32()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        setContentView(R.layout.activity_main)

        val chip8Surface = findViewById<Chip8SurfaceView>(R.id.chip8Surface)
        //setup toolbar?
        val toolbar = findViewById<MaterialToolbar>(R.id.materialToolbar)
        setSupportActionBar(toolbar)

        //instantiate cpu
        chip8Cycle = Chip8Cycle(applicationContext, planeColors, chip8Surface)

        val keys = resources.getStringArray(R.array.chip8_keys)

        physicalKeys[0] = sharedPreferences.getInt(keys[0], KeyEvent.KEYCODE_X)
        physicalKeys[1] = sharedPreferences.getInt(keys[1], KeyEvent.KEYCODE_1)
        physicalKeys[2] = sharedPreferences.getInt(keys[2], KeyEvent.KEYCODE_2)
        physicalKeys[3] = sharedPreferences.getInt(keys[3], KeyEvent.KEYCODE_3)

        physicalKeys[4] = sharedPreferences.getInt(keys[4], KeyEvent.KEYCODE_Q)
        physicalKeys[5] = sharedPreferences.getInt(keys[5], KeyEvent.KEYCODE_W)
        physicalKeys[6] = sharedPreferences.getInt(keys[6], KeyEvent.KEYCODE_E)
        physicalKeys[7] = sharedPreferences.getInt(keys[7], KeyEvent.KEYCODE_A)

        physicalKeys[8] = sharedPreferences.getInt(keys[8], KeyEvent.KEYCODE_S)
        physicalKeys[9] = sharedPreferences.getInt(keys[9], KeyEvent.KEYCODE_D)
        physicalKeys[10] = sharedPreferences.getInt(keys[10], KeyEvent.KEYCODE_Z)
        physicalKeys[11] = sharedPreferences.getInt(keys[11], KeyEvent.KEYCODE_C)

        physicalKeys[12] = sharedPreferences.getInt(keys[12], KeyEvent.KEYCODE_4)
        physicalKeys[13] = sharedPreferences.getInt(keys[13], KeyEvent.KEYCODE_R)
        physicalKeys[14] = sharedPreferences.getInt(keys[14], KeyEvent.KEYCODE_F)
        physicalKeys[15] = sharedPreferences.getInt(keys[15], KeyEvent.KEYCODE_V)



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
        keyPad = arrayOf(
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
        setButtonDayNightStyle(keyPad)
        //Log.i("onCreate","density: "+applicationContext.getResources().getDisplayMetrics().density)
    }
    private fun saveData(){
        val sharedPreferencesUI = getSharedPreferences("sharedPreferencesUI", Context.MODE_PRIVATE)
        val editor = sharedPreferencesUI.edit()
        editor.apply{
            putBoolean("BOOLEAN_KEY", dayTheme)
            putString("STRING_KEY", tempStatus)
        }.apply()

    }

    private fun loadData(theme: MenuItem?){
        val sharedPreferencesUI = getSharedPreferences("sharedPreferencesUI", Context.MODE_PRIVATE)
        val savedBoolean = sharedPreferencesUI.getBoolean("BOOLEAN_KEY", true)
        val savedString = sharedPreferencesUI.getString("STRING_KEY", "To Night Mode")
        dayTheme = savedBoolean
        theme?.setTitle(savedString)
        Log.d("tempString is: ", savedString.toString())
    }


    var count = 0
    var dayTheme:Boolean = false
    var tempStatus = ""

    private fun setButtonLayout(buttons: Array<Button>) {
        for (button in buttons) {
            val layoutParams = button.layoutParams as ViewGroup.MarginLayoutParams

            // Update the margins (change the values to your desired margins)
            layoutParams.leftMargin = 7
            layoutParams.topMargin = 7
            layoutParams.rightMargin = 7
            layoutParams.bottomMargin = 7
            // Set the updated layout parameters to the button
            button.layoutParams = layoutParams
            button.setTextColor(Color.BLACK)
        }
    }

    private fun setDayStyle(buttons: Array<Button>, actionBar: androidx.appcompat.app.ActionBar?){
        // Assuming you have a reference to your TableLayout
        dayTheme = true
        val tableLayout = findViewById<TableLayout>(R.id.keyPad)
        // Set the background color
        val colorRes = R.color.day_keypad_background
        val bgColor = ContextCompat.getColor(this, colorRes)
        tableLayout.setBackgroundColor(bgColor)
        supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.color.day_keypad_background))
        actionBar?.title?.let {
            val spannableTitle = SpannableString(it)
            spannableTitle.setSpan(
                ForegroundColorSpan(Color.BLACK), // Set the desired text color
                0, // Start index
                it.length, // End index
                0 // No flags
            )
            actionBar.title = spannableTitle
        }
        for (button in buttons) {
            if(count == 5 || count == 8 || count == 7 || count == 9){
                button.setBackgroundResource(R.drawable.rectangle_wasdbutton_background)
                Log.d("TEST: ", "Line Executed")
            }
            else{
                button.setBackgroundResource(R.drawable.rectangle_button_background)
            }
            count++
        }
    }

    private fun setNightStyle(buttons: Array<Button>,actionBar: androidx.appcompat.app.ActionBar?){
        supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.color.night_keypad_background))
        actionBar?.title?.let {
            val spannableTitle = SpannableString(it)
            spannableTitle.setSpan(
                ForegroundColorSpan(getResources().getColor(R.color.night_title)), // Set the desired text color
                0, // Start index
                it.length, // End index
                0 // No flags
            )
            actionBar.title = spannableTitle
        }
        // Assuming you have a reference to your TableLayout
        val tableLayout = findViewById<TableLayout>(R.id.keyPad)
        // Set the background color
        val colorRes = R.color.night_keypad_background
        val bgColor = ContextCompat.getColor(this, colorRes)
        tableLayout.setBackgroundColor(bgColor)
        for (button in buttons) {
            if(count == 5 || count == 8 || count == 7 || count == 9){
                button.setBackgroundResource(R.drawable.night_rectangle_wasdbutton_background)
            }
            else{
                button.setBackgroundResource(R.drawable.night_rectangle_button_background)
            }
            count++
        }
    }
    private fun setButtonDayNightStyle(buttons: Array<Button>) {
        val theme = menu?.findItem(R.id.menuTheme)
        val actionBar = supportActionBar
        loadData(theme)
        actionBar?.title = "Coffee-8"
        setButtonLayout(buttons)
        Log.d("STRING STATUS AFTER", tempStatus)

        if(dayTheme == true){
            setDayStyle(buttons,actionBar)
            var toPrint = theme?.title?.asSequence()
            println(toPrint)
        }
        else{
            setNightStyle(buttons,actionBar)
            var toPrint = theme?.title?.asSequence()
            println(toPrint)
        }
        count = 0
    }

    private fun setButtonDayNightStyleButtonListener(){
        if(dayTheme == true){
            dayTheme = false
            tempStatus = "To Day Mode"
            Log.d("STRING STATUS BEFORE", tempStatus)
            saveData()
            // Log.d("STRING STATUS", theme?.title.toString())
            setButtonDayNightStyle(keyPad)

        }
        else{
            dayTheme = true
            tempStatus = "To Night Mode"
            Log.d("STRING STATUS BEFORE", tempStatus)
            saveData()
            //Log.d("STRING STATUS", theme?.title.toString())
            setButtonDayNightStyle(keyPad)
        }
    }
//    companion object {
//        fun isExternal(inputDevice: InputDevice): Boolean {
//            if(Build.VERSION.SDK_INT >= 29){
//                return inputDevice.isExternal
//            }
//            return try {
//                val m = InputDevice::class.java.getMethod("isExternal")
//                m.invoke(inputDevice) as Boolean
//            } catch (e: NoSuchMethodException) {
//                e.printStackTrace()
//                false
//            } catch (e: IllegalAccessException) {
//                e.printStackTrace()
//                false
//            } catch (e: InvocationTargetException) {
//                e.printStackTrace()
//                false
//            }
//        }
//    }
override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
    var isHandled = false
    if(event == null){
        return isHandled
    }

    if(event.action == KeyEvent.ACTION_DOWN && !event.isSystem){
        if(event.repeatCount != 0){
            return true
        }
        when(event.keyCode){
            physicalKeys[0] -> {
                chip8Cycle.keyPress(0)
                isHandled = true
            }
            physicalKeys[1] -> {
                chip8Cycle.keyPress(1)
                isHandled = true
            }
            physicalKeys[2] -> {
                chip8Cycle.keyPress(2)
                isHandled = true
            }
            physicalKeys[3] -> {
                chip8Cycle.keyPress(3)
                isHandled = true
            }
            physicalKeys[4] -> {
                chip8Cycle.keyPress(4)
                isHandled = true
            }
            physicalKeys[5] -> {
                chip8Cycle.keyPress(5)
                isHandled = true
            }
            physicalKeys[6] -> {
                chip8Cycle.keyPress(6)
                isHandled = true
            }
            physicalKeys[7] -> {
                chip8Cycle.keyPress(7)
                isHandled = true
            }
            physicalKeys[8] -> {
                chip8Cycle.keyPress(8)
                isHandled = true
            }
            physicalKeys[9] -> {
                chip8Cycle.keyPress(9)
                isHandled = true
            }
            physicalKeys[10] -> {
                chip8Cycle.keyPress(10)
                isHandled = true
            }
            physicalKeys[11] -> {
                chip8Cycle.keyPress(11)
                isHandled = true
            }
            physicalKeys[12] -> {
                chip8Cycle.keyPress(12)
                isHandled = true
            }
            physicalKeys[13] -> {
                chip8Cycle.keyPress(13)
                isHandled = true
            }
            physicalKeys[14] -> {
                chip8Cycle.keyPress(14)
                isHandled = true
            }
            physicalKeys[15] -> {
                chip8Cycle.keyPress(15)
                isHandled = true
            }
            else -> isHandled = true
        }

    }else if(event.action == KeyEvent.ACTION_UP && !event.isSystem){
        when(event.keyCode){
            physicalKeys[0] -> {
                chip8Cycle.keyRelease(0)
                isHandled = true
            }
            physicalKeys[1] -> {
                chip8Cycle.keyRelease(1)
                isHandled = true
            }
            physicalKeys[2] -> {
                chip8Cycle.keyRelease(2)
                isHandled = true
            }
            physicalKeys[3] -> {
                chip8Cycle.keyRelease(3)
                isHandled = true
            }
            physicalKeys[4] -> {
                chip8Cycle.keyRelease(4)
                isHandled = true
            }
            physicalKeys[5] -> {
                chip8Cycle.keyRelease(5)
                isHandled = true
            }
            physicalKeys[6] -> {
                chip8Cycle.keyRelease(6)
                isHandled = true
            }
            physicalKeys[7] -> {
                chip8Cycle.keyRelease(7)
                isHandled = true
            }
            physicalKeys[8] -> {
                chip8Cycle.keyRelease(8)
                isHandled = true
            }
            physicalKeys[9] -> {
                chip8Cycle.keyRelease(9)
                isHandled = true
            }
            physicalKeys[10] -> {
                chip8Cycle.keyRelease(10)
                isHandled = true
            }
            physicalKeys[11] -> {
                chip8Cycle.keyRelease(11)
                isHandled = true
            }
            physicalKeys[12] -> {
                chip8Cycle.keyRelease(12)
                isHandled = true
            }
            physicalKeys[13] -> {
                chip8Cycle.keyRelease(13)
                isHandled = true
            }
            physicalKeys[14] -> {
                chip8Cycle.keyRelease(14)
                isHandled = true
            }
            physicalKeys[15] -> {
                chip8Cycle.keyRelease(15)
                isHandled = true
            }
        }
    }
    return isHandled
}
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if (event != null) {
//
//        }
//        return super.onKeyDown(keyCode, event)
//    }
//    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
//
//    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activitybar, menu)
        this.menu = menu;
        val theme = menu?.findItem(R.id.menuTheme)
        loadData(theme)
        return true
}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuReset -> resetButton(chip8Cycle)
            R.id.menuPause -> pauseEmulation(chip8Cycle)
            R.id.menuLoad -> openLoadROMIntent()
            R.id.menuSet -> showCyclesButton(chip8Cycle)
            R.id.menuChange -> showMachineTypeButton(chip8Cycle)
            R.id.menuKeyBind -> showKeyBinderActivity()
            R.id.menuTheme -> setButtonDayNightStyleButtonListener()
        }
        //this.menu = menu;
        return super.onOptionsItemSelected(item)
    }
    private fun showKeyBinderActivity(){
        val i = Intent(this, KeyControllerActivity::class.java)
        i.putExtra("bindableKeys", physicalKeys)
        keyBinder.launch(i)
    }
    //helper functions for various settings related to the emulated machine
    private fun showCyclesButton(chip8Cycle: Chip8Cycle){
        if(chip8Cycle.getRomStatus())
            showCyclesDialog(chip8Cycle)
        else
            Toast.makeText(this, "Machine is not running!", Toast.LENGTH_SHORT).show()
    }
    private fun showMachineTypeButton(chip8Cycle: Chip8Cycle){
        if(chip8Cycle.getRomStatus())
            showMachineTypeSelectorDialog(chip8Cycle)
        else
            Toast.makeText(this, "Machine is not running!", Toast.LENGTH_SHORT).show()
    }
    private fun resetButton(chip8Cycle: Chip8Cycle){
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
//        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
//        dialogTitle.text = resources.getString(R.string.cycles_dialog_title_text_en)

        val editText = dialog.findViewById<EditText>(R.id.newCycles)
        editText.setText(chip8Cycle.cycles.toString())
        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            //retrieve value from the text box
            val newCycles: Int
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
    private fun openLoadROMIntent(){
        getContent.launch("*/*")
    }
    val keyBinder = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->

        Log.i("here", "${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val newKeys = data?.getIntArrayExtra("bindableKeys")
            val editor: SharedPreferences.Editor = sharedPreferences.edit()

            val keys = resources.getStringArray(R.array.chip8_keys)
            if (newKeys != null) {
                for((index) in newKeys.withIndex()){
                    editor.putInt(keys[index], newKeys[index])
                    physicalKeys[index] = newKeys[index]
                }
            }

            editor.apply()
            editor.commit()
        }
    }
    private fun showInitialSetupDialog(romArray: ArrayList<Int>){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.initial_setup_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
//        dialogTitle.text = resources.getString(R.string.machine_type_dialog_title_text_en)
//        val dialogSubTitle = dialog.findViewById<TextView>(R.id.dialogSubTitle)
//        dialogSubTitle.text = resources.getString(R.string.cycles_dialog_title_text_en)



        val btnStart = dialog.findViewById<Button>(R.id.btnStart)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val editText = dialog.findViewById<EditText>(R.id.newCycles)
        val machineRgp = dialog.findViewById<RadioGroup>(R.id.machineGroup)

        btnStart.setOnClickListener {
            //retrieve value from the text box
            val newCycles: Int
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
            }
//            else if(selectedId == R.id.SCHIPCompatRadioButton){
//                if(!chip8Cycle.checkROMSize(romArray.size, MachineType.SUPERCHIP_1_1)){
//                    Toast.makeText(applicationContext,"Rom is too large for ${MachineType.SUPERCHIP_1_1_COMPAT.machineName}!",Toast.LENGTH_LONG).show()
//                }else{
//                    newMachine = MachineType.SUPERCHIP_1_1_COMPAT
//                }
//            }
            else if(selectedId == -1){
                newMachine = null
            }
            if(newCycles >= 0 && newMachine != null){
                chip8Cycle.cycles = newCycles
                chip8Cycle.currentMachine = newMachine
                Toast.makeText(this, "Cycles: $newCycles", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Machine Type: $newMachine", Toast.LENGTH_SHORT).show()
                val dbHandler: DatabaseHandler = DatabaseHandler(this)
                val status: Boolean = try{
                    dbHandler.saveConfigs(RomConfigClass(crc32.value, newMachine, newCycles))
                }catch (sqle: SQLiteException){
                    Toast.makeText(this, "An error occurred while loading the ROM: $sqle", Toast.LENGTH_SHORT).show()
                    Log.e("showInitialSetupDialog", sqle.toString())
                    false
                }finally {
                    dbHandler.close()
                }
                Log.i("status","status: $status")
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
//            val SCHIPCompatRadioButton = dialog.findViewById<RadioButton>(R.id.SCHIPCompatRadioButton)
//            SCHIPCompatRadioButton.isEnabled = false
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
//        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
//        dialogTitle.text = resources.getString(R.string.machine_type_dialog_title_text_en)

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
//            else if(selectedId == R.id.SCHIPCompatRadioButton){
//                if(!chip8Cycle.checkROMSize(chip8Cycle.getRomSize(), MachineType.SUPERCHIP_1_1)){
//                    Toast.makeText(applicationContext,"Rom is too large for ${MachineType.SUPERCHIP_1_1_COMPAT.machineName}!",Toast.LENGTH_LONG).show()
//                }else{
//                    newMachine = MachineType.SUPERCHIP_1_1_COMPAT
//                }
//            }
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
//        else if(chip8Cycle.currentMachine == MachineType.SUPERCHIP_1_1_COMPAT){
//            val btn = dialog.findViewById<RadioButton>(R.id.SCHIPCompatRadioButton)
//            btn.isChecked = true
//        }
        val romSize = chip8Cycle.getRomSize()

        if(romSize > 3232L){
            val COSMACradioButton = dialog.findViewById<RadioButton>(R.id.COSMACradioButton)
            COSMACradioButton.isEnabled = false
        }
        if(romSize > 3583L){
            val SCHIPradioButton = dialog.findViewById<RadioButton>(R.id.SCHIPradioButton)
            SCHIPradioButton.isEnabled = false
//            val SCHIPCompatRadioButton = dialog.findViewById<RadioButton>(R.id.SCHIPCompatRadioButton)
//            SCHIPCompatRadioButton.isEnabled = false
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
        if(chip8Cycle.getRomStatus()){
            chip8Cycle.stopEmulation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chip8Cycle.stopEmulation()
        chip8Cycle.closeSound()
        chip8Cycle.closeDbHandler()
    }

    private fun pauseEmulation(chip8Cycle: Chip8Cycle){
        val status = menu?.findItem(R.id.menuPause)
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
