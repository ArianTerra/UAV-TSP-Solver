package com.epsilonlabs.uavpathcalculator.main

enum class EditorState {
    DEFAULT, // default state, do nothing
    ADD_NODE, //add default node
    ADD_START,  //add starting node
    //ADD_END,  //add ending node
    REMOVE  //remove node
}