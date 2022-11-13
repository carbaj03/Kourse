package com.example.myapplication.empty.user

import arrow.core.Either
import com.example.myapplication.empty.common.DBError
import com.example.myapplication.empty.common.DomainError
import com.example.myapplication.empty.common.NetworkError

interface UserRepository {
    fun logIn(): Either<DomainError, User>
    fun logOut(): Either<DomainError, User>
    fun signIn(): Either<DomainError, User>
    
    fun getUser(): Either<DomainError, User>
}

object Network {
    data class User(val name: String)
}

interface UserNetwork {
    fun get(): Either<NetworkError, Network.User>
}

object DB {
    data class User(val name: String)
}

interface UserDB {
    fun get(): Either<DBError, DB.User>
}

context(UserNetwork, UserDB)
fun UserRepository(): UserRepository =
    object : UserRepository {
        override fun logIn(): Either<DomainError, User> {
            TODO("Not yet implemented")
        }
        
        override fun logOut(): Either<DomainError, User> {
            TODO("Not yet implemented")
        }
        
        override fun signIn(): Either<DomainError, User> {
            TODO("Not yet implemented")
        }
        
        override fun getUser(): Either<DomainError, User> {
            TODO("Not yet implemented")
        }
    }