package com.example.dummyimageuploading

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.rememberAsyncImagePainter
import com.example.dummyimageuploading.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.security.MessageDigest

@Composable
fun test(){
    var imageRef by remember {
        mutableStateOf<Uri?>(null)
    }
    var context = LocalContext.current
    val requestToPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var laucher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            imageRef = uri
        }

    //permissionLauncher
    var permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {

            } else {

            }

        }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image(painter = if(imageRef==null) painterResource(id = R.drawable.thread_logo)
            else rememberAsyncImagePainter(imageRef),
            contentDescription =null,
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .clickable {
                    var isGranted = ContextCompat.checkSelfPermission(
                        context,
                        requestToPermission
                    ) == PackageManager.PERMISSION_GRANTED

                    if (isGranted) {
                        laucher.launch("image/*")
                    } else {
                        permissionLauncher.launch(requestToPermission)
                    }
                }
        )
        Button(onClick = {
            imageRef?.let { uri ->
                uploadImageToCloudinary(context, uri, "346939552412259", "c4lBSFh9s2NjEX2KGvHlN2OmRIE", "thread")
            }
        }) {
            Text("Upload")
        }
    }
}

fun generateSignature(params: Map<String, String>, apiSecret: String): String {
    val sortedParams = params.toSortedMap()
    val paramString = sortedParams.entries.joinToString("&") { "${it.key}=${it.value}" }
    val stringToSign = "$paramString$apiSecret"
    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(stringToSign.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
}

fun uploadImageToCloudinary(
    context: Context,
    imageUri: Uri,
    apiKey: String,
    apiSecret: String,
    uploadPreset: String
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Convert URI to File
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val file = File.createTempFile("upload", ".jpg", context.cacheDir)
            println("file "+ file)
            file.outputStream().use { inputStream?.copyTo(it) }

            // Generate Cloudinary Signature
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val signatureParams = mapOf(
                "timestamp" to timestamp,
                "upload_preset" to uploadPreset
            )
            val signature = generateSignature(signatureParams, apiSecret)
            println("signature "+ signature)


            // Create MultipartBody for the image
            val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            println("requestBody " +requestBody)
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)
            println("multipartBody " +multipartBody)

            // Call Cloudinary API
            val response = RetrofitInstance.api.uploadImage(
                multipartBody,
                apiKey,
                timestamp,
                signature,
                uploadPreset
            )
            println("response "+ response)

            // Log the response
            println("Image uploaded successfully: ${response.secure_url}")
        } catch (e: Exception) {
            println("Error uploading image: ${e.message}")
        }
    }
}
