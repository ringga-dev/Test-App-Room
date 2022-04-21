package com.music.testapp.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Location(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "city") val city: String?,
    @ColumnInfo(name = "zip_code") val zip_code: String?,
    @ColumnInfo(name = "lat_long") val lat_long: String?,
    @ColumnInfo(name = "stts") val stts: String?,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}