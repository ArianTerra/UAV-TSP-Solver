package com.epsilonlabs.uavpathcalculator.activities.result

import android.os.AsyncTask
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.epsilonlabs.uavpathcalculator.R
import com.epsilonlabs.uavpathcalculator.database.AppApplication
import com.epsilonlabs.uavpathcalculator.database.AppDatabase
import com.epsilonlabs.uavpathcalculator.database.entities.UavEntity
import com.epsilonlabs.uavpathcalculator.databinding.ActivityMainBinding
import com.epsilonlabs.uavpathcalculator.databinding.ActivityResultBinding
import com.epsilonlabs.uavpathcalculator.utils.MarkerParcelable
import com.epsilonlabs.uavpathcalculator.utils.tsp.TSP
import com.epsilonlabs.uavpathcalculator.viewmodels.UavViewModel
import com.epsilonlabs.uavpathcalculator.viewmodels.UavViewModelFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.start
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val uavViewModel: UavViewModel by viewModels {
        UavViewModelFactory((application as AppApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fillSpinner()
        findViewById<FloatingActionButton>(R.id.fab_fill_table).setOnClickListener {
            calculateSchedule()
        }

    }
    private fun fillSpinner() {
        val uavList: ArrayList<UavEntity> = arrayListOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, uavList)
        uavViewModel.allUavsLive.observe(this, Observer {
            uavList.clear()
            for (uav in it) {
                uavList.add(uav)
            }
            adapter.notifyDataSetChanged()
        })
        val spinner = findViewById<Spinner>(R.id.uav_spinner)
        spinner.adapter = adapter
    }
    private fun calculateSchedule() {
        val path = this.intent.extras?.get("PATH") as ArrayList<MarkerParcelable>
        val uav = binding.uavSpinner.selectedItem as UavEntity
        val data = TSP.createSchedule(
            uav,
            path,
            LocalTime.NOON,
            Duration.ofMinutes(3),
            Duration.ofMinutes(2),
            100
        )
    }
    private fun inflateTable(data: ArrayList<Marker>) {
        val table = binding.scheduleTable
        //clear all rows except first one
        while(table.childCount > 1) {
            table.removeView(table.getChildAt(table.childCount - 1))
        }


    }
}