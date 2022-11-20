package com.example.myapplication.empty.user

import android.util.Log
import arrow.core.Either
import arrow.core.right
import com.example.myapplication.empty.common.DomainError
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

interface UserRepository {
    suspend fun logIn(username: String, password: String): Either<DomainError, User>
    suspend fun logOut(): Either<DomainError, Success>
    suspend fun signIn(): Either<DomainError, User>
    
    suspend fun getUser(): Either<DomainError, User>
}

object Network {
    data class User(val name: String)
}

interface UserNetwork {
}

fun UserNetwork(
    client: HttpClient,
): UserNetwork =
    object : UserNetwork {
//        override suspend fun login(
//            name: String,
//            password: String,
//        ) {
//            try {
////            val response = client.get("http://10.0.2.2:5000/")
//                //                val response = client.get("http://kourse-env.eba-xhpbikif.us-east-1.elasticbeanstalk.com/")
//                val response = client.get("http://192.168.0.101:5000/books")
//                Log.e("ktor", response.toString())
//
////                val response2 = client.get("http://10.0.2.2:5000/customer")
////                Log.e("ktor", response2.toString())
//            } catch (e: Exception) {
//                Log.e("ktor", e.message.toString())
//            }
//        }
    }

object DB {
    data class User(val name: String)
}

interface UserDB

fun UserDB() = object : UserDB {}

object Success

interface RepositoryDispatcher {
    val io: CoroutineContext
    val default: CoroutineContext
}

context(UserNetwork, UserDB, RepositoryDispatcher)
fun UserRepository(): UserRepository =
    object : UserRepository {
        var user: User? = null
        
        override suspend fun logIn(username: String, password: String): Either<DomainError, User> =
            withContext(io) {
                User(username, password).right().also { it.map { user = it } }
            }
        
        override suspend fun logOut(): Either<DomainError, Success> =
            withContext(io) {
                Success.right().also { user = null }
            }
        
        override suspend fun signIn(): Either<DomainError, User> {
            TODO("Not yet implemented")
        }
        
        override suspend fun getUser(): Either<DomainError, User> {
            TODO("Not yet implemented")
        }
    }