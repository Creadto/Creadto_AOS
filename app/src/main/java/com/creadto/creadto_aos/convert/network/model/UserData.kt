package com.creadto.creadto_aos.convert.network.model

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("Status")
    val status : String,
    @SerializedName("Data")
    val data : String?
)
