package com.music.testapp.room

import androidx.room.*

@Dao
interface LocationDao {
//menampilkan data dengan limit 20 dan start

    @Query("SELECT * FROM location where stts = :stts LIMIT 20 OFFSET :start")
    fun getLocation(stts: String, start: Int): List<Location>

    @Query("SELECT * FROM location  LIMIT 20 OFFSET :start")
    fun getLocationAll(start: Int): List<Location>

    @Query("SELECT * FROM location")
    fun getCount(): List<Location>

    //get data by id
    @Query("SELECT * FROM location WHERE id = :id")
    fun getLocationById(id: Int): Location


    @Insert
    fun insert(vararg chatDb: Location)

    //update
    @Query("UPDATE location SET name = :name, address=:address,city=:city,zip_code=:zip_code,lat_long=:lat_long, stts = :stts WHERE id = :id")
    fun update(
        id: Int,
        name: String,
        address: String,
        city: String,
        zip_code: String,
        lat_long: String,
        stts: String
    )


    @Query("DELETE FROM location WHERE id = :id")
    fun delete(id: Int)
}