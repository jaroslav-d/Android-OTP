package com.example.androidotp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Response(val id: Int, val data: Float): Parcelable
