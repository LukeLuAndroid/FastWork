package com.lyd.test

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.sdk.socket.*
import org.json.JSONObject
import java.io.*
import java.net.Socket


class MainActivity : AppCompatActivity() {

    lateinit var socketBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getSystemInfo();
        getWifiInfo();
        initView()
//        startSocketServer();
    }

    private fun getSystemInfo() {
        var base_ver = getBaseband_Ver()
        println("base_ver=$base_ver")
        println("cpuInfo=" + getCpuName())
    }

    private fun initView() {
        socketBtn = findViewById(R.id.socket_test)
        socketBtn.setOnClickListener { l ->
            connectToSocket();
        }
    }

    private fun getWifiInfo() {
        val manager: WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var list: List<WifiConfiguration> = manager.configuredNetworks;
        for (config in list) {
            Log.d("MainActivity", config.SSID);
            Log.d("MainActivity", config.preSharedKey);
        }
    }

    /**
     * BASEBAND-VER
     * 基带版本
     * return String
     */

    fun getBaseband_Ver(): String {
        var Version = ""
        try {
            val cl = Class.forName("android.os.SystemProperties")
//            val invoker = cl.newInstance()
            val m = cl.getMethod("get", *arrayOf<Class<*>>(String::class.java, String::class.java))
            val result = m.invoke(null, "gsm.version.baseband", "no message")
            // System.out.println(">>>>>>><<<<<<<" +(String)result);
            Version = result as String
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Version
    }

    fun getCpuName(): String? {
        try {
            val fr = FileReader("/proc/cpuinfo")
            val br = BufferedReader(fr)
            val text = br.readLine()
            val array = text.split(":\\s+".toRegex(), 2).toTypedArray()
            for (i in array.indices) {
            }
            return array[1]
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * 开始socket服务
     */
    private fun startSocketServer() {
        val s: SocketServer = SocketServer(10110);
        s.init()
        s.setListener {
            it.let {
                var data: MsgData = it.data
                println("receive data from client = " + data.toJson())
                data.response = "server has receive data"
                s.writeDataToClient(it);
            }
        }
        s.startSocket()
    }

    private fun connectToSocket() {
//        var client: SocketClient = SocketClient("127.0.0.1", 10110)
//        client.setListener { response ->
//            try {
//                var json = JSONObject(response)
//                if (json["code"] == 1) {
//                    println("receive data from server = " + json["response"])
//                }
//            } catch (e: java.lang.Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        client.connect()
//
//        var data: MsgData = MsgData()
//        data.code = 1
//        data.request = "request url"
//        client.write(data)

        var urlManager = SocketManager.getInstance().urlManager
        Log.d("MainActivity","urlManager = "+urlManager)

        var debugInfo = SocketManager.getInstance().debugInfo
        Log.d("MainActivity","debugInfo = "+debugInfo)

        var log = SocketManager.getInstance().log
        Log.d("MainActivity","log = "+log)
    }

}
