/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Parneet Singh <gurayaparneet@gmail.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccUtils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import autodagger.AutoInjector
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.gcc.talk.gccActivities.GccTakePhotoActivity
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccRemotefilebrowser.activities.GccRemoteFileBrowserActivity
import com.gcc.talk.gccUtils.bundle.GccBundleKeys
import com.gcc.talk.gccUtils.permissions.GccPlatformPermissionUtil
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccPickImage(private val activity: Activity, private var currentUser: GccUser?) {

    @Inject
    lateinit var ncApi: GccNcApi

    @Inject
    lateinit var permissionUtil: GccPlatformPermissionUtil

    init {
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)
    }

    fun selectLocal(startImagePickerForResult: ActivityResultLauncher<Intent>) {
        ImagePicker.Companion.with(activity)
            .provider(ImageProvider.GALLERY)
            .crop()
            .cropSquare()
            .compress(MAX_SIZE)
            .maxResultSize(MAX_SIZE, MAX_SIZE)
            .createIntent { intent ->
                startImagePickerForResult.launch(intent)
            }
    }

    private fun selectLocal(startImagePickerForResult: ActivityResultLauncher<Intent>, file: File) {
        ImagePicker.Companion.with(activity)
            .provider(ImageProvider.URI)
            .crop()
            .cropSquare()
            .compress(MAX_SIZE)
            .maxResultSize(MAX_SIZE, MAX_SIZE)
            .setUri(Uri.fromFile(file))
            .createIntent { intent ->
                startImagePickerForResult.launch(intent)
            }
    }

    fun selectRemote(startSelectRemoteFilesIntentForResult: ActivityResultLauncher<Intent>) {
        val bundle = Bundle()
        bundle.putString(GccBundleKeys.KEY_MIME_TYPE_FILTER, GccMimetype.IMAGE_PREFIX)

        val avatarIntent = Intent(activity, GccRemoteFileBrowserActivity::class.java)
        avatarIntent.putExtras(bundle)
        startSelectRemoteFilesIntentForResult.launch(avatarIntent)
    }

    fun takePicture(startTakePictureIntentForResult: ActivityResultLauncher<Intent>) {
        if (permissionUtil.isCameraPermissionGranted()) {
            startTakePictureIntentForResult.launch(GccTakePhotoActivity.createIntent(activity))
        } else {
            activity.requestPermissions(
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_PERMISSION_CAMERA
            )
        }
    }

    private fun handleAvatar(startImagePickerForResult: ActivityResultLauncher<Intent>, remotePath: String?) {
        val uri = currentUser!!.baseUrl + "/index.php/apps/files/api/v1/thumbnail/512/512/" +
            Uri.encode(remotePath, "/")
        val downloadCall = ncApi.downloadResizedImage(
            GccApiUtils.getCredentials(currentUser!!.username, currentUser!!.token),
            uri
        )
        downloadCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                saveBitmapAndPassToImagePicker(
                    startImagePickerForResult,
                    BitmapFactory.decodeStream(response.body()!!.byteStream())
                )
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // unused atm
            }
        })
    }

    // only possible with API26
    private fun saveBitmapAndPassToImagePicker(
        startImagePickerForResult: ActivityResultLauncher<Intent>,
        bitmap: Bitmap
    ) {
        val file: File = saveBitmapToTempFile(bitmap) ?: return
        selectLocal(startImagePickerForResult, file)
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap): File? {
        try {
            val file = createTempFileForAvatar()
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, FULL_QUALITY, out)
                }
                return file
            } catch (e: IOException) {
                Log.e(TAG, "Error compressing bitmap", e)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error creating temporary avatar image", e)
        }
        return null
    }

    private fun createTempFileForAvatar(): File {
        GccFileUtils.removeTempCacheFile(
            activity,
            AVATAR_PATH
        )
        return GccFileUtils.getTempCacheFile(
            activity,
            AVATAR_PATH
        )
    }

    fun onImagePickerResult(data: Intent?, handleImage: (uri: Uri) -> Unit) {
        val uri: Uri = data?.data!!
        handleImage(uri)
    }

    fun onSelectRemoteFilesResult(startImagePickerForResult: ActivityResultLauncher<Intent>, data: Intent?) {
        val pathList = data?.getStringArrayListExtra(GccRemoteFileBrowserActivity.EXTRA_SELECTED_PATHS)
        if (pathList?.size!! >= 1) {
            handleAvatar(startImagePickerForResult, pathList[0])
        }
    }

    fun onTakePictureResult(startImagePickerForResult: ActivityResultLauncher<Intent>, data: Intent?) {
        data?.data?.path?.let {
            selectLocal(startImagePickerForResult, File(it))
        }
    }

    companion object {
        private const val TAG: String = "GccPickImage"
        private const val MAX_SIZE: Int = 1024
        private const val AVATAR_PATH = "photos/avatar.png"
        private const val FULL_QUALITY: Int = 100
        const val REQUEST_PERMISSION_CAMERA: Int = 1
    }
}
