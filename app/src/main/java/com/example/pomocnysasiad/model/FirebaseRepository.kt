package com.example.pomocnysasiad.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.util.*
import kotlin.collections.ArrayList

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val cloud = FirebaseFirestore.getInstance()
    private var listenerAllRequestsRegistration: ListenerRegistration? = null
    private var listenerNewRequestRegistration: ListenerRegistration? = null

    init {
        //todo raczej pobrac z locale, chyba, że target 100% polsza
        auth.setLanguageCode("pl")

    }

    //requests
    fun insertRequest(request: Request) {
        cloud.collection("requestsForHelp").add(request)
    }

    fun insertShoppingListForRequest(request: Request, list: ProductsListWrapper) {
        cloud.collection("requestsForHelp").document("shoppingLists")
            .collection("shoppingLists")
            .document(request.id.toString())
            .set(list)
    }

    fun getNearbyRequests(filter: Filter): LiveData<List<Request>> {
        val requests = MutableLiveData<List<Request>>()
        listenerAllRequestsRegistration?.remove()
        listenerAllRequestsRegistration = cloud.collection("requestsForHelp")
            .whereGreaterThanOrEqualTo("location", filter.nearbyPoints[0])
            .whereLessThanOrEqualTo("location", filter.nearbyPoints[1])
            .whereIn("longitudeZone", filter.longZone)
            .addSnapshotListener { value, _ ->
                if (value != null && !value.isEmpty) {
                    Log.d("getNearbyRequests", "triggered nor empty")
                    val list = ArrayList<Request>()
                    for (req in value.documents) {
                        val data = req.toObject(Request::class.java)
                        data?.let { list.add(it) }
                    }
                    requests.postValue(list.filter { req ->
                        req.location.longitude >= filter.longNearbyPoints[0]
                                && req.location.longitude <= filter.longNearbyPoints[1]
                    })

                }
            }

        return requests
    }

    fun noticeNewRequest(filter: Filter): LiveData<Request> {
        val request = MutableLiveData<Request>()
        listenerNewRequestRegistration?.remove()
        listenerNewRequestRegistration = cloud.collection("requestsForHelp")
            .orderBy("id")
            .limitToLast(2)
            //.whereGreaterThanOrEqualTo("location", filter.nearbyPoints[0])
            //.whereGreaterThanOrEqualTo("id", Calendar.getInstance().timeInMillis)
            //.whereLessThanOrEqualTo("location", filter.nearbyPoints[1])
            .whereIn("longitudeZone", filter.longZone)
            .addSnapshotListener { value, _ ->
                Log.d("noticeNewRequest", "triggered")
                if (value != null && !value.isEmpty) {
                    Log.d("noticeNewRequest", "triggered nor empty")
                    val list = ArrayList<Request>()
                    val listOfNew = ArrayList<Request>()
                    for (req in value.documents) {
                        val data = req.toObject(Request::class.java)
                        data?.let { list.add(it) }
                    }
                    for (req in value.documentChanges) {
                        if (req.type == DocumentChange.Type.ADDED) {
                            val data = req.document.toObject(Request::class.java)
                            listOfNew.add(data)
                            Log.d("dodano", data.toString())
                        }
                    }
                    Log.d("Nowe size", listOfNew.size.toString())
                    Log.d("Wszystkie size", list.size.toString())

                    if (listOfNew.size != list.size
                        && listOfNew[0].location.longitude >= filter.longNearbyPoints[0]
                        && listOfNew[0].location.longitude <= filter.longNearbyPoints[1]) {
                        request.postValue(listOfNew[0])
                    }
                }
            }
        return request
    }

    //users
    fun createNewUser() {
        cloud.collection("users").document(auth.currentUser!!.uid).set(
            hashMapOf(
                "id" to auth.currentUser!!.uid,
                "name" to auth.currentUser!!.displayName,
                "tokens" to 5,
                "score" to 0
            )
        )
    }

    fun getUser(): LiveData<User> {
        val user = MutableLiveData<User>()
        if (auth.currentUser != null) {
            cloud.collection("users").document(auth.currentUser!!.uid)
                .addSnapshotListener { value, _ ->
                    if (value != null && value.exists()) {
                        user.postValue(value.toObject(User::class.java))
                    }
                }
        }
        return user
    }

    fun decreaseUsersToken() {
        cloud.collection("users").document(auth.currentUser!!.uid)
            .update("tokens", FieldValue.increment(-1))
    }

    fun increaseUsersToken() {
        cloud.collection("users").document(auth.currentUser!!.uid)
            .update("tokens", FieldValue.increment(1))
    }

    fun isLogoutUserOrNotVerified() =
        (auth.currentUser == null || (!auth.currentUser!!.isEmailVerified && auth.currentUser!!.phoneNumber == null))

    fun isUserVerified(): Boolean {
        auth.currentUser!!.reload()
        return auth.currentUser!!.isEmailVerified || auth.currentUser!!.phoneNumber != null
    }

    fun sendEmailVerif() {
        auth.currentUser!!.sendEmailVerification()
    }
}