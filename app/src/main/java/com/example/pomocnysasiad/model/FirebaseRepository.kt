package com.example.pomocnysasiad.model

import android.text.BoringLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val cloud = FirebaseFirestore.getInstance()

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

    fun getAllRequests(): LiveData<List<Request>> {
        //todo tylko testowo, pozniej tylko dla podanej lokalizacji
        val requests = MutableLiveData<List<Request>>()
        cloud.collection("requestsForHelp").addSnapshotListener { value, error ->
            if (value != null && !value.isEmpty) {
                val list = ArrayList<Request>()
                for (req in value.documents) {
                    val data = req.toObject(Request::class.java)
                    data?.let { list.add(it) }
                }
                requests.postValue(list.toList())
            }
        }
        return requests
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