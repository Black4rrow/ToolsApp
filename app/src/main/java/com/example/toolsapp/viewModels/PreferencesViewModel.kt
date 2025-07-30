package com.example.toolsapp.viewModels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.toolsapp.model.classes.PersistableSettings
import com.example.toolsapp.model.classes.Settings
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesViewModel : ViewModel() {
    private val database: FirebaseDatabase = Firebase.database
    private val mySettingsRef = database.getReference("userSettings")
    private val myToolsFavoritesRef = database.getReference("userFavoriteTools")

    fun saveToolsFavoritesToDatabase(userId: String, tools: List<String>) {
        myToolsFavoritesRef.child(userId).setValue(tools)
    }

    private val _favoriteTools = MutableStateFlow<List<String>>(emptyList())
    val favoriteTools: StateFlow<List<String>> = _favoriteTools
    private var toolsListener: ValueEventListener? = null

    fun observeToolsFavorites(userId: String) {
        toolsListener?.let{
            myToolsFavoritesRef.child(userId).removeEventListener(it)
        }

        toolsListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val tools = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                _favoriteTools.value = tools
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read tools favorites", error.toException())
            }
        }

        myToolsFavoritesRef.child(userId).addValueEventListener(toolsListener!!)
    }

    fun toggleToolFavorite(userId: String, toolName: String) {
        val ref = myToolsFavoritesRef.child(userId)

        ref.get().addOnSuccessListener { snapshot ->
            val currentFavorites = snapshot.children.mapNotNull { it.getValue(String::class.java) }

            val updatedFavorites = if (toolName in currentFavorites) {
                currentFavorites.filterNot { it == toolName }
            } else {
                currentFavorites + toolName
            }

            ref.setValue(updatedFavorites)
        }.addOnFailureListener {
            Log.e("Firebase", "Failed to toggle tool favorite", it)
        }
    }

    fun saveSettingsToDatabase(userId: String, settings: PersistableSettings) {
        mySettingsRef.child(userId).setValue(settings)
    }

    fun loadSettingsFromDatabase(userId: String, onSettingsLoaded: (PersistableSettings) -> Unit) {
        mySettingsRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val settings = snapshot.getValue(PersistableSettings::class.java)
                settings?.let {
                    onSettingsLoaded(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        toolsListener?.let{
            myToolsFavoritesRef.removeEventListener(it)
        }
    }
}