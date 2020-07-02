package pt.lisomatrix.safevault.extensions

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

fun File.delete(context: Context): Boolean {
    var selectionArgs = arrayOf(this.absolutePath)
    val contentResolver = context.contentResolver
    var where: String? = null
    var filesUri: Uri? = null
    if (android.os.Build.VERSION.SDK_INT >= 29) {
        filesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        where = MediaStore.Images.Media._ID + "=?"
        selectionArgs = arrayOf(this.name)
    } else {
        where = MediaStore.MediaColumns.DATA + "=?"
        filesUri = MediaStore.Files.getContentUri("external")
    }

    val int = contentResolver.delete(filesUri!!, where, selectionArgs)

    return !this.exists()
}