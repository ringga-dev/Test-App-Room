package com.music.testapp.utills

import android.content.Context
import android.widget.Toast

fun toals(context: Context, text :String){
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}