package com.example.ihatemobile

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query


data class LoginRequest(val login: String, val password: String)
data class RegisterRequest(val login: String, val password: String)

data class LoginResponse(
    @SerializedName("UserId") val userId: Int,
    @SerializedName("Token") val token: Token
)

data class Token(
    @SerializedName("AccessToken") val accessToken: String,
    @SerializedName("RefreshToken") val refreshToken: String
)

data class ContactData(
    @SerializedName("ID") val id: Int,
    @SerializedName("Login") val login: String,
    @SerializedName("UserName") val userName: String
)

data class SetUsernameRequest(val username: String)
data class AddContact(val contact_login: String)

data class RefreshTokenRequest(val token: String, val userId: Int)
data class RefreshTokenResponse(val token: String)

data class ChatData(
    val is_direct: Boolean,
    val members_ids: List<Int>,
    val name: String
)

data class ChatDataResponce(val chat_id: Int)

data class AddInChat(val chat_id: Int, val members_ids: List<Int>)

data class AllChatsData(
    @SerializedName("id") val id: Int,
    @SerializedName("Name") val name: String,
    @SerializedName("IsDirect") val isDirect: Boolean,
    @SerializedName("OwnerId") val ownerId: Int,
    @SerializedName("Owner") val owner: Owner
)

data class Owner(
    @SerializedName("id") val id: Int,
    @SerializedName("Login") val login: String,
    @SerializedName("UserName") val userName: String,
    @SerializedName("Password") val password: String
)

data class Messages(
    @SerializedName("ID") val id: Int,
    @SerializedName("Text") val text: String,
    @SerializedName("SendingTime") val sendingTime: String,
    @SerializedName("SenderId") val senderId: Int,
    @SerializedName("UserName") val userName: String,
    @SerializedName("ChatId") val chatId: Int
)

interface ApiService {
    @POST("user/sign-in")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("user/sign-up")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<Void>

    @POST("user/set/username")
    suspend fun setUsername(
        @Body setUsernameRequest: SetUsernameRequest,
        @Header("Authorization") token: String
    ): Response<Void>


    @POST("user/refresh")
    suspend fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): Response<RefreshTokenResponse>

    ////////////////////

    @POST("contact")
    suspend fun addContact(
        @Body addContact: AddContact,
        @Header("Authorization") token: String
    ): Response<Void>

    @GET("contact/all")
    suspend fun showContacts(
        @Header("Authorization") token: String
    ): Response<List<ContactData>>

    @GET("contact/{id}")
    suspend fun showContact(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<ContactData>

    @DELETE("contact/{id}")
    suspend fun deleteContact(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Void>


    ///////////


    @POST("chat")
    suspend fun addChat(
        @Body addContact: ChatData,
        @Header("Authorization") token: String
    ): Response<ChatDataResponce>

    @POST("chat/add/members")
    suspend fun addMembers(
        @Body addContact: AddInChat,
        @Header("Authorization") token: String
    ): Response<Void>

    @GET("chat/all")
    suspend fun showChats(
        @Header("Authorization") token: String
    ): Response<List<AllChatsData>>

    @GET("chat/members/{id}")
    suspend fun showMembers(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<ContactData>

    @GET("chat/messages")
    suspend fun showMessages(
        @Query("chat-id") chat_id: Int,
        @Query("page-id") page_id: Int,
        @Header("Authorization") token: String
    ): Response<List<Messages>>

    @GET("chat/{id}")
    suspend fun showChat(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<ChatDataResponce>

    @DELETE("chat/{id}")
    suspend fun deleteChat(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Void>
}

object NetworkService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://193.124.33.25:8080/api/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    suspend fun logIn(login: String, password: String): Response<LoginResponse> {
        val request = LoginRequest(login, password)
        return apiService.login(request)
    }

    suspend fun register(login: String, password: String): Response<Void> {
        val request = RegisterRequest(login, password)
        return apiService.register(request)
    }

    suspend fun refreshToken(token: String, userId: Int): Response<RefreshTokenResponse> {
        val request = RefreshTokenRequest(token, userId)
        return apiService.refreshToken(request)
    }

    suspend fun setUsername(username: String, token: String): Response<Void> {
        val request = SetUsernameRequest(username)
        val authHeader = "Bearer $token"
        return apiService.setUsername(request, authHeader)
    }

    ///////

    suspend fun addContacts(contactlog: String, token: String): Response<Void> {
        val request = AddContact(contactlog)
        val authHeader = "Bearer $token"
        return apiService.addContact(request, authHeader)
    }

    suspend fun showContacts(token: String): Response<List<ContactData>> {
        val authHeader = "Bearer $token"
        return apiService.showContacts(authHeader)
    }

    suspend fun showContact(token: String, id: Int): Response<ContactData> {
        val authHeader = "Bearer $token"
        return apiService.showContact(id, authHeader)
    }

    suspend fun deleteContact(token: String, id:Int): Response<Void> {
        val authHeader = "Bearer $token"
        return apiService.deleteContact(id, authHeader)
    }

    ///////

    suspend fun createChat(token: String, name: String, members: List<Int>, isDirect: Boolean): Response<ChatDataResponce> {
        val request = ChatData(isDirect, members, name)
        val authHeader = "Bearer $token"
        return apiService.addChat(request, authHeader)
    }

    suspend fun addMember(token: String, chat_id: Int, members_ids: List<Int>): Response<Void> {
        val request = AddInChat(chat_id, members_ids)
        val authHeader = "Bearer $token"
        return apiService.addMembers(request, authHeader)
    }

    suspend fun allChats(token: String): Response<List<AllChatsData>> {
        val authHeader = "Bearer $token"
        return apiService.showChats(authHeader)
    }

    suspend fun allMembersChat(token: String, id: Int): Response<ContactData> {
        val authHeader = "Bearer $token"
        return apiService.showMembers(id, authHeader)
    }

    //вот это пиздец пока что. TODO
    suspend fun showHistoryChat(token: String, id: Int, pageId: Int): Response<List<Messages>> {
        val authHeader = "Bearer $token"
        return apiService.showMessages(id, pageId, authHeader)
    }

    suspend fun chatByID(token: String, id: Int): Response<ChatDataResponce> { //чинить UPDATE: поебать)
        val authHeader = "Bearer $token"
        return apiService.showChat(id, authHeader)
    }

    suspend fun deleteChat(token: String, id: Int): Response<Void> {
        val authHeader = "Bearer $token"
        return apiService.deleteChat(id, authHeader)
    }

}
