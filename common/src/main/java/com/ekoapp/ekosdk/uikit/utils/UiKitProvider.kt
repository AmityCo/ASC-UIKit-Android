package com.ekoapp.ekosdk.uikit.utils

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

class UiKitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("Not yet implemented")
    }
}