package com.example.pomocnysasiad.model

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalRepository(context: Context) {
    private var requestDao: RequestDao
    private var productDao: ProductDao
    private var chatDao: ChatDao
    private var messageDao: MessageDao

    init {
        val database = LocalDatabase.getInstance(context)
        requestDao = database!!.requestDao()
        productDao = database.productDao()
        chatDao = database.chatDao()
        messageDao = database.messageDao()
    }

    fun insertManyProducts(products: List<Product>) =
        CoroutineScope(Dispatchers.IO).launch {
            productDao.insertMany(products)
        }

    fun updateProduct(product: Product) =
        CoroutineScope(Dispatchers.IO).launch {
            productDao.update(product)
        }

    fun deleteProduct(product: Product) =
        CoroutineScope(Dispatchers.IO).launch {
            productDao.delete(product)
        }

    fun deleteManyProducts(products: List<Product>) =
        CoroutineScope(Dispatchers.IO).launch {
            productDao.deleteMany(products)
        }

    fun deleteProductsByListId(id: Long) =
        CoroutineScope(Dispatchers.IO).launch {
            productDao.deleteProductsByListId(id)
        }

    fun insertRequest(request: Request) =
        CoroutineScope(Dispatchers.IO).launch {
            requestDao.insertRequest(request)
        }

    fun deleteRequest(request: Request) =
        CoroutineScope(Dispatchers.IO).launch {
            requestDao.deleteRequest(request)
        }

    fun deleteRequestById(id: Long) =
        CoroutineScope(Dispatchers.IO).launch {
            requestDao.deleteRequestById(id)
        }

    fun updateRequest(request: Request) =
        CoroutineScope(Dispatchers.IO).launch {
            requestDao.updateRequest(request)
        }

    fun getAllAcceptedRequest(uid: String): LiveData<List<Request>> =
        requestDao.getAllAcceptedRequest(uid)

    fun getAllMyRequest(uid: String): LiveData<List<Request>> =
        requestDao.getAllMyRequest(uid)

    fun getRequestWithShoppingListById(uid: Long): LiveData<RequestWithShoppingList> =
        requestDao.getRequestWithShoppingListById(uid)

    fun insertMessage(message: Message) =
        CoroutineScope(Dispatchers.IO).launch {
            messageDao.insertMessage(message)
        }

    fun insertMessages(messages: List<Message>) =
        CoroutineScope(Dispatchers.IO).launch {
            messageDao.insertMessages(messages)
        }

    fun deleteMessagesByChatId(id: Long) =
        CoroutineScope(Dispatchers.IO).launch {
            messageDao.deleteMessagesByChatId(id)
        }

    fun insertChat(chat: Chat) =
        CoroutineScope(Dispatchers.IO).launch {
            chatDao.insertChat(chat)
        }

    fun deleteChat(chat: Chat) =
        CoroutineScope(Dispatchers.IO).launch {
            chatDao.deleteChat(chat)
        }

    fun updateChat(chat: Chat) =
        CoroutineScope(Dispatchers.IO).launch {
            chatDao.updateChat(chat)
        }

    fun updateChats(chats: List<Chat>) =
        CoroutineScope(Dispatchers.IO).launch {
            chatDao.updateChats(chats)
        }

    fun getChatById(id: Long): LiveData<ChatWithMessages> = chatDao.getChatById(id)

    fun getAllVolunteerChats(id: String): LiveData<List<Chat>> = chatDao.getAllVolunteerChats(id)

    fun getAllInNeedChats(id: String): LiveData<List<Chat>> = chatDao.getAllInNeedChats(id)
}