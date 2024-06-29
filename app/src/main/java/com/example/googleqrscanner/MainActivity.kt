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
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

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

    private fun startScanning() {
        scanner.startScan().addOnSuccessListener {
            val result = it.rawValue
            result?.let {
                scannedValueTv.text = buildString {
                    append("Scanned Value : ")
                    append(it)
                }
            }

            val client = OkHttpClient()

            val request = Request.Builder()
                .url("https://publicobject.com/helloworld.txt")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Запрос к серверу не был успешен:" +
                                " ${response.code} ${response.message}")
                    }
                    // пример получения конкретного заголовка ответа
                    println("Server: ${response.header("Server")}")
                    // вывод тела ответа
                    println(response.body!!.string())
                }
            } catch (e: IOException) {
                println("Ошибка подключения: $e");
            }

        }.addOnCanceledListener {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()

        }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()

            }
    }
}