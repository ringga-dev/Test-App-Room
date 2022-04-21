package com.music.testapp.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.music.testapp.ActionDataActivity
import com.music.testapp.R
import com.music.testapp.databinding.ItemListBinding
import com.music.testapp.room.Location

class LocationAdapter(
    private var lagus: MutableList<Location>,
    private var context: Context
) : RecyclerView.Adapter<LocationAdapter.PageHolder>() {

    inner class PageHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemListBinding.bind(view)
    }

    override fun onBindViewHolder(holder: PageHolder, position: Int) {
        with(holder) {
            binding.name.text = lagus[position].name

            if (lagus[position].stts == "active") {
                binding.status.text = "Online Booking Enabled"
                binding.sttss.text = ""
            } else {
                binding.sttss.text = "Inactive"
                binding.status.text= ""
            }

            //cek ganjil atau genap
            if (position % 2 == 0) {
                binding.item.setCardBackgroundColor(Color.parseColor("#BFBFBF"))
            }else{
                binding.item.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }


            binding.ditail.setOnClickListener {
                val intent = Intent(context, ActionDataActivity::class.java)
                intent.putExtra("action", "edit")
                intent.putExtra("id", lagus[position].id)
                context.startActivity(intent)
            }
        }
    }

    fun setLagu(r: List<Location>) {
        lagus.clear()
        lagus.addAll(r)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
        return PageHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_list, parent, false)
        )
    }

    override fun getItemCount() = lagus.size
}