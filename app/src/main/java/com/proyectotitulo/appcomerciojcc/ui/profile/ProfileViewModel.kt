package com.proyectotitulo.appcomerciojcc.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _username = MutableLiveData<String>()
    val username : LiveData<String> = _username

    private val _averageScore = MutableLiveData<Float>()
    val averageScore : LiveData<Float> = _averageScore

    private val _registerDate = MutableLiveData<String>()
    val registerDate : LiveData<String> = _registerDate

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val result = repository.getPrivateProfile()
            result.onSuccess { response ->
                if (response.status == "success") {
                    _username.value = response.data.username
                    _averageScore.value = response.data.averageScore
                    _registerDate.value = response.data.formattedRegisterDate
                }
            }.onFailure { }
        }
    }
}