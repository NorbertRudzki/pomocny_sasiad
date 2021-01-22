package com.example.pomocnysasiad.model

import com.google.firebase.firestore.GeoPoint

data class Filter(
    val nearbyPoints: List<GeoPoint>,
    val longZone: List<Int>,
    val longNearbyPoints: List<Double>,
    var categoryList: List<Int>
)
