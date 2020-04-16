package com.lyd.test

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var base_ver = getBaseband_Ver()
        print("base_ver=$base_ver")

        print("cpuInfo="+Build.CPU_ABI)
        print("cpuInfo="+Build.BOARD)

        print("cpuInfo="+getCpuName())

        getWifiInfo();
    }

    private fun getWifiInfo() {
        val manager: WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var list:List<WifiConfiguration> = manager.configuredNetworks;
        for (config in list){
            Log.d("MainActivity",config.SSID);
            Log.d("MainActivity",config.preSharedKey);
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
}
