package com.epsilonlabs.uavpathcalculator.activities.result

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.epsilonlabs.uavpathcalculator.R
import com.epsilonlabs.uavpathcalculator.database.AppApplication
import com.epsilonlabs.uavpathcalculator.database.entities.UavEntity
import com.epsilonlabs.uavpathcalculator.databinding.ActivityResultBinding
import com.epsilonlabs.uavpathcalculator.utils.AlertUtils
import com.epsilonlabs.uavpathcalculator.utils.MarkerParcelable
import com.epsilonlabs.uavpathcalculator.utils.tsp.ResultData
import com.epsilonlabs.uavpathcalculator.utils.tsp.ScheduleCreator
import com.epsilonlabs.uavpathcalculator.utils.tsp.TSP
import com.epsilonlabs.uavpathcalculator.viewmodels.UavViewModel
import com.epsilonlabs.uavpathcalculator.viewmodels.UavViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
            try {
                val data = calculateSchedule()
                inflateTable(data)
            } catch (e: IllegalArgumentException) {
                AlertUtils.showOkAlert(this, "Error", e.message!!)
            }

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
    private fun calculateSchedule() : ArrayList<ResultData> {
        val path = this.intent.extras?.get("PATH") as ArrayList<MarkerParcelable>
        val uav = binding.uavSpinner.selectedItem as UavEntity
        return ScheduleCreator(
            uav,
            path,
            LocalTime.NOON, //TODO make settings for this
            Duration.ofMinutes(3),
            Duration.ofMinutes(2),
            100
        ).schedule
    }
    private fun inflateTable(resultArray: ArrayList<ResultData>) {
        val table = binding.scheduleTable
        //clear all rows
        table.removeAllViews()
        //row header
        val header = LayoutInflater.from(this).inflate(R.layout.table_row, null)
        table.addView(header)
        //add data to table
        for(result in resultArray) {
            //row setup
            val row = LayoutInflater.from(this).inflate(R.layout.table_row, null)
            if(result == resultArray.last()) {
                row.setBackgroundResource(R.drawable.table_row_last_bg)
            }
            row.findViewById<TextView>(R.id.row_point).text = result.marker.title
            row.findViewById<TextView>(R.id.row_aerial_vehicle).text = "TODO"
            row.findViewById<TextView>(R.id.row_arrival).text = result.arrivalTime.toString()
            row.findViewById<TextView>(R.id.row_time_spent).text = result.timeSpent.toString()
            row.findViewById<TextView>(R.id.row_departure).text = result.departureTime.toString()
            row.findViewById<TextView>(R.id.row_battery_time).text = result.batteryTimeLeft.toString()
            table.addView(row)
        }
    }
}