package com.example.toolsapp.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.toolsapp.model.classes.Settings
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class SettingsViewModel : ViewModel() {
    private val database: FirebaseDatabase = Firebase.database
    private val myRef = database.getReference("userSettings")

    fun saveToDatabase(userId: String, settings: Settings) {
        myRef.child(userId).setValue(settings)
    }

    fun loadFromDatabase(userId: String, onSettingsLoaded: (Settings) -> Unit) {
        myRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val settings = snapshot.getValue(Settings::class.java)
                settings?.let {
                    onSettingsLoaded(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}