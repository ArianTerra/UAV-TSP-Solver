package com.epsilonlabs.uavpathcalculator.main

import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.epsilonlabs.uavpathcalculator.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.epsilonlabs.uavpathcalculator.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nambimobile.widgets.efab.FabOption

/*
* TODO
*  Show current editor state (adding/removing node)
*
* */

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private val TAG = "MainActivity"
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    //buttons
    private lateinit var addDefault : FabOption
    private lateinit var addStart : FabOption
    private lateinit var remove : FabOption
    private lateinit var compute : FloatingActionButton
    //variables
    private lateinit var allMarkers : ArrayList<Marker>
    private lateinit var editorState: EditorState
    private var startNode : Marker? = null
    //private var endNode : Marker? = null
    private var polylinePath : Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //get controls
        addDefault = findViewById(R.id.fab_add_node)
        addStart = findViewById(R.id.fab_add_start)
        //addEnd = findViewById(R.id.fab_add_end)
        remove = findViewById(R.id.fab_remove)
        compute = findViewById(R.id.fab_continue)
        //initialize variables
        allMarkers = ArrayList()
        editorState = EditorState.DEFAULT

        //set up buttons events
        addDefault.setOnClickListener {
            editorState = EditorState.ADD_NODE
        }
        addStart.setOnClickListener {
            editorState = EditorState.ADD_START
        }
//        addEnd.setOnClickListener {
//            editorState = EditorState.ADD_END
//        }
        remove.setOnClickListener {
            editorState = EditorState.REMOVE
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

        compute.setOnClickListener {
            if(allMarkers.size < 3) {
                showToast("Add more markers!")
            } else {
                drawTSP()
            }
        }
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

    private fun addMarkerButtonsEvent(it: LatLng) {
        when (editorState) {
            EditorState.ADD_START -> {
                if (startNode == null) {
                    addStartNode(it)
                } else {
                    startNode!!.position = it;
                    showToast("Moving start node...")
                }

            }
            EditorState.ADD_NODE -> {
                if(startNode == null) {
                    addStartNode(it)
                    showToast("Creating start node...")
                } else {
                    val marker = map.addMarker(MarkerOptions().position(it))
                    marker.tag = NodeType.DEFAULT
                    allMarkers.add(marker)
                }
            }
            else -> {
                //do nothing
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
        marker.tag = NodeType.START
        allMarkers.add(marker)
        startNode = marker
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (editorState == EditorState.REMOVE) {
            if(marker.tag == NodeType.START) {
                showToast("Can't delete start node")
            } else {
                allMarkers.remove(marker)
                marker.remove()
                showToast("Marker deleted")
            }
        }
        return true
    }

    private fun showToast(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }
}