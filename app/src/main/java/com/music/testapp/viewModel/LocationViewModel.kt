package com.music.testapp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.room.Room
import com.music.testapp.room.AppDatabase
import com.music.testapp.room.Location
import com.music.testapp.utills.SingleLiveEvent

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val context by lazy { getApplication<Application>().applicationContext }
    private lateinit var db: AppDatabase

    private var state: SingleLiveEvent<UserState> = SingleLiveEvent()

    fun getLocation(stts: String, start:Int) {
        this.getCountData()
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "database-test"
        ).allowMainThreadQueries().build()

        val userDao = db.locationDao()
        val users: List<Location> =
            if (stts == "all") userDao.getLocationAll(start) else userDao.getLocation(stts,start)
        if (users.isNotEmpty()) {
            state.value = UserState.Data(users)
        } else {
            state.value = UserState.Error("No data Registered")
        }
    }

    fun getCountData() {
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "database-test"
        ).allowMainThreadQueries().build()

        val userDao = db.locationDao()
        val users: List<Location> = userDao.getCount()

        var active = 0
        var inactive = 0
        if (users.isNotEmpty()) {
            //hitung jumlah data yang aktive dan non aktif
            for (i in users.indices) {
                if (users[i].stts == "active") {
                    active++
                } else {
                    inactive++
                }
            }
            state.value = UserState.Active(active)
            state.value = UserState.InActive(inactive)
            state.value = UserState.AllData(active + inactive)
        } else {
            state.value = UserState.Error("No data Registered")
        }
    }

    //fungsi update by id
    fun updateLocation(id:Int,data: Location) {
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "database-test"
        ).allowMainThreadQueries().build()

        val userDao = db.locationDao()
        userDao.update(id,data.name, data.address!!,data.city!!,data.zip_code!!,data.lat_long!!,data.stts!!)
        state.value = UserState.Error("Update Success")
    }

    fun getDataById(id:Int) {
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "database-test"
        ).allowMainThreadQueries().build()

        val userDao = db.locationDao()
        val users: Location = userDao.getLocationById(id)
        if (users.id != 0) {
            state.value = UserState.DataById(users)
        } else {
            state.value = UserState.Error("No data Registered")
        }
    }


    fun saveLocation(location: Location) {
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "database-test"
        ).allowMainThreadQueries().build()

        val userDao = db.locationDao()
        userDao.insert(location)
        state.value = UserState.Success("Location Saved")
    }
    fun delete(id: Int) {
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "database-test"
        ).allowMainThreadQueries().build()

        val userDao = db.locationDao()
        userDao.delete(id)
        state.value = UserState.Success("Location Deleted")
    }

    fun getState() = state


}

sealed class UserState {
    data class Error(var err: String) : UserState()
    data class ShoewToals(var message: String) : UserState()

    data class Active(var active: Int) : UserState()
    data class InActive(var inactive: Int) : UserState()
    data class AllData(var alldata: Int) : UserState()

    data class IsLoding(var state: Boolean = false) : UserState()
    data class Data(var locationData: List<Location>) : UserState()
    data class DataById(var locationData: Location) : UserState()
    data class Failed(var message: String) : UserState()
    data class Success(var message: String) : UserState()

    object Reset : UserState()
}