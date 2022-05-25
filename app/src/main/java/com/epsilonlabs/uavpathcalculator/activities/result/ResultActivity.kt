package com.epsilonlabs.uavpathcalculator.activities.result

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.epsilonlabs.uavpathcalculator.R
import com.epsilonlabs.uavpathcalculator.database.AppApplication
import com.epsilonlabs.uavpathcalculator.database.entities.UavEntity
import com.epsilonlabs.uavpathcalculator.databinding.ActivityResultBinding
import com.epsilonlabs.uavpathcalculator.utils.AlertUtils
import com.epsilonlabs.uavpathcalculator.utils.MarkerParcelable
import com.epsilonlabs.uavpathcalculator.utils.schedule.ResultData
import com.epsilonlabs.uavpathcalculator.utils.schedule.ScheduleCreator
import com.epsilonlabs.uavpathcalculator.viewmodels.UavViewModel
import com.epsilonlabs.uavpathcalculator.viewmodels.UavViewModelFactory
import java.time.Duration
import java.time.LocalTime

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val uavViewModel: UavViewModel by viewModels {
        UavViewModelFactory((application as AppApplication).repository)
    }
    private var departureTime: LocalTime = LocalTime.of(1,0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fillSpinner()

        binding.resultButton.setOnClickListener {
            try {
                val data = calculateSchedule()
                inflateTable(data)
                //setUavInfoText()
            } catch (e: IllegalArgumentException) {
                AlertUtils.showOkAlert(this, "Error", e.message!!)
            }
        }
        binding.timePickerButton.setOnClickListener {
            showTimePicker()
        }
        binding.uavSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                setUavInfoText()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

    }

    /**
     * Connects to DB and gets all UAV data from it to spinner
     */
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

    /**
     * Reads all values from input and creates table data
     */
    private fun calculateSchedule() : ArrayList<ResultData> {
        val path = this.intent.extras?.get("PATH") as ArrayList<MarkerParcelable>
        val uav = binding.uavSpinner.selectedItem as UavEntity
        //parse input settings
        val timeMonitoring = binding.timeMonitoringInput.text.toString().toIntOrNull()
        val timeCharging = binding.timeChargingInput.text.toString().toIntOrNull()
        val abrasSpeed = binding.abrasSpeedInput.text.toString().toIntOrNull()
        if(timeMonitoring == null || timeMonitoring <= 0) {
            throw IllegalArgumentException("Monitoring time is set to wrong value")
        }
        if(timeCharging == null || timeCharging <= 0) {
            throw IllegalArgumentException("Charging time is set to wrong value")
        }
        if(abrasSpeed == null || abrasSpeed <= 0) {
            throw IllegalArgumentException("ABRAS speed is set to wrong value")
        }

        return ScheduleCreator(
            uav,
            path,
            departureTime,
            Duration.ofMinutes(timeMonitoring.toLong()),
            Duration.ofMinutes(timeCharging.toLong()),
            abrasSpeed
        ).schedule
    }

    /**
     * Draws table
     */
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
            row.findViewById<TextView>(R.id.row_aerial_vehicle).text = result.uavName
            row.findViewById<TextView>(R.id.row_arrival).text = result.arrivalTime?.toString().orEmpty()
            row.findViewById<TextView>(R.id.row_time_spent).text = result.timeSpent?.toString().orEmpty()
            row.findViewById<TextView>(R.id.row_departure).text = result.departureTime?.toString().orEmpty()
            row.findViewById<TextView>(R.id.row_battery_time).text = result.batteryTimeLeft?.toString().orEmpty()
            table.addView(row)
        }
    }

    private fun setUavInfoText() {
        val uav = binding.uavSpinner.selectedItem as UavEntity
        binding.uavInfoText.text = "ID: ${uav.id} Name: ${uav.name} Speed: ${uav.speed} km/h " +
                "Battery time: ${uav.flightTime} m"
    }

    /**
     * Time picker pop up window
     */
    private fun showTimePicker() {
        val listener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            binding.timePickerButton.text = "%02d:%02d".format(hour, minute)
            departureTime = LocalTime.of(hour, minute)
        }
        val timePickerDialog = TimePickerDialog(this, listener, departureTime.hour,
            departureTime.minute, true)
        timePickerDialog.setTitle("Select time:")
        timePickerDialog.show()
    }
}