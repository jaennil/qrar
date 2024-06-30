package com.example.googleqrscanner

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var scanQrBtn: Button
    private lateinit var scannedValueTv: TextView
    private var isScannerInstalled = false
    private lateinit var scanner: GmsBarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        installGoogleScanner()
        initVars()
        registerUiListener()
    }


    private fun installGoogleScanner() {
        val moduleInstall = ModuleInstall.getClient(this)
        val moduleInstallRequest = ModuleInstallRequest.newBuilder()
            .addApi(GmsBarcodeScanning.getClient(this))
            .build()

        moduleInstall.installModules(moduleInstallRequest).addOnSuccessListener {
            isScannerInstalled = true
        }.addOnFailureListener {
            isScannerInstalled = false
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initVars() {
        scanQrBtn = findViewById(R.id.scanQrBtn)
        scannedValueTv = findViewById(R.id.scannedValueTv)

        val options = initializeGoogleScanner()
        scanner = GmsBarcodeScanning.getClient(this, options)
    }

    private fun initializeGoogleScanner(): GmsBarcodeScannerOptions {
        return GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom().build()
    }

    private fun registerUiListener() {
        scanQrBtn.setOnClickListener {
            if (isScannerInstalled) {
                startScanning()
            } else {
                Toast.makeText(this, "Please try again...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun qrAPI(value: String): String? {

        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("qrraw", value)
            .add("token", "27643.f8kIwQlqpqtBt8LvD")
            .build()

        val request = Request.Builder()
            .url("https://proverkacheka.com/api/v1/check/get")
            .post(formBody)
            .build()

        return client.newCall(request).execute().body?.string()
    }

    private fun startScanning() {
        scanner.startScan().addOnSuccessListener {
            val result = it.rawValue
            result?.let { qrCodeValue ->
                scannedValueTv.text = buildString {
                    append("Scanned Value : ")
                    append(qrCodeValue)
                }
                println(qrCodeValue);

                GlobalScope.launch(Dispatchers.IO) {
                    val json_string = qrAPI(qrCodeValue)
                    val json = json_string?.let { it1 -> JSONObject(it1) }
                    val items = json?.getJSONObject("data")?.getJSONObject("json")?.getJSONArray("items")
                    if (items != null) {
                        for (i in 0..<items.length()) {
                            println(items[i])
                        }
                    }
                }
            }
        }.addOnCanceledListener {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()

        }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()

            }
    }
}