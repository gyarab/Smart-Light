package com.cubanstudio.smartlight

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.lang.IllegalArgumentException

class BluetoothService(context: Context){
    private lateinit var bleAdapter: BluetoothAdapter
    private lateinit var deviceName:String
    private var applicationContext:Context = context
    private lateinit var socket: BluetoothSocket
    private lateinit var comThread: CommunicationThread
    private lateinit var targetDevice: BluetoothDevice


    fun bluetoothInit(){
        bleAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bleAdapter==null){
            //TODO()//handle null bluetooth adapter
        }else{

        }
    }
    fun getFoundDevice():BluetoothDevice{
        return targetDevice
    }

    fun getTargetDevice(name:String):BluetoothDevice?{
        for (device in bleAdapter.bondedDevices){
            if (device!=null){
                if(device.name.equals(name)){
                    try {
                        applicationContext.unregisterReceiver(deviceReceiver)
                    }catch (exception:IllegalArgumentException){

                    }
                    targetDevice = device
                    return device
                }
            }
        }
        return null
    }

    fun connectDevice(device: BluetoothDevice){
        socket = device.createInsecureRfcommSocketToServiceRecord(device.uuids[0].uuid)
        socket.connect()
         if(socket.isConnected){
             comThread = CommunicationThread(socket.inputStream,socket.outputStream,applicationContext)
             comThread.listenForData()
           //  verifyDevice()
         }
    }

    fun isConnected():Boolean{
        return socket.isConnected
    }

    fun sendData(head: String,body :String){
        comThread.sendData(head,body)
    }

    fun findAndBond(){
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        applicationContext.registerReceiver(deviceReceiver, filter)

        bleAdapter.startDiscovery()

    }


    private val deviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val dev = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        if (dev.name.equals(deviceName)) {
                            dev.createBond()
                            bleAdapter.cancelDiscovery()
                        }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                        val dev = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        if (dev.bondState == BluetoothDevice.BOND_BONDED) {
                            getTargetDevice(deviceName)

                            }
                }
            }
        }
    }
}