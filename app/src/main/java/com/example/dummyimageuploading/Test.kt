package com.example.dummyimageuploading

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
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
                uploadImageToCloudinary(context, uri,  "thread")
            }
        }) {
            Text("Upload")
        }
    }
}

fun uploadImageToCloudinary(context: Context, imageUri: Uri, uploadPreset: String) {
    val cloudName = "dixcja6yr"
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(imageUri)
    val file = File(context.cacheDir, "temp_image.jpg").apply {
        outputStream().use { inputStream?.copyTo(it) }
    }

    val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
    val presetRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), uploadPreset)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val api = RetrofitInstance.getClient()
            val response = api.uploadImage(cloudName, body, presetRequestBody)
            if (response.isSuccessful) {
                val cloudinaryResponse = response.body()
                val secureUrl = cloudinaryResponse?.secure_url
                println("secureUrl" + secureUrl)
            } else {
                Log.e("Cloudinary", "Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("Cloudinary", "Exception: ${e.message}")
        }
    }
}
