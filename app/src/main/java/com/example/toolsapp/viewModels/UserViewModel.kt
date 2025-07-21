package com.example.toolsapp.viewModels

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(auth.currentUser != null)
    val authState: StateFlow<Boolean> = _authState

    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData> = _userData

    init{
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser != null
        }
    }

    fun loginUser(email: String, password: String, onSuccess: (userId:String, code:String, userMail: String) -> Unit, onFailure: (Exception) -> Unit) {
        signInOrCreateUserWithEmailAndPassword(email, password, onSuccess, onFailure)
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "1"
    }

    private fun signInOrCreateUserWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: (userId:String, code:String, userMail: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        onSuccess(it, "CONNECTED", email)
                    } ?: run {
                        onFailure(Exception("User ID is null"))
                    }
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { createTask ->
                            if (createTask.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                userId?.let {
                                    onSuccess(it, "ACCOUNT_CREATED", email)
                                } ?: run {
                                    onFailure(Exception("User ID is null"))
                                }
                            } else {
                                onFailure(createTask.exception ?: Exception("Authentication failed"))
                            }
                        }
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = false
    }

    fun emptyUserData(){
        _userData.value = UserData()
    }

    private val database: FirebaseDatabase = Firebase.database
    private val myRef = database.getReference("userData")

    fun saveUserDataToDatabase(userId: String, userData: UserData) {
        userData.email = FirebaseAuth.getInstance().currentUser?.email.toString()
        myRef.child(userId).setValue(userData)
    }

    fun loadUserDataFromDatabase(userId: String, onUserDataLoaded: (UserData) -> Unit) {
        myRef.child(userId).get().addOnSuccessListener { dataSnapshot ->
            val userData = dataSnapshot.getValue(UserData::class.java)
            userData?.let {
                onUserDataLoaded(it)
                _userData.value = it
            }
        }
    }

    fun levelUp(xp: Int) {
        val currentUserData = userData.value
        val xpToNextLevel = getXpToNextLevel()

        val newUserData = if (currentUserData.currentXp + xp >= xpToNextLevel) {
            currentUserData.copy(
                level = currentUserData.level + 1,
                currentXp = (xp - (xpToNextLevel - currentUserData.currentXp)).toInt()
            )
        } else {
            currentUserData.copy(
                currentXp = currentUserData.currentXp + xp
            )
        }

        _userData.value = newUserData
    }

    fun getXpToNextLevel(): Int {
        val currentLevel = userData.value.level
        return ((100 + (currentLevel * 10)) * 1.01f).toInt()
    }


}

data class UserData(
    var userId: String = "",
    var pseudo: String = "Anonymous",
    var email: String = "",
    var phoneNumber: String = "",
    var profilePictureUrl: String = "",
    var level: Int = 1,
    var currentXp: Int = 0,
){

}