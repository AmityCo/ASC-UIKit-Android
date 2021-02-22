package com.ekoapp.ekosdk.uikit.base

import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class SpacesItemDecoration(
        private val left: Int,
        private val right: Int,
        private val top: Int,
        private val bottom: Int
) : ItemDecoration(), Parcelable {


    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()
    ) {
    }

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
    ) {
        outRect.left = left
        outRect.right = right
        outRect.bottom = bottom;
        outRect.top = top
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(left)
        parcel.writeInt(right)
        parcel.writeInt(top)
        parcel.writeInt(bottom)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SpacesItemDecoration> {
        override fun createFromParcel(parcel: Parcel): SpacesItemDecoration {
            return SpacesItemDecoration(parcel)
        }

        override fun newArray(size: Int): Array<SpacesItemDecoration?> {
            return arrayOfNulls(size)
        }
    }


}