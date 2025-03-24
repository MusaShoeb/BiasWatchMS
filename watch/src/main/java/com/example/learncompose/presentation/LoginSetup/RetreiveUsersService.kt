package com.example.learncompose.presentation.LoginSetup

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.MutableState
import com.google.firebase.Timestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Employee(val fullName : String, val employeeID: String)

class RetreiveUsersService(private val employeeListState: MutableState<List<Employee>>) {

    var employeeList = mutableListOf<Employee>()

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var myRef: DatabaseReference = database.getReference("EHTS/EHTS2025/employees")

    data class TestData (
            var sp02 : Int = 0,
            var heartRate: List<Int> = emptyList()
            )

    fun getCurrentTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return currentDateTime.format(formatter)
    }

    fun getUsers() {
        myRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (postSnapshot in dataSnapshot.children) {
                    val username = postSnapshot.child("fullName").getValue(String::class.java)
                    val employeeID = postSnapshot.child("employeeID").getValue(String::class.java)
                    if (username != null && employeeID != null) {
                        val newEmployee = Employee(username, employeeID)
                        employeeList.add(newEmployee)
                        Log.d("New User", "$username")
                        Log.d("New User ID: ", "$employeeID")
                    }
                }
                    employeeListState.value = employeeList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Log any error from Firebase
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    fun createUserTest(currentUser: String) {

        val testId : String = "Test_" + getCurrentTime()

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot){
                for (postSnapshot in snapshot.children) {
                    // Use '==' for string comparison
                    val fullName = postSnapshot.child("employeeID").getValue(String::class.java)
                    if (fullName == currentUser) {
                        // Create a new test instance to store
                        val newTest = TestData()
                        // Generate a custom ID for the test

                        // Get the reference to the location where you want to store the new test
                        val userTestRef = postSnapshot.ref.child(testId) // Use child(testId) on ref to set a custom ID

                        // Store the new test data under the generated test ID
                        userTestRef.setValue(newTest).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Success", "New Test Complete")
                            } else {
                                Log.d("Failure", "New Test Was not Pushed")
                            }
                        }
                        break // Exit loop once the correct user is found
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Firebase Error", "$error")
            }
        })
    }

}