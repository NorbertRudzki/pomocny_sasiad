package com.example.pomocnysasiad.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.*
import kotlin.collections.ArrayList

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val cloud = FirebaseFirestore.getInstance()
    private var listenerAllRequestsRegistration: ListenerRegistration? = null
    private var listenerNewRequestRegistration: ListenerRegistration? = null
    private var listenerChat: ListenerRegistration? = null

    init {
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

    fun deleteShoppingListForRequest(id: Long) {
        cloud.collection("requestsForHelp").document("shoppingLists")
            .collection("shoppingLists")
            .document(id.toString()).delete()
    }

    fun getUserId() = auth.currentUser!!.uid

    fun getUserName() = auth.currentUser!!.displayName

    fun getNearbyRequests(filter: Filter): LiveData<List<Request>> {
        val requests = MutableLiveData<List<Request>>()
        listenerAllRequestsRegistration?.remove()
        listenerAllRequestsRegistration = cloud.collection("requestsForHelp")
            .whereGreaterThanOrEqualTo("location", filter.nearbyPoints[0])
            .whereLessThanOrEqualTo("location", filter.nearbyPoints[1])
            .whereIn("longitudeZone", filter.longZone)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    // if (value != null && !value.isEmpty) {
                    Log.d("getNearbyRequests", "triggered nor empty")
                    val list = ArrayList<Request>()
                    for (req in value.documents) {
                        val data = req.toObject(Request::class.java)
                        Log.d("getNearbyRequests", data.toString())
                        data?.let { list.add(it) }
                    }
                    requests.postValue(list.filter { req ->
                        req.location.longitude >= filter.longNearbyPoints[0]
                        && req.location.longitude <= filter.longNearbyPoints[1]
                        && req.userInNeedId != getUserId()
                        && filter.categoryList.contains(req.category)
                    })

                }
            }

        return requests
    }

    fun noticeNewRequest(filter: Filter): LiveData<Request> {
        //todo może zapamietac gdzies ostatnie wczytane/ powiadomione i porownywac?
        val request = MutableLiveData<Request>()
        listenerNewRequestRegistration?.remove()
        listenerNewRequestRegistration = cloud.collection("requestsForHelp")
            .whereIn("longitudeZone", filter.longZone)
            .orderBy("id", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { value, _ ->
                Log.d("noticeNewRequest", "triggered")
                if (value != null && !value.isEmpty) {
                    Log.d("noticeNewRequest", "triggered nor empty")
                    Log.d("value.documentChanges[0]", value.documentChanges[0].document.toString())
                    Log.d("value.documentChanges size", value.documentChanges.size.toString())
                    if (value.documentChanges.last().type == DocumentChange.Type.ADDED) {
                        val data =
                            value.documentChanges.last().document.toObject(Request::class.java)
                        Log.d("ADDED", data.toString())
                        if (data.location.longitude >= filter.longNearbyPoints[0]
                            && data.location.longitude <= filter.longNearbyPoints[1]
                            && data.userInNeedId != getUserId()
                            && filter.categoryList.contains(data.category)
                        ) {
                            request.postValue(data)
                        }
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
                "score" to 0F,
                "helpCounter" to 0,
                "opinionList" to emptyList<String>()
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

    fun getUser(id: String): LiveData<User> {
        val user = MutableLiveData<User>()
        if (auth.currentUser != null) {
            cloud.collection("users").document(id)
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

    fun decreaseUsersToken(x: Long) {
        cloud.collection("users").document(auth.currentUser!!.uid)
            .update("tokens", FieldValue.increment(-x))
    }

    fun increaseUsersToken() {
        cloud.collection("users").document(auth.currentUser!!.uid)
            .update("tokens", FieldValue.increment(1))
    }

    fun increaseUsersToken(x: Long) {
        Log.d("increaseUsersToken",x.toString())
        cloud.collection("users").document(auth.currentUser!!.uid)
            .update("tokens", FieldValue.increment(x))
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

    fun setDisplayName(name: String){
        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        auth.currentUser!!.updateProfile(profileUpdates)
        cloud.collection("users").document(auth.currentUser!!.uid).update("name", name)
    }

    //chat

    fun createChat(chat: Chat) {
        cloud.collection("chats").document(chat.id.toString()).set(ChatWithMessages(chat))
    }

    fun clearChat(chat: Chat) {
        cloud.collection("chats").document(chat.id.toString()).update(
            "chat.status", 0,
            "chat.volunteerId", "",
            "chat.volunteerName", "",
            "messages", FieldValue.delete()
        )
    }

    fun deleteChat(chat: Chat) {
        cloud.collection("chats").document(chat.id.toString()).delete()
    }

    fun acceptRequestAndGetChat(request: Request): LiveData<Chat> {
        val chatLiveData = MutableLiveData<Chat>()
        cloud.collection("requestsForHelp").whereEqualTo("id", request.id).limit(1).get()
            .addOnSuccessListener { req ->
                Log.d("addOnSuccessListener", req.toString())
                if (req != null && !req.isEmpty) {
                    req.documents[0].reference.delete()

                    cloud.collection("chats").document(request.id.toString()).update(
                        "chat.volunteerId", auth.currentUser!!.uid,
                        "chat.volunteerName", auth.currentUser!!.displayName!!,
                        "chat.status", 1
                    ).addOnSuccessListener {
                        cloud.collection("chats").document(request.id.toString()).get()
                            .addOnSuccessListener {
                                if (it != null && it.exists()) {
                                    val data = it.toObject(ChatWithMessages::class.java)
                                    data?.let { newChat -> chatLiveData.postValue(newChat.chat) }
                                }
                            }
                    }
                }
            }
        return chatLiveData
    }

    fun getShoppingList(id: Long): LiveData<List<Product>> {
        val products = MutableLiveData<List<Product>>()
        cloud.collection("requestsForHelp").document("shoppingLists").collection("shoppingLists")
            .document(id.toString()).get().addOnSuccessListener { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.toObject(ProductsListWrapper::class.java)
                    data?.let { products.postValue(it.list) }
                }
            }

        return products
    }

    fun createCode(code: Code) {
        cloud.collection("tokenCodes").add(code)
    }

    fun getCode(codeID: Int): LiveData<Code> {
        val code = MutableLiveData<Code>()
        cloud.collection("tokenCodes").whereEqualTo("codeID", codeID).get().addOnSuccessListener {
            if (it.documents.size > 0) {
                val data = it.documents[0].toObject(Code::class.java)
                code.postValue(data)
            }
        }
        return code
    }

    fun getCode(userID: String): LiveData<Code> {
        val code = MutableLiveData<Code>()
        cloud.collection("tokenCodes").whereEqualTo("userID", userID)
            .addSnapshotListener { value, _ ->
                Log.d("getCode firebase", "triggered")
                if (value != null) {
                    Log.d("getCode firebase", "not null")
                    if (value.isEmpty) {
                        Log.d("getCode firebase", "empty")
                        code.postValue(null)
                    } else {
                        Log.d("getCode firebase", "not empty")
                        code.postValue(value.documents[0].toObject(Code::class.java))
                    }
                }
            }
        return code
    }

    fun deleteCode(codeID: Int) {
        cloud.collection("tokenCodes").whereEqualTo("codeID", codeID).get().addOnSuccessListener {
            if (it != null) {
                if (it.documents.size > 0) {
                    val code = it.documents[0].toObject(Code::class.java)
                    increaseUsersToken(code!!.tokenNum.toLong())
                    it.documents[0].reference.delete()
                }
            }
        }
    }

    fun deleteCode(userID: String) {
        cloud.collection("tokenCodes").whereEqualTo("userID", userID).get().addOnSuccessListener {
            if (it != null) {
                if (it.documents.size > 0) {
                    it.documents[0].reference.delete()
                }
            }
        }
    }

    fun getMyChatCloudUpdate(chatsId: List<Long>): LiveData<List<ChatWithMessages>> {
        Log.d("getMyChatCloudUpdate", "enter")
        listenerChat?.remove()
        val chatsLiveData = MutableLiveData<List<ChatWithMessages>>()
        Log.d("chat ids", chatsId.toString())
        if (chatsId.isNotEmpty()) {
            listenerChat = cloud.collection("chats").whereIn("chat.id", chatsId)
                .addSnapshotListener { value, _ ->
                    Log.d("getMyChatCloudUpdate", "trigger")
                    if (value != null && !value.isEmpty) {
                        val array = ArrayList<ChatWithMessages>()
                        for (doc in value.documents) {
                            Log.d("getMyChatCloudUpdate document", doc.data.toString())
                            val data = doc.toObject(ChatWithMessages::class.java)
                            data?.let { array.add(it) }
                        }
                        chatsLiveData.postValue(array)
                    }
                }
        }
        return chatsLiveData
    }

    fun setChatCloudStatus(id: Long, status: Int) {
        cloud.collection("chats").document(id.toString()).update("chat.status", status)
    }

    fun sendMessage(id: Long, message: Message) {
        cloud.collection("chats").document(id.toString())
            .update("messages", FieldValue.arrayUnion(message))
    }

    fun assessUser(userId: String, opinion: String, score: Float) {
        cloud.collection("users").document(userId).get().addOnSuccessListener {
            if (it != null && it.exists()) {
                val user = it.toObject(User::class.java)!!
                val newScore =
                    ((user.score * user.helpCounter) + score) / (user.helpCounter + 1).toFloat()
                cloud.collection("users").document(userId).update(
                    "helpCounter",
                    FieldValue.increment(1),
                    "opinionList",
                    FieldValue.arrayUnion(
                        Opinion(
                            auth.currentUser!!.displayName!!,
                            opinion,
                            score
                        )
                    ),
                    "score",
                    newScore
                )
            }
        }
    }
}