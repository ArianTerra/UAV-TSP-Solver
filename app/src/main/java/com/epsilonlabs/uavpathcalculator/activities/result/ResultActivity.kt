package com.epsilonlabs.uavpathcalculator.activities.result

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.epsilonlabs.uavpathcalculator.R
import com.epsilonlabs.uavpathcalculator.database.AppApplication
import com.epsilonlabs.uavpathcalculator.utils.SimpleToast
import com.epsilonlabs.uavpathcalculator.viewmodels.UavViewModel
import com.epsilonlabs.uavpathcalculator.viewmodels.UavViewModelFactory

class ResultActivity : AppCompatActivity() {
    private val uavViewModel: UavViewModel by viewModels {
        UavViewModelFactory((application as AppApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        uavViewModel.getById(2).observe(this) {
            SimpleToast.show(this, it.name!!)
        }

        val uavList: ArrayList<String> = arrayListOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, uavList)
        val spinner = findViewById<Spinner>(R.id.uav_spinner)
        spinner.adapter = adapter

        uavViewModel.allUavs.observe(this) {
            for (uav in it) {
                uavList.add(uav.name!!)
            }
            adapter.notifyDataSetChanged()
        }
    }
}