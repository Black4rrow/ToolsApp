package com.example.toolsapp.viewModels

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserGameData(
    var frenzyClickerHighScoreMap: MutableMap<String, Int> = mutableMapOf(),
){}

class UserGameDataSingleton private constructor() {
    companion object {

        @Volatile
        private var instance: UserGameDataSingleton? = null

        var userGameData: UserGameData = UserGameData()
        private val userGameDataViewModel = UserGameDataViewModel()

        fun getInstance(): UserGameDataSingleton {
            return instance ?: synchronized(this){
                val localInstance = UserGameDataSingleton()
                instance = localInstance
                localInstance
            }
        }

        fun saveGameDataToDatabase(){
            userGameDataViewModel.saveUserGameDataToDatabase()
        }

        fun loadGameDataFromDatabase(userId: String){
            userGameDataViewModel.getUserGameDataFromDatabase(userId)
        }
    }
}

class UserGameDataViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(auth.currentUser != null)
    val authState: StateFlow<Boolean> = _authState

    private val database: FirebaseDatabase = Firebase.database
    private val myRef = database.getReference("userGameData")

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser != null
        }
    }

    fun saveUserGameDataToDatabase() {
        val userGameData = UserGameDataSingleton.userGameData
        val userId = auth.currentUser?.uid ?: return
        myRef.child(userId).setValue(userGameData)
    }

    fun getUserGameDataFromDatabase(userId: String){
        myRef.child(userId).get().addOnSuccessListener { dataSnapshot ->
            val userGameData = dataSnapshot.getValue(UserGameData::class.java)
            userGameData?.let {
                UserGameDataSingleton.userGameData = it
            }
        }
    }
}