package com.epsilonlabs.uavpathcalculator.activities.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.epsilonlabs.uavpathcalculator.R
import com.epsilonlabs.uavpathcalculator.activities.result.ResultActivity
import com.epsilonlabs.uavpathcalculator.databinding.ActivityMainBinding
import com.epsilonlabs.uavpathcalculator.utils.AlertUtils
import com.epsilonlabs.uavpathcalculator.utils.tsp.TSP
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.epsilonlabs.uavpathcalculator.utils.MarkerParcelable
import com.epsilonlabs.uavpathcalculator.utils.tsp.TspDynamicProgramming
import com.epsilonlabs.uavpathcalculator.utils.tsp.TspNearestNeighbor

/*
* TODO
*  Add a text prompt to name a marker while creating
*  Add settings
*   - autonaming markers switch
*  Show current editor state (adding/removing node)
* */

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private enum class EditorState {
        DEFAULT, // default state, do nothing
        ADD_NODE, //add default node
        ADD_START,  //add starting node
        REMOVE  //remove node
    }

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var map: GoogleMap

    //variables
    private lateinit var allMarkers : ArrayList<Marker>
    private lateinit var editorState: EditorState
    private var startNode : Marker? = null
    private var polylinePath : Polyline? = null
    private var canContinue: Boolean = false
    private lateinit var path: ArrayList<MarkerParcelable>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //initialize variables
        allMarkers = ArrayList()
        editorState = EditorState.DEFAULT

        //set up buttons events
        binding.fabAddNode.setOnClickListener {
            editorState = EditorState.ADD_NODE
        }
        binding.fabAddStart.setOnClickListener {
            editorState = EditorState.ADD_START
        }
        binding.fabRemove.setOnClickListener {
            editorState = EditorState.REMOVE
        }
        binding.fabRemoveAll.setOnClickListener {
            removeAllNodesButtonEvent()
        }
        //fill spinner
        val list = arrayListOf(
            TspDynamicProgramming(),
            TspNearestNeighbor()
            //TspBranching(), //TODO
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
        binding.algorithmSpinner.adapter = adapter
        binding.algorithmSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                canContinue = false
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        //map on click
        map = googleMap
        map.setOnMapClickListener {
            addMarkerButtonsEvent(it)
        }
        //move camera to KHAI
        map.setOnMapLoadedCallback {
            val khaiBound = LatLngBounds(
                LatLng(50.039419, 36.275588), //SW
                LatLng(50.046652, 36.295396)  //NE
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(khaiBound.center, 15f))
        }
        // Set a listener for marker click.
        map.setOnMarkerClickListener(this)

        // continue button
        binding.fabContinue.setOnClickListener {
            resultButtonEvent()
        }
    }

    private fun resultButtonEvent() {
//        val intent = Intent(this@MainActivity, ResultActivity::class.java)
//        startActivity(intent)
        if(canContinue) {
            val intent = Intent(this@MainActivity, ResultActivity::class.java)
            intent.putExtra("PATH", path)
            startActivity(intent)
            canContinue = false
            return
        }
        if(allMarkers.size < 3) {
            AlertUtils.showShortToast(this, "Add more markers!")
            canContinue = false
        } else {
            drawTSP()
            //compute.backgroundTintList = ColorStateList.valueOf(Color.rgb(255, 50, 50))
            AlertUtils.showShortToast(this, "Click again for result")
            canContinue = true
        }
    }
    private fun removeAllNodesButtonEvent() {
        //TODO make confirmation window
        for (marker in allMarkers) {
            marker.remove()
        }
        allMarkers.clear()
        startNode = null
        polylinePath?.remove()
        canContinue = false
        AlertUtils.showShortToast(this, "All markers removed")
    }

    private fun addMarkerButtonsEvent(it: LatLng) {
        canContinue = false
        when (editorState) {
            EditorState.ADD_START -> {
                if (startNode == null) {
                    addStartNode(it)
                } else {
                    startNode!!.position = it
                    AlertUtils.showShortToast(this, "Moving start node...")
                }

            }
            EditorState.ADD_NODE -> {
                if(startNode == null) {
                    addStartNode(it)
                    AlertUtils.showShortToast(this, "Creating start node...")
                } else {
                    val marker = map.addMarker( //todo
                        MarkerOptions().position(it).title(getString(R.string.point_name) +
                                (allMarkers.size).toString())
                    )
                    if (marker != null) {
                        marker.tag = NodeType.DEFAULT
                        allMarkers.add(marker)
                    }
                }
            }
            else -> {}
        }
        editorState = EditorState.DEFAULT
    }

    private fun addStartNode(it: LatLng) {
        val marker = map.addMarker(
            MarkerOptions().position(it).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            ).title(getString(R.string.start_point_name))
        )
        marker?.tag = NodeType.START
        if (marker != null) {
            allMarkers.add(marker)
        }
        startNode = marker
    }
    private fun drawTSP() {
        polylinePath?.remove()
        val markers = allMarkers.mapTo(ArrayList<MarkerParcelable>()) {
            MarkerParcelable(it.title!!, it.position.latitude, it.position.longitude)
        }

        val tsp = binding.algorithmSpinner.selectedItem as TSP
        path = tsp.calculatePath(markers)
        AlertUtils.showShortToast(this, "Path: ${TSP.calculatePathLength(path)} m.")

        val options = PolylineOptions()
            .width(25f)
            .color(Color.BLUE).addAll(path.map { LatLng(it.latitude, it.longitude) })

        polylinePath = map.addPolyline(options)
    }
    override fun onMarkerClick(marker: Marker): Boolean {
        if (editorState == EditorState.REMOVE) {
            if(marker.tag == NodeType.START) {
                AlertUtils.showShortToast(this,"Can't delete start node")
            } else {
                allMarkers.remove(marker)
                marker.remove()
                polylinePath?.remove()
                canContinue = false
                editorState = EditorState.DEFAULT
                AlertUtils.showShortToast(this,"Marker deleted")
            }
            return true
        }
        return false
    }
}