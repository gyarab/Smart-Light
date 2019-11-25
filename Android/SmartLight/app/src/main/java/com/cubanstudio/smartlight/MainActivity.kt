package com.cubanstudio.smartlight

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {


    private val key = "\$=EF%@gR;[M+SLWx*i%m@qA}x6kDl."
    private lateinit var wifiManager: WifiManager
    private lateinit var btService: BluetoothService
    private var wifiArray = ArrayList<String>()
    private lateinit var wifiArrayAdapter: ArrayAdapter<String>

    private var smartLightDevice = "Smart Light"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        //set statusbar transparent
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        //app fullscreen for notch displays
        val attrib = window.attributes
        if (Build.VERSION.SDK_INT >= 28)
            attrib.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        loading.setMaxFrame(125)
        wifiList.alpha = 0.0f
        btService = BluetoothService(applicationContext)
        btService.bluetoothInit()
        if(btService.getTargetDevice(smartLightDevice)!=null){
            btService.connectDevice(btService.getFoundDevice())
        }else{
            //TODO find and bond with device
        }
        val incomeDataFilter = IntentFilter()
        incomeDataFilter.addAction("WILI") // WiFi network list
        incomeDataFilter.addAction("IPAD") // IP ADDRESS
        incomeDataFilter.addAction("CONNECTING")
        //incomeDataFilter.addAction("")
        //incomeDataFilter.addAction("")

        registerReceiver(dataReceiver,incomeDataFilter)


        while (!btService.isConnected()){Log.e(" . "," . ")}
        loading.setMaxFrame(194)
        loading.repeatCount = 0

        verifyDevice()
        btService.sendData("GET_WIFI","")
        wifiArrayAdapter =ArrayAdapter<String>(applicationContext,R.layout.wifi_list_row_layout,R.id.rowText,wifiArray)
        wifiList.adapter = wifiArrayAdapter

        wifiList.animate().alpha(1.0f).setDuration(3000).setStartDelay(5000).start()
        wifiList.setOnItemClickListener(itemListener)







        wifiName.setOnClickListener {


            //packageManager.setComponentEnabledSetting(componentName,
            //  PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP)
            packageManager.setComponentEnabledSetting(
                ComponentName(applicationContext, "com.cubanstudio.smartlight.OFF"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(applicationContext, "com.cubanstudio.smartlight.ON"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )

        }
        loading.setOnClickListener {
            packageManager.setComponentEnabledSetting(
                ComponentName(applicationContext, "com.cubanstudio.smartlight.ON"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(applicationContext, "com.cubanstudio.smartlight.OFF"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
        }
        reloadWifi.setOnClickListener{
            btService.sendData("GET_WIFI","")

        }


    }


    private val itemListener = object : AdapterView.OnItemClickListener {
        override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

            val selectedWifi = p1?.findViewById<TextView>(R.id.rowText)?.text.toString()

            Toast.makeText(
                applicationContext,
                selectedWifi,
                Toast.LENGTH_SHORT
            ).show()


            wifiSSID.text = selectedWifi

            wifiList.animate().alpha(0.0f).setDuration(2000).setStartDelay(0).start()
            wifiSSID.animate().alpha(1.0f).setDuration(2000).start()

            wifiPasswordField.animate().alpha(1.0f).setDuration(2000).start()
            button.animate().alpha(1.0f).setDuration(2000).start()
            button.setOnClickListener {
                btService.sendData("CONNECT",selectedWifi+";"+wifiPasswordField.text.toString())
            }

        }
    }

    val dataReceiver = object :BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            val incoming_data = intent?.getStringExtra("DATA")
            Log.e("INCOME",incoming_data)
            when(intent?.action){
                "WILI" -> {
                    if (!wifiArray.contains(incoming_data)){
                    wifiArray.add(incoming_data as String)
                    wifiArrayAdapter.notifyDataSetChanged()}
                }
                "IPAD" -> {
                    Toast.makeText(
                        applicationContext,
                        "WiFi IP : " + incoming_data,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                "CONNECTING" ->{
                    when(incoming_data){
                        "0" ->{}
                        "1" ->{}
                        "2" ->{}
                    }

                }

            }
        }
    }


    private fun verifyDevice() {
        btService.sendData("VERIFICATION",key)
    }


}
