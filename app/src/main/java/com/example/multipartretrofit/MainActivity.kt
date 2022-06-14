package com.example.multipartretrofit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.multipartretrofit.api.ApiClient
import com.example.multipartretrofit.api.ApiService
import com.example.multipartretrofit.model.ImageResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var fileUri: Uri
    private val PICK_IMAGE: Int = 1001
    private lateinit var apiService: ApiService
    private var catPhoto = File("")

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiService = ApiClient.createServiceWithAuth(ApiService::class.java)
        setupUi()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupUi() {
        val pickImage = findViewById<Button>(R.id.btnPickImage)
        val uploadImage = findViewById<Button>(R.id.btnUploadImage)

        pickImage.setOnClickListener {
            checkPermission()
        }

        uploadImage.setOnClickListener {
            uploadImageToServer()
        }
    }

    private fun uploadImageToServer() {
        val builder: MultipartBody.Builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)

        if (catPhoto.length() > 0) {
            builder.addFormDataPart(
                "file",
                catPhoto.name,
                catPhoto.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            builder.addFormDataPart("sub_id", "something")

            val body = builder.build()
            apiService.uploadPhoto(body).enqueue(object : Callback<ImageResponse> {
                override fun onResponse(
                    call: Call<ImageResponse>,
                    response: Response<ImageResponse>
                ) {
                    Log.d("TAG", "onResponse: ${response.body()}")
                }

                override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                    Log.d("TAG", "onFailure: ${t.localizedMessage}")
                }

            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray(),
                PICK_IMAGE
            )
        } else {
            startGallery()
        }
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            fileUri = data?.data!!

            val ins = this.contentResolver.openInputStream(fileUri)
            catPhoto = File.createTempFile(
                "file",
                ".jpg",
                this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            val fileOutputStream = FileOutputStream(catPhoto)

            ins?.copyTo(fileOutputStream)
            ins?.close()
            fileOutputStream.close()
        }
    }
}