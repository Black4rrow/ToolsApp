package com.example.toolsapp.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.toolsapp.model.TodoItem
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database


class TodoViewModel () : ViewModel(){
    private val database: FirebaseDatabase = Firebase.database
    private val myRef = database.getReference("todoItems")

    fun addTodoItem(todoItem: TodoItem) {
        val itemId = todoItem.id.toString()
        itemId.let {
            myRef.child(it).setValue(todoItem)
        }
    }

    fun getTodoItems(userId: String, onDataChange: (List<TodoItem>) -> Unit) {
        myRef.orderByChild("userId").equalTo(userId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val todoItems = mutableListOf<TodoItem>()
                for (itemSnapshot in snapshot.children) {
                    val todoItem = itemSnapshot.getValue(TodoItem::class.java)
                    todoItem?.let {
                        todoItems.add(it)
                    }
                }
                onDataChange(todoItems)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun updateTodoItem(todoItem: TodoItem) {
        val itemId = todoItem.id.toString()
        myRef.child(itemId).setValue(todoItem)
    }

    fun deleteTodoItem(todoItemId: String) {
        myRef.child(todoItemId).removeValue()
    }

}