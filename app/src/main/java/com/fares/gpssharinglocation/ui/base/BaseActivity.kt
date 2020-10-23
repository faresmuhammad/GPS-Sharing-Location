package com.fares.gpssharinglocation.ui.base

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fares.gpssharinglocation.R
import dagger.android.support.DaggerAppCompatActivity

typealias layout = R.layout
typealias drawable = R.drawable
typealias string = R.string

private const val TAG = "BaseActivity"

abstract class BaseActivity() : DaggerAppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }


    fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


    fun showProgressBar(p: ProgressBar) {
        p.visibility = View.VISIBLE
    }

    fun hideProgressBar(p: ProgressBar) {
        p.visibility = View.GONE
    }

    fun setRecyclerView(
        recyclerView: RecyclerView,
        recyclerAdapter: RecyclerView.Adapter<*>,
        recyclerLayoutManager: LinearLayoutManager
    ) {
        recyclerView.apply {
            adapter = recyclerAdapter
            layoutManager = recyclerLayoutManager
        }
    }


}