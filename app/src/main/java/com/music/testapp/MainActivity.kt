package com.music.testapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.music.testapp.adapter.LocationAdapter
import com.music.testapp.databinding.ActivityMainBinding
import com.music.testapp.room.Location
import com.music.testapp.utills.toals
import com.music.testapp.viewModel.LocationViewModel
import com.music.testapp.viewModel.UserState


class MainActivity : AppCompatActivity() {
    private var stts: String = "all"
    private lateinit var binding: ActivityMainBinding
    private lateinit var userViewModel: LocationViewModel

    private var data: List<Location>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        stts = "all"
        setupRecler()
        userViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        userViewModel.getState().observer(this, Observer {
            handleUiState(it)
        })
        binding.fab.setOnClickListener {
            //pindah ke halaman lain
            val intent = Intent(this, ActionDataActivity::class.java)
            intent.putExtra("action", "add")
            startActivity(intent)
        }
        buttonAction()
        initScrollListener()
    }

    //fungsi load more swipe up RecyclerView
    private fun initScrollListener() {
        binding.rvList.layoutManager = LinearLayoutManager(this)

        binding.rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?

                if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == data!!.size - 1) {
                    //bottom of list!
                    if (data!!.size >=19){
                        userViewModel.getLocation(stts, data!![data!!.size-1].id)
                    }
                }

            }
        })
    }


    private fun buttonAction() {

        binding.active.setOnClickListener {
            stts = "active"
            binding.inactive.setBackgroundResource(R.drawable.bg_right_inactive)
            binding.active.setBackgroundResource(R.drawable.bg_center_active)
            binding.allStatus.setBackgroundResource(R.drawable.bg_left_inactive)
            userViewModel.getLocation(stts, 0)

        }

        binding.inactive.setOnClickListener {
            stts = "inactive"
            binding.inactive.setBackgroundResource(R.drawable.bg_right_active)
            binding.active.setBackgroundResource(R.drawable.bg_center_inactive)
            binding.allStatus.setBackgroundResource(R.drawable.bg_left_inactive)
            userViewModel.getLocation(stts, 0)
        }
        binding.allStatus.setOnClickListener {
            stts = "all"
            binding.inactive.setBackgroundResource(R.drawable.bg_right_inactive)
            binding.active.setBackgroundResource(R.drawable.bg_center_inactive)
            binding.allStatus.setBackgroundResource(R.drawable.bg_left_active)
            userViewModel.getLocation(stts, 0)
        }
    }

    private fun handleUiState(it: UserState?) {
        when (it) {
            is UserState.Error -> {
                toals(this, it.err)
            }
            is UserState.ShoewToals -> toals(this, it.message)

            is UserState.Data -> {
                binding.rvList.removeAllViews()
                binding.rvList.removeAllViewsInLayout()
                data = it.locationData
                viewData()
            }
            is UserState.Active -> {
                binding.active.text = "Active(${it.active})"
            }
            is UserState.InActive -> {
                binding.inactive.text = "Inactive(${it.inactive})"
            }
            is UserState.AllData -> {
                binding.allStatus.text = "ALL(${it.alldata})"
            }
        }
    }

    private fun viewData() {
        binding.rvList.adapter?.let { adapter ->
            if (adapter is LocationAdapter) {
                adapter.setLagu(data!!)
            }
        }
    }

    private fun setupRecler() {
        binding.rvList.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            adapter = LocationAdapter(mutableListOf(), this@MainActivity)
        }
    }

    override fun onResume() {
        super.onResume()
        userViewModel.getLocation(stts, 0)
    }
}