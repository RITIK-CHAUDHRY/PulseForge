
package com.pulseforge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.pulseforge.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    init {
        _user.value = auth.currentUser
        _authState.value = if (auth.currentUser != null) AuthState.LoggedIn else AuthState.LoggedOut
        if (auth.currentUser != null) {
            fetchUserProfile()
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.signInWithEmailAndPassword(email, password).await()
                _user.value = auth.currentUser
                fetchUserProfile()
                _authState.value = AuthState.LoggedIn
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.createUserWithEmailAndPassword(email, password).await()
                _user.value = auth.currentUser
                createUserProfile()
                _authState.value = AuthState.LoggedIn
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
        _userProfile.value = null
        _authState.value = AuthState.LoggedOut
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val document = firestore.collection("userProfiles").document(userId).get().await()
                if (document.exists()) {
                    _userProfile.value = UserProfile.fromMap(document.data ?: emptyMap())
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun createUserProfile() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val newProfile = UserProfile(userId = userId)
                firestore.collection("userProfiles").document(userId).set(newProfile.toMap()).await()
                _userProfile.value = newProfile
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateUserProfile(updatedProfile: UserProfile) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                firestore.collection("userProfiles").document(userId).set(updatedProfile.toMap()).await()
                _userProfile.value = updatedProfile
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateStreak() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val currentProfile = _userProfile.value ?: return@launch
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val updatedProfile = if (currentProfile.lastWorkoutDate == null || currentProfile.lastWorkoutDate!! < today) {
                    currentProfile.copy(
                        streakCount = currentProfile.streakCount + 1,
                        lastWorkoutDate = today
                    )
                } else {
                    currentProfile
                }

                firestore.collection("userProfiles").document(userId).set(updatedProfile.toMap()).await()
                _userProfile.value = updatedProfile
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object LoggedIn : AuthState()
    object LoggedOut : AuthState()
    data class Error(val message: String) : AuthState()
}
