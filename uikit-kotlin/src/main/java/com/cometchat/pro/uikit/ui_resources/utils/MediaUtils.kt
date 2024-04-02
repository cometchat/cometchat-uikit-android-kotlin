package com.cometchat.pro.uikit.ui_resources.utils

import android.app.Activity
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Vibrator
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.cometchat.pro.uikit.BuildConfig
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.Utils.Companion.generateFileName
import com.cometchat.pro.uikit.ui_resources.utils.Utils.Companion.getDocumentCacheDir
import com.cometchat.pro.uikit.ui_resources.utils.Utils.Companion.getFileName
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class MediaUtils {
    companion object {
        private var activity: Activity? = null

        var pictureImagePath: String? = null

        var uri: Uri? = null
        private var bitmap: Bitmap? = null

        fun getPickImageChooserIntent(a: Activity): Intent? {
            activity = a
            // Determine Uri of camera image to save.
            val outputFileUri: Uri = getCaptureImageOutputUri()!!
            val allIntents: MutableList<Intent?> = ArrayList()
            val packageManager: PackageManager = activity!!.getPackageManager()

            // collect all camera intents
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val listCam = packageManager.queryIntentActivities(captureIntent, 0)
            for (res in listCam) {
                val intent = Intent(captureIntent)
                intent.component =
                    ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(res.activityInfo.packageName)
                if (outputFileUri != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                }
                allIntents.add(intent)
            }

            // collect all gallery intents
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/*"
            val listGallery = packageManager.queryIntentActivities(galleryIntent, 0)
            for (res in listGallery) {
                val intent = Intent(galleryIntent)
                intent.component =
                    ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(res.activityInfo.packageName)
                allIntents.add(intent)
            }

            // the main intent is the last in the list (fucking android) so pickup the useless one
            var mainIntent = allIntents[allIntents.size - 1]
            for (intent in allIntents) {
                if (intent!!.component!!.className == "com.android.documentsui.DocumentsActivity") {
                    mainIntent = intent
                    break
                }
            }
            allIntents.remove(mainIntent)

            // Create a chooser from the main intent
            val chooserIntent = Intent.createChooser(mainIntent, "Select source")

            // Add all other intents
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toTypedArray())
            return chooserIntent
        }

        fun getFileIntent(type: Array<String>): Intent? {
            val intent = Intent()
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, type)
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            return intent
        }

        /**
         * This method is used to open file from url.
         * @param url is Url of file.
         */
        fun openFile(url: String?, context: Context) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        fun openCamera(context: Context): Intent? {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "$timeStamp.jpg"
            val storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )
            pictureImagePath = storageDir.absolutePath + "/" + imageFileName
            val file: File = File(pictureImagePath)
            var outputFileUri: Uri?
            var app: ApplicationInfo? = null
            var provider: String? = null
            try {
                app = context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )
                val bundle = app.metaData
                provider = bundle.getString(BuildConfig.LIBRARY_PACKAGE_NAME)
                Log.d("openCamera", "openCamera:  $provider")
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            outputFileUri = FileProvider.getUriForFile(context, "$provider.provider", file)
            if (Build.VERSION.SDK_INT >= 29) {
                val resolver = context.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM")
                outputFileUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri = outputFileUri
            } else if (Build.VERSION.SDK_INT <= 23) {
                outputFileUri = Uri.fromFile(file)
                uri = outputFileUri
            }
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            return intent
        }

        fun openGallery(a: Activity) {
            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            a.startActivityForResult(pickPhoto, UIKitConstants.RequestCode.GALLERY)
        }

        fun openAudio(a: Activity){
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "audio/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a Audio")
            a.startActivityForResult(chooseFile,UIKitConstants.RequestCode.AUDIO)
        }

        private fun getCaptureImageOutputUri(): Uri? {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "$timeStamp.jpg"
            val storageDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(storageDir.absolutePath + "/" + imageFileName)
            return Uri.fromFile(file)
        }

        fun handleCameraImage(): String? {
            return pictureImagePath
        }

        fun processImageIntentData(resultCode: Int, data: Intent): File? {
            val bitmap: Bitmap?
            val picUri: Uri
            if (resultCode == Activity.RESULT_OK) {
                if (getPickImageResultUri(data) != null) {
                    picUri = getPickImageResultUri(data)!!
                    try {
                        bitmap =
                            MediaStore.Images.Media.getBitmap(activity?.contentResolver, picUri)
                        return createFileFromBitmap(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    bitmap = data.extras!!["data"] as Bitmap?
                    return createFileFromBitmap(bitmap!!)
                }
            }
            return null
        }

        private fun getPickImageResultUri(data: Intent?): Uri? {
            var isCamera = true
            if (data != null) {
                val action = data.action
                isCamera = action != null && action == MediaStore.ACTION_IMAGE_CAPTURE
            }
            return if (isCamera) getCaptureImageOutputUri() else data!!.data
        }

        private fun createFileFromBitmap(bitmap: Bitmap): File? {
            val f: File = File(activity?.cacheDir, System.currentTimeMillis().toString())
            try {
                f.createNewFile()
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
                val bitmapdata = bos.toByteArray()
                val fos = FileOutputStream(f)
                fos.write(bitmapdata)
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return f
        }

        fun getRealPath(context: Context?, fileUri: Uri?): File {
            Log.d("", "getRealPath: " + fileUri?.path)
            val realPath: String
            if (fileUri?.let { isGoogleDrive(it) } == true) {
                return saveDriveFile(context!!, fileUri)
            } else if (Build.VERSION.SDK_INT < 30) {
                realPath = fileUri?.let { getRealPathFromURI(context!!, it) }!!
            } else {
                //(Build.VERSION.SDK_INT == 30)
                realPath = fileUri?.let { getRealPathFromN(context, it) }!!
            }

            return File(realPath)
        }

        @RequiresApi(Build.VERSION_CODES.R)
        private fun getRealPathFromN(context: Context?, uri: Uri): String? {
            val returnUri = uri
            val returnCursor = context?.contentResolver?.query(
                returnUri,
                null, null, null, null
            )
            /*
             * Get the column indexes of the data in the Cursor,
             *     * move to the first row in the Cursor, get the data,
             *     * and display it.
             * */
            /*
             * Get the column indexes of the data in the Cursor,
             *     * move to the first row in the Cursor, get the data,
             *     * and display it.
             * */
            val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE)
            returnCursor?.moveToFirst()
            val name = returnCursor?.getString(nameIndex!!)
            val size = returnCursor?.getLong(sizeIndex!!)
            val file = File(context?.filesDir, name)
            try {
                val inputStream = context?.contentResolver?.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                var read = 0
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable = inputStream!!.available()

                //int bufferSize = 1024;
                val bufferSize = min(bytesAvailable, maxBufferSize)
                val buffers = ByteArray(bufferSize)
                while (inputStream?.read(buffers).also {
                        if (it != null) {
                            read = it
                        }
                    } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                Log.e("File Size", "Size " + file.length())
                inputStream.close()
                outputStream.close()
                Log.e("File Path", "Path " + file.path)
                Log.e("File Size", "Size " + file.length())
            } catch (e: Exception) {
                Log.e("Exception", e.message!!)
            }
            return file.path
        }

        fun saveDriveFile(context: Context?, uri: Uri?): File {
            var file: File? = null
            try {
                if (uri != null) {
                    file = File(context?.cacheDir, getFileName(context, uri))
                    val inputStream = context?.contentResolver?.openInputStream(uri)
                    try {
                        val output: OutputStream = FileOutputStream(file)
                        try {
                            val buffer = ByteArray(4 * 1024) // or other buffer size
                            var read: Int
                            while (inputStream!!.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                            }
                            output.flush()
                        } finally {
                            output.close()
                        }
                    } finally {
                        inputStream!!.close()
                        //Upload Bytes.
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "File Uri is null", Toast.LENGTH_LONG).show()
            }
            return file!!
        }

        fun makeEmptyFileWithTitle(title: String?): File? {
            val root = Environment.getExternalStorageDirectory().absolutePath
            return File(root, title)
        }

        /**
         * Get a file path from a Uri. This will get the the path for Storage Access
         * Framework Documents, as well as the _data field for the MediaStore and
         * other file-based ContentProviders.
         *
         * @param context The context.
         * @param uri     The Uri to query.
         * @author paulburke
         */
        private fun getRealPathFromURI(context: Context, uri: Uri): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                } else if (isDownloadsDocument(uri)) {
                    var id = DocumentsContract.getDocumentId(uri)
                    if (id != null) {
                        if (id.startsWith("raw:")) {
                            return id.substring(4)
                        }
                        if (id.startsWith("msf:")) {
                            id = id.substring(4)
                        }
                    }
                    val contentUriPrefixesToTry = arrayOf(
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads"
                    )
                    for (contentUriPrefix in contentUriPrefixesToTry) {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse(contentUriPrefix),
                            java.lang.Long.valueOf(id!!)
                        )
                        try {
                            val path: String = getDataColumn(context, contentUri, null, null)!!
                            if (path != null) {
                                return path
                            }
                        } catch (e: Exception) {
                        }
                    }

                    // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                    val fileName = getFileName(context, uri)
                    val cacheDir = getDocumentCacheDir(context)
                    val file = generateFileName(fileName, cacheDir)
                    var destinationPath: String? = null
                    if (file != null) {
                        destinationPath = file.absolutePath
                        saveFileFromUri(context, uri, destinationPath)
                    }
                    return destinationPath
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {

                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {
            var ips: InputStream? = null
            var bos: BufferedOutputStream? = null
            try {
                ips = context.contentResolver.openInputStream(uri)
                bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
                val buf = ByteArray(1024)
                ips!!.read(buf)
                do {
                    bos.write(buf)
                } while (ips.read(buf) != -1)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    ips?.close()
                    bos?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Get the value of the data column for this Uri. This is useful for
         * MediaStore Uris, and other file-based ContentProviders.
         *
         * @param context       The context.
         * @param uri           The Uri to query.
         * @param selection     (Optional) Filter used in the query.
         * @param selectionArgs (Optional) Selection arguments used in the query.
         * @return The value of the _data column, which is typically a file path.
         */
        fun getDataColumn(
            context: Context,
            uri: Uri?,
            selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context.contentResolver.query(
                    uri!!, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Drive
         */
        fun isGoogleDrive(uri: Uri): Boolean {
            return uri.authority!!.contains("com.google.android.apps.docs.storage")
        }


        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }

        fun openFrontCam(): Camera? {
            var camCount = 0
            var camera: Camera? = null
            val cameraInfo = CameraInfo()
            camCount = Camera.getNumberOfCameras()
            for (i in 0 until camCount) {
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        camera = Camera.open(i)
                        camera.setDisplayOrientation(90)
                    } catch (re: RuntimeException) {
                    }
                }
            }
            return camera
        }

        fun playSendSound(context: Context?, ringId: Int) {
            if (FeatureRestriction.isMessagesSoundEnabled()) {
                val mMediaPlayer = MediaPlayer.create(context, ringId)
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mMediaPlayer.start()
                mMediaPlayer.setOnCompletionListener { mediaPlayer ->
                    var mediaPlayer = mediaPlayer
                    if (mediaPlayer != null) {
                        mediaPlayer.stop()
                        mediaPlayer.release()
                        mediaPlayer = null
                    }
                }
            }
        }

        fun vibrate(context: Context) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(100)
        }

//        fun saveFile(context: Context, imageUri: Uri, messageType: String, sendIntentType : String) :File? {
//            val var1 = Environment.getExternalStorageDirectory().toString() + "/" + context.resources.getString(R.string.app_name) + "/" + "sent/"
//            Utils.createDirectory(var1)
//            if (messageType == CometChatConstants.MESSAGE_TYPE_IMAGE)
//                return saveImageFile(context, var1, imageUri, sendIntentType)
//            else if (messageType == CometChatConstants.MESSAGE_TYPE_VIDEO || messageType == CometChatConstants.MESSAGE_TYPE_AUDIO || messageType == CometChatConstants.MESSAGE_TYPE_FILE)
//                return saveAudioVideoDocumentFiles(context, var1, imageUri, messageType, sendIntentType)
//            return null
//        }

//        private fun saveAudioVideoDocumentFiles(context: Context, var1: String, imageUri: Uri, messageType: String, sendIntentExtensionType: String): File? {
//            var file : File? = null
//            var count : Int? = 0
//            val istream : InputStream? = context.contentResolver.openInputStream(imageUri)
//            if (messageType == CometChatConstants.MESSAGE_TYPE_VIDEO) {
//                val videoPath = var1 + "/" +"video/"
//                Utils.createDirectory(videoPath)
//                val extension = filterExtension(imageUri, sendIntentExtensionType)
//                file = File(videoPath, extension)
//            }
//            else if (messageType == CometChatConstants.MESSAGE_TYPE_AUDIO) {
//                val audioPath = var1 + "/" +"audio/"
//                Utils.createDirectory(audioPath)
//                val extension = filterExtension(imageUri, sendIntentExtensionType)
//                file = File(audioPath, extension)
//            }
//            else if (messageType == CometChatConstants.MESSAGE_TYPE_FILE) {
//                val docPath = var1 + "/" +"document/"
//                Utils.createDirectory(docPath)
//                val extension = filterExtension(imageUri, sendIntentExtensionType)
//                file = File(docPath, extension)
//            }
//            val byte = ByteArray(4096)
//            val out: FileOutputStream = FileOutputStream(file)
//            while (istream?.read(byte).also({ count = it }) != -1) {
//                out.write(byte, 0, count!!)
//            }
//            Log.e("TAG", "saveVideoFile: " + file.toString())
//            // flushing output
//            out.flush()
//            // closing streams
//            out.close()
//            istream?.close()
//
//            return file
//        }

        private fun filterExtension(uri: Uri, extensionType: String): String? {
            Log.e("TAG", "filterExtension: uri " + uri.toString())
            val fileString = uri.toString() //this is your String representing the File
            val lastDot = fileString.lastIndexOf('.')
            Log.e("TAG", "filterExtension: lastdot " + lastDot.toString())
            if (lastDot > 0) {
                val extension = fileString.substring(lastDot)
                Log.e("TAG", "filterExtension: " + extension)
                if (extension.contains(".pdf") || extension.contains(".docx") || extension.contains(
                        ".msword"
                    ) || extension.contains(".vnd.ms.excel") || extension.contains(".mspowerpoint") || extension.contains(
                        ".zip"
                    )
                ) {
                    return uri.lastPathSegment
                } else if (extensionType == "vnd.openxmlformats-officedocument.wordprocessingml.document") {
                    return uri.lastPathSegment + ".docx"
                } else if (extensionType == "vnd.openxmlformats-officedocument.presentationml.presentation") {
                    return uri.lastPathSegment + ".pptx"
                } else {
                    return uri.lastPathSegment + "." + extensionType
                }
            } else return uri.lastPathSegment
        }

//        private fun saveImageFile(context: Context, var1: String, imageUri: Uri, sendIntentType: String) : File {
//            var file :File? = null
//            var imagePath = var1 + "/" +"image/"
//            Utils.createDirectory(imagePath)
//            val istream = context.contentResolver.openInputStream(imageUri)
//            val extension = filterExtension(imageUri, sendIntentType)
//            file = File(imagePath, extension)
//
//            bitmap = BitmapFactory.decodeStream(istream)
//            val bytes = ByteArrayOutputStream()
//            bitmap?.compress(Bitmap.CompressFormat.PNG, 60, bytes)
//
//            var out: OutputStream = FileOutputStream(file)
//            out.write(bytes.toByteArray())
//            istream?.close()
//            out.flush()
//            out.close()
//            return file
//        }

        fun getExtensionType(type: String): String {
            val index = type.lastIndexOf('/')
            val extension = type.substring(index + 1)
            return extension
        }

    }
}