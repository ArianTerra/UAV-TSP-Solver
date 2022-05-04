package com.epsilonlabs.uavpathcalculator.activities.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.epsilonlabs.uavpathcalculator.R
import com.epsilonlabs.uavpathcalculator.activities.result.ResultActivity
import com.epsilonlabs.uavpathcalculator.databinding.ActivityMainBinding
import com.epsilonlabs.uavpathcalculator.utils.SimpleToast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

/*
* TODO
*  Add button to clear all markers
*  Add a text prompt to name a marker while creating
*  Implement UAV time calculation
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
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
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
        if(canContinue) {
            val intent = Intent(this@MainActivity, ResultActivity::class.java)
            startActivity(intent)
            return
        }
        if(allMarkers.size < 3) {
            SimpleToast.show(this, "Add more markers!")
            canContinue = false
        } else {
            drawTSP()
            //compute.backgroundTintList = ColorStateList.valueOf(Color.rgb(255, 50, 50))
            SimpleToast.show(this, "Click again for result")
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
        SimpleToast.show(this, "All markers removed")
    }

    private fun addMarkerButtonsEvent(it: LatLng) {
        when (editorState) {
            EditorState.ADD_START -> {
                if (startNode == null) {
                    addStartNode(it)
                } else {
                    startNode!!.position = it
                    SimpleToast.show(this, "Moving start node...")
                }

            }
            EditorState.ADD_NODE -> {
                if(startNode == null) {
                    addStartNode(it)
                    SimpleToast.show(this, "Creating start node...")
                } else {
                    val marker = map.addMarker(MarkerOptions().position(it))
                    if (marker != null) {
                        marker.tag = NodeType.DEFAULT
                        allMarkers.add(marker)
                    }
                }
            }
        }
        editorState = EditorState.DEFAULT
    }

    private fun addStartNode(it: LatLng) {
        val marker = map.addMarker(
            MarkerOptions().position(it).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        )
        marker?.tag = NodeType.START
        if (marker != null) {
            allMarkers.add(marker)
        }
        startNode = marker
    }
    private fun drawTSP() {
        polylinePath?.remove()
        val values = arrayListOf<LatLng>()
        for (v in allMarkers) values.add(v.position)

        val tsp = TSP(values)
        val markersPath = tsp.calculateNearestNeighbor()
        val path = arrayListOf<LatLng>()
        for (a in markersPath) {
            path.add(a)
        }

        val options = PolylineOptions()
            .width(25f)
            .color(Color.BLUE).addAll(path)

        polylinePath = map.addPolyline(options)
    }
    override fun onMarkerClick(marker: Marker): Boolean {
        if (editorState == EditorState.REMOVE) {
            if(marker.tag == NodeType.START) {
                SimpleToast.show(this,"Can't delete start node")
            } else {
                allMarkers.remove(marker)
                marker.remove()
                SimpleToast.show(this,"Marker deleted")
            }
        }
        return true
    }
}